/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.viewmodels

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.metrolist.innertube.YouTube
import com.metrolist.innertube.models.PlaylistItem
import com.metrolist.music.constants.HideVideoSongsKey
import com.metrolist.music.constants.PlaylistSongSortDescendingKey
import com.metrolist.music.constants.PlaylistSongSortType
import com.metrolist.music.constants.PlaylistSongSortTypeKey
import com.metrolist.music.db.MusicDatabase
import com.metrolist.music.db.entities.PlaylistSong
import com.metrolist.music.domain.manager.DataStoreManager
import com.metrolist.music.domain.mediaservice.handler.PlaylistType
import com.metrolist.music.domain.mediaservice.handler.QueueData
import com.metrolist.music.domain.utils.FilterState
import com.metrolist.music.extensions.reversed
import com.metrolist.music.extensions.toEnum
import com.metrolist.music.models.toMediaMetadata
import com.metrolist.music.utils.dataStore
import com.metrolist.music.viewmodels.xevrae.LocalPlaylistState
import com.metrolist.music.viewmodels.xevrae.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Collator
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class LocalPlaylistViewModel
@Inject
constructor(
    @ApplicationContext context: Context,
    private val database: MusicDatabase,
    private val dataStoreManager: DataStoreManager,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel(context) {
    val playlistId = savedStateHandle.get<String>("playlistId")!!

    private val _uiState: MutableStateFlow<LocalPlaylistState> = MutableStateFlow(LocalPlaylistState.initial())
    val uiState: StateFlow<LocalPlaylistState> get() = _uiState

    val playlist =
        database
            .playlist(playlistId)
            .stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val _onlinePlaylist = MutableStateFlow<PlaylistItem?>(null)
    val onlinePlaylist: StateFlow<PlaylistItem?> = _onlinePlaylist

    val playlistSongs: StateFlow<List<PlaylistSong>> =
        combine(
            database.playlistSongs(playlistId),
            context.dataStore.data
                .map {
                    Triple(
                        it[PlaylistSongSortTypeKey].toEnum(PlaylistSongSortType.CUSTOM),
                        it[PlaylistSongSortDescendingKey] ?: true,
                        it[HideVideoSongsKey] ?: false
                    )
                }.distinctUntilChanged(),
        ) { songs, (sortType, sortDescending, hideVideoSongs) ->
            val filteredSongs = if (hideVideoSongs) {
                songs.filter { !it.song.song.isVideo }
            } else {
                songs
            }
            when (sortType) {
                PlaylistSongSortType.CUSTOM -> filteredSongs
                PlaylistSongSortType.CREATE_DATE -> filteredSongs.sortedBy { it.map.id }
                PlaylistSongSortType.NAME -> {
                    val collator = Collator.getInstance(Locale.getDefault())
                    collator.strength = Collator.PRIMARY
                    filteredSongs.sortedWith(compareBy(collator) { it.song.song.title })
                }
                PlaylistSongSortType.ARTIST -> {
                    val collator = Collator.getInstance(Locale.getDefault())
                    collator.strength = Collator.PRIMARY
                    filteredSongs
                        .sortedWith(compareBy(collator) { song -> song.song.artists.joinToString("") { it.name } })
                        .groupBy { it.song.album?.title }
                        .flatMap { (_, songsByAlbum) ->
                            songsByAlbum.sortedBy {
                                it.song.artists.joinToString(
                                    ""
                                ) { it.name }
                            }
                        }
                }

                PlaylistSongSortType.PLAY_TIME -> filteredSongs.sortedBy { it.song.song.totalPlayTime }
            }.reversed(sortDescending && sortType != PlaylistSongSortType.CUSTOM)
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        // Sync uiState and metadata
        viewModelScope.launch {
            playlist.collectLatest { playlist ->
                if (playlist != null) {
                    _uiState.update {
                        it.copy(
                            id = playlist.playlist.id.removePrefix("LP").toLongOrNull() ?: 0L,
                            title = playlist.playlist.name,
                            thumbnail = playlist.playlist.thumbnailUrl,
                            inLibrary = playlist.playlist.bookmarkedAt,
                            trackCount = playlist.songCount,
                            loadState = LocalPlaylistState.PlaylistLoadState.Success
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            val filter = when (dataStoreManager.localPlaylistFilter.first()) {
                DataStoreManager.LOCAL_PLAYLIST_FILTER_OLDER_FIRST -> FilterState.OlderFirst
                DataStoreManager.LOCAL_PLAYLIST_FILTER_NEWER_FIRST -> FilterState.NewerFirst
                DataStoreManager.LOCAL_PLAYLIST_FILTER_TITLE -> FilterState.Title
                DataStoreManager.LOCAL_PLAYLIST_FILTER_CUSTOM_ORDER -> FilterState.CustomOrder
                else -> FilterState.OlderFirst
            }
            _uiState.update { it.copy(filterState = filter) }
        }

        // Metrolist's position sync
        viewModelScope.launch {
            playlistSongs.collectLatest { songs ->
                if (songs.isEmpty()) return@collectLatest
                val sortedSongs = songs.sortedWith(compareBy({ it.map.position }, { it.map.id }))
                database.transaction {
                    sortedSongs.forEachIndexed { index, playlistSong ->
                        if (playlistSong.map.position != index) {
                            update(playlistSong.map.copy(position = index))
                        }
                    }
                }
            }
        }

        // Metrolist's online sync
        viewModelScope.launch {
            playlist.collectLatest { localPlaylist ->
                val browseId = localPlaylist?.playlist?.browseId
                if (browseId != null) {
                    val page = withContext(Dispatchers.IO) {
                        YouTube.playlist(browseId).getOrNull()
                    }
                    val online = page?.playlist
                    _onlinePlaylist.value = online
                }
            }
        }
    }

    fun setBrush(brush: List<Color>) {
        _uiState.update { it.copy(colors = brush) }
    }

    fun onUIEvent(ev: LocalPlaylistUIEvent) {
        when (ev) {
            is LocalPlaylistUIEvent.ChangeFilter -> {
                viewModelScope.launch {
                    dataStoreManager.setLocalPlaylistFilter(
                        when (ev.filterState) {
                            FilterState.OlderFirst -> DataStoreManager.LOCAL_PLAYLIST_FILTER_OLDER_FIRST
                            FilterState.NewerFirst -> DataStoreManager.LOCAL_PLAYLIST_FILTER_NEWER_FIRST
                            FilterState.Title -> DataStoreManager.LOCAL_PLAYLIST_FILTER_TITLE
                            FilterState.CustomOrder -> DataStoreManager.LOCAL_PLAYLIST_FILTER_CUSTOM_ORDER
                        }
                    )
                    _uiState.update { it.copy(filterState = ev.filterState) }
                }
            }

            is LocalPlaylistUIEvent.ItemClick -> {
                val songs = playlistSongs.value.map { it.song.toMediaMetadata().toYTItem() }
                val clickedSong = songs.find { it.id == ev.videoId } ?: return
                
                setQueueData(
                    QueueData.Data(
                        listTracks = songs,
                        firstPlayedTrack = clickedSong,
                        playlistId = "LP" + _uiState.value.id,
                        playlistName = "${getString(com.metrolist.music.R.string.playlist)} \"${_uiState.value.title}\"",
                        playlistType = PlaylistType.LOCAL_PLAYLIST,
                    )
                )
                loadMediaItem(clickedSong, "PLAYLIST_CLICK", songs.indexOf(clickedSong))
            }

            is LocalPlaylistUIEvent.PlayClick -> {
                val songs = playlistSongs.value.map { it.song.toMediaMetadata().toYTItem() }
                if (songs.isEmpty()) {
                    makeToast(getString(com.metrolist.music.R.string.playlist_is_empty))
                    return
                }
                
                setQueueData(
                    QueueData.Data(
                        listTracks = songs,
                        firstPlayedTrack = songs.first(),
                        playlistId = "LP" + _uiState.value.id,
                        playlistName = "${getString(com.metrolist.music.R.string.playlist)} \"${_uiState.value.title}\"",
                        playlistType = PlaylistType.LOCAL_PLAYLIST,
                    )
                )
                loadMediaItem(songs.first(), "PLAYLIST_CLICK", 0)
            }

            is LocalPlaylistUIEvent.ShuffleClick -> {
                val songs = playlistSongs.value.map { it.song.toMediaMetadata().toYTItem() }
                if (songs.isEmpty()) {
                    makeToast(getString(com.metrolist.music.R.string.playlist_is_empty))
                    return
                }
                val shuffled = songs.shuffled()
                setQueueData(
                    QueueData.Data(
                        listTracks = shuffled,
                        firstPlayedTrack = shuffled.first(),
                        playlistId = "LP" + _uiState.value.id,
                        playlistName = "${getString(com.metrolist.music.R.string.playlist)} \"${_uiState.value.title}\"",
                        playlistType = PlaylistType.LOCAL_PLAYLIST,
                    )
                )
                shufflePlaylist(0)
            }
            else -> {}
        }
    }

    fun deletePlaylist(id: String) {
        viewModelScope.launch {
            database.transaction {
                playlistByBrowseId(id)?.let { delete(it.playlist) }
            }
        }
    }

    fun deleteItem(playlistId: String, songId: String) {
        viewModelScope.launch {
            database.transaction {
                deletePlaylistSongMap(playlistId, songId)
            }
        }
    }
}

sealed class LocalPlaylistUIEvent {
    data class ChangeFilter(val filterState: FilterState) : LocalPlaylistUIEvent()
    data class ItemClick(val videoId: String) : LocalPlaylistUIEvent()
    data class SuggestionsItemClick(val videoId: String) : LocalPlaylistUIEvent()
    data object PlayClick : LocalPlaylistUIEvent()
    data object ShuffleClick : LocalPlaylistUIEvent()
}
