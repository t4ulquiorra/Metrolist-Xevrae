/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 * Merged with Xevrae UI
 */

package com.metrolist.music.viewmodels

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import com.metrolist.innertube.YouTube
import com.metrolist.innertube.models.AlbumItem
import com.metrolist.innertube.models.Artist
import com.metrolist.innertube.models.SongItem
import com.metrolist.music.db.MusicDatabase
import com.metrolist.music.db.entities.Album
import com.metrolist.music.domain.mediaservice.handler.PlaylistType
import com.metrolist.music.domain.mediaservice.handler.QueueData
import com.metrolist.music.playback.DownloadUtil
import com.metrolist.music.playback.ExoDownloadService
import com.metrolist.music.ui.theme.xevrae.md_theme_dark_background
import com.metrolist.music.viewmodels.xevrae.LocalPlaylistState
import com.metrolist.music.viewmodels.xevrae.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val database: MusicDatabase,
    private val downloadUtil: DownloadUtil,
    private val savedStateHandle: SavedStateHandle,
    @ApplicationContext context: Context,
) : BaseViewModel(context) {

    val albumId = savedStateHandle.get<String>("albumId") ?: savedStateHandle.get<String>("browseId") ?: ""
    val playlistId = MutableStateFlow("")
    
    // Maintain Metrolist's original flow for compatibility
    val albumWithSongs = if (albumId.isNotEmpty()) {
        database.albumWithSongs(albumId).stateIn(viewModelScope, SharingStarted.Eagerly, null)
    } else {
        MutableStateFlow(null)
    }
    
    var otherVersions = MutableStateFlow<List<AlbumItem>>(emptyList())

    private val _uiState: MutableStateFlow<AlbumUIState> = MutableStateFlow(AlbumUIState.initial())
    val uiState: StateFlow<AlbumUIState> = _uiState

    private var job: Job? = null
    private var collectDownloadStateJob: Job? = null

    init {
        if (albumId.isNotEmpty()) {
            updateBrowseId(albumId)
        }
    }

    fun updateBrowseId(browseId: String) {
        if (browseId.isEmpty()) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(browseId = browseId) }
            
            // Get local data first
            database.album(browseId).first()?.let { album ->
                _uiState.update {
                    it.copy(
                        title = album.album.title,
                        thumbnail = album.album.thumbnailUrl,
                        artist = album.artists.firstOrNull()?.let { a -> Artist(a.name, a.id) } ?: Artist("", null),
                        year = album.album.year?.toString() ?: "",
                        trackCount = album.album.songCount,
                        liked = album.album.bookmarkedAt != null,
                        loadState = LocalPlaylistState.PlaylistLoadState.Success
                    )
                }
            }

            YouTube.album(browseId).onSuccess { page ->
                val data = page.album
                playlistId.value = data.playlistId
                otherVersions.value = page.otherVersions
                
                _uiState.update {
                    it.copy(
                        browseId = browseId,
                        title = data.title,
                        thumbnail = data.thumbnail,
                        artist = data.artists?.firstOrNull() ?: Artist("", null),
                        year = data.year?.toString() ?: LocalDateTime.now().year.toString(),
                        trackCount = page.songs.size,
                        listTrack = page.songs,
                        otherVersion = page.otherVersions,
                        loadState = LocalPlaylistState.PlaylistLoadState.Success,
                    )
                }
                
                // Sync with database
                database.transaction {
                    val existing = album(browseId).first()
                    if (existing == null) {
                        insert(page)
                    } else {
                        update(existing.album, page, existing.artists)
                    }
                }
                
                getAlbumFlow(browseId)
            }.onFailure { res ->
                if (uiState.value.loadState != LocalPlaylistState.PlaylistLoadState.Success) {
                    _uiState.update {
                        it.copy(
                            loadState = LocalPlaylistState.PlaylistLoadState.Error,
                        )
                    }
                }
            }
        }
    }

    fun setBrush(brush: List<Color>) {
        _uiState.update {
            it.copy(
                colors = brush,
            )
        }
    }

    fun setAlbumLike() {
        viewModelScope.launch {
            val browseId = uiState.value.browseId
            database.transaction {
                val album = album(browseId).first()
                if (album != null) {
                    update(album.album.copy(bookmarkedAt = if (album.album.bookmarkedAt == null) LocalDateTime.now() else null))
                }
            }
        }
    }

    private fun getAlbumFlow(browseId: String) {
        job?.cancel()
        collectDownloadStateJob?.cancel()
        job =
            viewModelScope.launch {
                database.album(browseId).collectLatest { album ->
                    if (album != null) {
                        _uiState.update {
                            it.copy(
                                liked = album.album.bookmarkedAt != null,
                            )
                        }
                    }
                }
            }
        collectDownloadStateJob =
            viewModelScope.launch {
                downloadUtil.downloads.collectLatest { downloads ->
                    // Optional: update track download state in uiState
                }
            }
    }

    fun playTrack(track: SongItem) {
        val uiState = _uiState.value
        setQueueData(
            QueueData.Data(
                listTracks = uiState.listTrack,
                firstPlayedTrack = track,
                playlistId = uiState.browseId.replaceFirst("VL", ""),
                playlistName = uiState.title,
                playlistType = PlaylistType.PLAYLIST,
            ),
        )
        val index = uiState.listTrack.indexOf(track)
        loadMediaItem(track, "ALBUM_CLICK", if (index == -1) 0 else index)
    }

    fun shuffle() {
        if (uiState.value.listTrack.isEmpty()) return
        
        val shuffleList = uiState.value.listTrack.shuffled()
        val randomIndex = shuffleList.indices.random()
        setQueueData(
            QueueData.Data(
                listTracks = shuffleList,
                firstPlayedTrack = shuffleList[randomIndex],
                playlistId = uiState.value.browseId.replaceFirst("VL", ""),
                playlistName = uiState.value.title,
                playlistType = PlaylistType.PLAYLIST,
            ),
        )
        loadMediaItem(shuffleList[randomIndex], "ALBUM_CLICK", randomIndex)
    }

    fun downloadFullAlbum() {
        viewModelScope.launch {
            val songs = uiState.value.listTrack
            if (songs.isEmpty()) return@launch
            
            songs.forEach { song ->
                DownloadService.sendAddDownload(
                    context,
                    ExoDownloadService::class.java,
                    DownloadRequest.Builder(song.id, song.id.toUri()).build(),
                    false
                )
            }
        }
    }
}

data class AlbumUIState(
    val browseId: String = "",
    val title: String = "",
    val thumbnail: String? = null,
    val colors: List<Color> = listOf(Color(0xFF121212), md_theme_dark_background),
    val artist: Artist = Artist("", null),
    val year: String = LocalDateTime.now().year.toString(),
    val liked: Boolean = false,
    val trackCount: Int = 0,
    val description: String? = null,
    val length: String = "",
    val listTrack: List<SongItem> = emptyList(),
    val otherVersion: List<AlbumItem> = emptyList(),
    val loadState: LocalPlaylistState.PlaylistLoadState = LocalPlaylistState.PlaylistLoadState.Loading,
) {
    companion object {
        fun initial(): AlbumUIState = AlbumUIState()
    }
}
