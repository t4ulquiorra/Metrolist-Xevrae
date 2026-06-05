@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.metrolist.music.viewmodels.xevrae

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import com.metrolist.innertube.YouTube
import com.metrolist.innertube.models.Artist
import com.metrolist.innertube.models.SongItem
import com.metrolist.innertube.models.WatchEndpoint
import com.metrolist.music.db.MusicDatabase
import com.metrolist.music.db.entities.PlaylistEntity
import com.metrolist.music.db.entities.SongEntity
import com.metrolist.music.domain.mediaservice.handler.PlaylistType
import com.metrolist.music.domain.mediaservice.handler.QueueData
import com.metrolist.music.models.xevrae.PlaylistState
import com.metrolist.music.playback.DownloadUtil
import com.metrolist.music.playback.ExoDownloadService
import com.metrolist.music.viewmodels.xevrae.PlaylistUIState.Error
import com.metrolist.music.viewmodels.xevrae.PlaylistUIState.Loading
import com.metrolist.music.viewmodels.xevrae.PlaylistUIState.Success
import com.metrolist.music.viewmodels.xevrae.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val database: MusicDatabase,
    private val downloadUtil: DownloadUtil,
    @ApplicationContext context: Context,
) : BaseViewModel(context) {
    private var _uiState = MutableStateFlow<PlaylistUIState>(Loading)
    val uiState: StateFlow<PlaylistUIState> = _uiState

    private var _listColors = MutableStateFlow<List<Color>>(emptyList())
    val listColors: StateFlow<List<Color>> = _listColors

    private var _continuation = MutableStateFlow<String?>(null)
    val continuation: StateFlow<String?> = _continuation

    private var _playlistEntity: MutableStateFlow<PlaylistEntity?> = MutableStateFlow(null)
    var playlistEntity: StateFlow<PlaylistEntity?> = _playlistEntity

    val downloadState = MutableStateFlow(0) // Simplified
    val liked = _playlistEntity.map { it?.bookmarkedAt != null }.stateIn(viewModelScope, WhileSubscribed(1000), false)

    private var _tracks = MutableStateFlow<List<SongItem>>(emptyList())
    val tracks: StateFlow<List<SongItem>> = _tracks

    private var _tracksListState = MutableStateFlow<ListState>(ListState.IDLE)
    val tracksListState: StateFlow<ListState> = _tracksListState

    fun getData(id: String) {
        _uiState.value = Loading
        _tracks.value = emptyList()
        _continuation.value = null
        
        viewModelScope.launch {
            YouTube.playlist(id).onSuccess { page ->
                val playlist = page.playlist
                _uiState.value = Success(
                    data = PlaylistState(
                        id = id,
                        title = playlist.title,
                        isRadio = id.startsWith("RD"),
                        author = playlist.author,
                        thumbnail = playlist.thumbnail,
                        description = null,
                        trackCount = playlist.songCountText?.filter { it.isDigit() }?.toIntOrNull(),
                        shuffleEndpoint = playlist.shuffleEndpoint,
                        radioEndpoint = playlist.radioEndpoint
                    )
                )
                _tracks.value = page.songs
                _continuation.value = page.songsContinuation
                if (page.songsContinuation == null) _tracksListState.value = ListState.PAGINATION_EXHAUST
                
                // Get local entity if exists
                database.playlistByBrowseId(id).collectLatest { entity ->
                    _playlistEntity.value = entity?.playlist
                }
            }.onFailure {
                _uiState.value = Error(it.message ?: "Error")
            }
        }
    }

    fun getContinuationTrack(
        playlistId: String,
        continuation: String?,
    ) {
        viewModelScope.launch {
            if (continuation.isNullOrEmpty()) {
                _tracksListState.value = ListState.PAGINATION_EXHAUST
                return@launch
            }
            _tracksListState.value = ListState.PAGINATING
            YouTube.playlistContinuation(continuation).onSuccess { page ->
                _tracks.update { it + page.songs }
                _continuation.value = page.continuation
                if (page.continuation == null) {
                    _tracksListState.value = ListState.PAGINATION_EXHAUST
                } else {
                    _tracksListState.value = ListState.IDLE
                }
            }.onFailure {
                _tracksListState.value = ListState.ERROR
            }
        }
    }

    fun setBrush(listColors: List<Color>) {
        _listColors.value = listColors
    }

    fun onUIEvent(event: PlaylistUIEvent) {
        val data = uiState.value.data ?: return
        when (event) {
            is PlaylistUIEvent.ItemClick -> {
                val videoId = event.videoId
                val loadedList = tracks.value
                val clickedSong = loadedList.first { it.id == videoId }
                val index = loadedList.indexOf(clickedSong)
                setQueueData(
                    QueueData.Data(
                        listTracks = loadedList,
                        firstPlayedTrack = clickedSong,
                        playlistId = data.id,
                        playlistName = "${getString(com.metrolist.music.R.string.playlist)} \"${data.title}\"",
                        playlistType = PlaylistType.PLAYLIST,
                        continuation = continuation.value,
                    ),
                )
                loadMediaItem(clickedSong, "PLAYLIST_CLICK", index)
            }

            PlaylistUIEvent.PlayAll -> {
                val loadedList = tracks.value
                if (loadedList.isEmpty()) {
                    makeToast(getString(com.metrolist.music.R.string.playlist_is_empty))
                    return
                }
                val clickedSong = loadedList.first()
                setQueueData(
                    QueueData.Data(
                        listTracks = loadedList,
                        firstPlayedTrack = clickedSong,
                        playlistId = data.id,
                        playlistName = "${getString(com.metrolist.music.R.string.playlist)} \"${data.title}\"",
                        playlistType = PlaylistType.PLAYLIST,
                        continuation = continuation.value,
                    ),
                )
                loadMediaItem(clickedSong, "PLAYLIST_CLICK", 0)
            }

            PlaylistUIEvent.Shuffle -> {
                val shuffleEndpoint = data.shuffleEndpoint
                if (shuffleEndpoint == null) {
                    makeToast(getString(com.metrolist.music.R.string.shuffle_not_available))
                    return
                }
                viewModelScope.launch {
                    YouTube.next(com.metrolist.innertube.models.WatchEndpoint(videoId = shuffleEndpoint.videoId, playlistId = shuffleEndpoint.playlistId, params = shuffleEndpoint.params)).onSuccess { next ->
                        val nextTracks = next.items.filterIsInstance<SongItem>()
                        if (nextTracks.isNotEmpty()) {
                            setQueueData(
                                QueueData.Data(
                                    listTracks = nextTracks,
                                    firstPlayedTrack = nextTracks.first(),
                                    playlistId = shuffleEndpoint.playlistId,
                                    playlistName = "\"${data.title}\" ${getString(com.metrolist.music.R.string.shuffle)}",
                                    playlistType = PlaylistType.RADIO,
                                    continuation = next.continuation,
                                ),
                            )
                            loadMediaItem(nextTracks.first(), "RADIO_CLICK", 0)
                        }
                    }
                }
            }

            PlaylistUIEvent.StartRadio -> {
                val radioEndpoint = data.radioEndpoint
                if (radioEndpoint == null) {
                    makeToast(getString(com.metrolist.music.R.string.radio_not_available))
                    return
                }
                viewModelScope.launch {
                    YouTube.next(com.metrolist.innertube.models.WatchEndpoint(videoId = radioEndpoint.videoId, playlistId = radioEndpoint.playlistId, params = radioEndpoint.params)).onSuccess { next ->
                        val nextTracks = next.items.filterIsInstance<SongItem>()
                        if (nextTracks.isNotEmpty()) {
                            setQueueData(
                                QueueData.Data(
                                    listTracks = nextTracks,
                                    firstPlayedTrack = nextTracks.first(),
                                    playlistId = radioEndpoint.playlistId,
                                    playlistName = "\"${data.title}\" ${getString(com.metrolist.music.R.string.radio)}",
                                    playlistType = PlaylistType.RADIO,
                                    continuation = next.continuation,
                                ),
                            )
                            loadMediaItem(nextTracks.first(), "RADIO_CLICK", 0)
                        }
                    }
                }
            }

            PlaylistUIEvent.Download -> {
                downloadFullPlaylist()
            }

            PlaylistUIEvent.Favorite -> {
                viewModelScope.launch {
                    val isLiked = liked.value
                    database.transaction {
                        val entity = null // playlistByBrowseId(data.id).firstOrNull()
                        if (entity != null) {
                            // update(entity.copy(bookmarkedAt = if (isLiked) null else LocalDateTime.now()))
                            // TODO: copy not available
                        } else {
                            insert(PlaylistEntity(
                                name = data.title,
                                browseId = data.id,
                                bookmarkedAt = LocalDateTime.now()
                            ))
                        }
                    }
                }
            }
        }
    }

    fun downloadFullPlaylist() {
        viewModelScope.launch {
            val loadedTracks = tracks.value
            if (loadedTracks.isEmpty()) {
                makeToast(getString(com.metrolist.music.R.string.playlist_is_empty))
                return@launch
            }
            makeToast(getString(com.metrolist.music.R.string.downloading))
            loadedTracks.forEach { song ->
                DownloadService.sendAddDownload(
                    context,
                    ExoDownloadService::class.java,
                    DownloadRequest.Builder(song.id, song.id.toUri()).build(),
                    false
                )
            }
        }
    }

    fun saveToLocal(tracks: List<SongItem>) {
        // Implementation for saving to local playlist in Metrolist
    }

    fun updatePlaylistTitle(
        title: String,
        id: String,
    ) {
        // YouTube API for updating playlist title is not yet available in Metrolist's innertube
    }
}

    fun getFullTracks(onDone: (List<com.metrolist.innertube.models.SongItem>) -> Unit) {
        viewModelScope.launch {
            val tracks = (_uiState.value as? PlaylistUIState.Success)?.data?.listTracks ?: emptyList()
            onDone(tracks)
        }
    }

    fun saveToLocal(tracks: List<com.metrolist.innertube.models.SongItem>) {
        // Stub — save to local playlist
    }
}

sealed class PlaylistUIState(
    val data: PlaylistState? = null,
    val message: String? = null,
) {
    data object Loading : PlaylistUIState()

    class Success(
        data: PlaylistState,
    ) : PlaylistUIState(data = data)

    class Error(
        message: String? = null,
    ) : PlaylistUIState(message = message)
}

sealed class PlaylistUIEvent {
    data object PlayAll : PlaylistUIEvent()
    data object Shuffle : PlaylistUIEvent()
    data object StartRadio : PlaylistUIEvent()
    data class ItemClick(val videoId: String) : PlaylistUIEvent()
    data object Favorite : PlaylistUIEvent()
    data object Download : PlaylistUIEvent()
}

enum class ListState {
    IDLE, LOADING, PAGINATING, ERROR, PAGINATION_EXHAUST
}
