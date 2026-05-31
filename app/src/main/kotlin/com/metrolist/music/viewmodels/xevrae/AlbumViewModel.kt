package com.metrolist.music.viewmodels.xevrae

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import com.metrolist.innertube.YouTube
import com.metrolist.innertube.models.AlbumItem
import com.metrolist.innertube.models.Artist
import com.metrolist.innertube.models.SongItem
import com.metrolist.music.db.MusicDatabase
import com.metrolist.music.db.entities.Album
import com.metrolist.music.db.entities.Song
import com.metrolist.music.db.entities.AlbumEntity
import com.metrolist.music.playback.DownloadUtil
import com.metrolist.music.ui.theme.md_theme_dark_background
import com.metrolist.music.viewmodels.xevrae.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import android.content.Context
import androidx.core.net.toUri
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import com.metrolist.music.domain.mediaservice.handler.PlaylistType
import com.metrolist.music.domain.mediaservice.handler.QueueData
import com.metrolist.music.playback.ExoDownloadService
import dagger.hilt.android.qualifiers.ApplicationContext

@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val database: MusicDatabase,
    private val downloadUtil: DownloadUtil,
    @ApplicationContext context: Context,
) : BaseViewModel(context) {
    private val _uiState: MutableStateFlow<AlbumUIState> = MutableStateFlow(AlbumUIState.initial())
    val uiState: StateFlow<AlbumUIState> = _uiState

    private var job: Job? = null
    private var collectDownloadStateJob: Job? = null

    fun updateBrowseId(browseId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(browseId = browseId) }
            
            // Get local data first
            database.album(browseId).collectLatest { album ->
                if (album != null) {
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
            }

            YouTube.album(browseId).onSuccess { page ->
                val data = page.album
                _uiState.update {
                    it.copy(
                        browseId = browseId,
                        title = data.title,
                        thumbnail = data.thumbnail,
                        artist = data.artists?.firstOrNull() ?: Artist("", null),
                        year = data.year?.toString() ?: LocalDateTime.now().year.toString(),
                        trackCount = page.songs.size,
                        description = null, // AlbumPage doesn't have description in Metrolist
                        length = "", // AlbumPage doesn't have duration in Metrolist
                        listTrack = page.songs,
                        otherVersion = page.otherVersions,
                        loadState = LocalPlaylistState.PlaylistLoadState.Success,
                    )
                }
                
                // Sync with database
                database.transaction {
                    val existing = album(browseId)
                    if (existing == null) {
                        insert(page)
                    } else {
                        // We need to fetch the existing Album with songs to update it properly
                        // For now just insert/upsert should be handled by DatabaseDao.insert(AlbumPage)
                        insert(page) 
                    }
                }
                
                getAlbumFlow(browseId)
            }.onFailure { res ->
                log("Error: ${res.message}", LogLevel.ERROR)
                makeToast(getString(com.metrolist.music.R.string.error) + ": ${res.message}")
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
                val album = album(browseId)
                if (album != null) {
                    update(album.album.copy(bookmarkedAt = if (album.album.bookmarkedAt == null) LocalDateTime.now() else null))
                }
            }
            _uiState.update {
                it.copy(
                    liked = !it.liked,
                )
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
                    var count = 0
                    uiState.value.listTrack.forEach { track ->
                        if (downloads[track.id]?.state == androidx.media3.exoplayer.offline.Download.STATE_COMPLETED) {
                            count++
                        }
                    }
                    // Metrolist doesn't have a specific "album download state" in the same way, 
                    // but we can track it in UI state
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
                playlistName = "${getString(com.metrolist.music.R.string.album)} \"${uiState.title}\"",
                playlistType = PlaylistType.PLAYLIST,
            ),
        )
        val index = uiState.listTrack.indexOf(track)
        loadMediaItem(track, "ALBUM_CLICK", if (index == -1) 0 else index)
    }

    fun shuffle() {
        if (uiState.value.listTrack.isEmpty()) {
            makeToast(getString(com.metrolist.music.R.string.playlist_is_empty))
            return
        }
        val shuffleList = uiState.value.listTrack.shuffled()
        val randomIndex = shuffleList.indices.random()
        setQueueData(
            QueueData.Data(
                listTracks = shuffleList,
                firstPlayedTrack = shuffleList[randomIndex],
                playlistId = uiState.value.browseId.replaceFirst("VL", ""),
                playlistName = "${getString(com.metrolist.music.R.string.album)} \"${uiState.title}\"",
                playlistType = PlaylistType.PLAYLIST,
            ),
        )
        loadMediaItem(shuffleList[randomIndex], "ALBUM_CLICK", randomIndex)
    }

    fun downloadFullAlbum() {
        viewModelScope.launch {
            val songs = uiState.value.listTrack
            if (songs.isEmpty()) {
                makeToast(getString(com.metrolist.music.R.string.playlist_is_empty))
                return@launch
            }
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
    val downloadState: Int = 0, // 0 = STATE_NOT_DOWNLOADED
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