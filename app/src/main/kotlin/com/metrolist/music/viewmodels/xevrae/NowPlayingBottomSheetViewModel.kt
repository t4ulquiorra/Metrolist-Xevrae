package com.metrolist.music.viewmodels.xevrae

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.viewModelScope
import com.metrolist.music.common.Config
import com.metrolist.music.constants.AudioQuality
import com.metrolist.music.constants.AudioQualityKey
import com.metrolist.music.constants.PreferredLyricsProvider
import com.metrolist.music.constants.PreferredLyricsProviderKey
import com.metrolist.music.db.MusicDatabase
import com.metrolist.music.db.entities.PlaylistEntity
import com.metrolist.music.db.entities.PlaylistSongMap
import com.metrolist.music.db.entities.SongEntity
import com.metrolist.music.models.xevrae.*
import com.metrolist.music.playback.DownloadUtil
import com.metrolist.music.playback.ExoDownloadService
import com.metrolist.music.playback.PlayerConnection
import com.metrolist.music.utils.dataStore
import com.metrolist.music.extensions.toMediaItem
import com.metrolist.music.utils.enumPreference
import com.metrolist.music.utils.shareUrl
import com.metrolist.music.viewmodels.xevrae.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.core.net.toUri
import androidx.media3.common.PlaybackParameters
import com.metrolist.innertube.YouTube
import com.metrolist.music.models.toMediaMetadata
import timber.log.Timber

@HiltViewModel
class NowPlayingBottomSheetViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val database: MusicDatabase,
    private val downloadUtil: DownloadUtil,
) : BaseViewModel(context) {
    private val _uiState: MutableStateFlow<NowPlayingBottomSheetUIState> =
        MutableStateFlow(
            NowPlayingBottomSheetUIState(
                listLocalPlaylist = emptyList(),
                listYouTubePlaylist = emptyList(),
                mainLyricsProvider = PreferredLyricsProvider.BETTER_LYRICS.name,
                sleepTimer =
                    SleepTimerState(
                        false,
                        0,
                    ),
            ),
        )
    val uiState: StateFlow<NowPlayingBottomSheetUIState> get() = _uiState.asStateFlow()

    private var getSongAsFlow: Job? = null

    init {
        viewModelScope.launch {
            launch {
                playerConnection.sleepTimerTimeRemaining.collectLatest { time ->
                    _uiState.update { 
                        it.copy(sleepTimer = SleepTimerState(time > 0, time.toInt() / 60 / 1000)) 
                    }
                }
            }
            launch {
                database.playlists().collectLatest { list ->
                    _uiState.update { state ->
                        state.copy(
                            listLocalPlaylist = list.map { 
                                LocalPlaylistEntity(
                                    id = it.playlist.id.removePrefix("LP").toLongOrNull() ?: 0L,
                                    title = it.playlist.name,
                                    thumbnail = it.songPreview.firstOrNull()?.song?.thumbnailUrl
                                )
                            }
                        )
                    }
                }
            }
            launch {
                // In Metrolist we don't have a direct "library playlist" repo like Xevrae
                // We'll use YouTube.libraryPlaylists()
                runCatching {
                    YouTube.libraryPlaylists()
                }.onSuccess { playlists ->
                    _uiState.update { state ->
                        state.copy(
                            listYouTubePlaylist = playlists?.map {
                                PlaylistsResult(
                                    browseId = it.id,
                                    title = it.name,
                                    thumbnails = it.thumbnails?.map { t -> Thumbnail(t.url) }
                                )
                            } ?: emptyList()
                        )
                    }
                }
            }
            launch {
                context.dataStore.data.map { it[PreferredLyricsProviderKey] }.collectLatest { lyricsProvider ->
                    _uiState.update { it.copy(mainLyricsProvider = lyricsProvider ?: PreferredLyricsProvider.BETTER_LYRICS.name) }
                }
            }
        }
    }

    fun resetPlaylists() {
        // Playlists are tracked via flows in init, so we don't need manual reset
        // unless we want to force refresh YouTube playlists
        viewModelScope.launch {
             runCatching {
                YouTube.libraryPlaylists()
            }.onSuccess { playlists ->
                _uiState.update { state ->
                    state.copy(
                        listYouTubePlaylist = playlists?.map {
                            PlaylistsResult(
                                browseId = it.id,
                                title = it.name,
                                thumbnails = it.thumbnails?.map { t -> Thumbnail(t.url) }
                            )
                        } ?: emptyList()
                    )
                }
            }
        }
    }

    fun setSongEntity(songEntity: SongEntity?) {
        val songOrNowPlaying = songEntity ?: (playerConnection.mediaMetadata.value?.let { 
            SongEntity(
                id = it.id,
                title = it.title,
                thumbnailUrl = it.thumbnailUrl,
                albumId = it.albumId,
                albumName = it.albumName,
                duration = it.duration
            )
        } ?: return)
        
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    songUIState = state.songUIState.copy(
                        isAddedToYouTubeLiked = false,
                    ),
                )
            }
            
            database.insert(songOrNowPlaying)
            getSongEntityFlow(id = songOrNowPlaying.id)
        }
    }

    private fun getSongEntityFlow(id: String) {
        getSongAsFlow?.cancel()
        if (id.isEmpty()) return
        getSongAsFlow =
            viewModelScope.launch {
                database.song(id).collectLatest { songFull ->
                    val song = songFull?.song
                    if (song != null) {
                        val download = downloadUtil.downloads.value[song.id]
                        _uiState.update { state ->
                            state.copy(
                                songUIState =
                                    NowPlayingBottomSheetUIState.SongUIState(
                                        videoId = song.id,
                                        title = song.title,
                                        listArtists = songFull.artists.map { Artist(it.name, it.id) },
                                        thumbnails = song.thumbnailUrl,
                                        liked = song.liked,
                                        downloadState = when (download?.state) {
                                            Download.STATE_COMPLETED -> DownloadState.STATE_DOWNLOADED
                                            Download.STATE_DOWNLOADING -> DownloadState.STATE_DOWNLOADING
                                            Download.STATE_QUEUED -> DownloadState.STATE_PREPARING
                                            else -> DownloadState.STATE_NOT_DOWNLOADED
                                        },
                                        album = song.albumName?.let { Album(it, song.albumId ?: "") },
                                    ),
                            )
                        }
                    }
                }
            }
    }

    fun onUIEvent(ev: NowPlayingBottomSheetUIEvent) {
        val songUIState = uiState.value.songUIState
        if (songUIState.videoId.isEmpty()) return
        viewModelScope.launch {
            when (ev) {
                is NowPlayingBottomSheetUIEvent.DeleteFromPlaylist -> {
                    val playlistId = "LP${ev.playlistId}"
                    database.delete(PlaylistSongMap(playlistId, songUIState.videoId, 0))
                    makeToast(getString(com.metrolist.music.R.string.delete_song_from_playlist))
                }

                is NowPlayingBottomSheetUIEvent.AddToYouTubePlaylist -> {
                    runCatching {
                        YouTube.addToPlaylist(ev.browseId, songUIState.videoId)
                    }.onSuccess {
                        makeToast(getString(com.metrolist.music.R.string.added_to_youtube_playlist))
                    }.onFailure {
                        makeToast(it.message ?: getString(com.metrolist.music.R.string.error_occurred))
                    }
                }

                is NowPlayingBottomSheetUIEvent.ToggleLike -> {
                    database.song(songUIState.videoId).collectLatest { songFull ->
                        songFull?.song?.let { song ->
                            database.upsert(song.toggleLike())
                        }
                    }
                }

                is NowPlayingBottomSheetUIEvent.Download -> {
                    val download = downloadUtil.downloads.value[songUIState.videoId]
                    if (download?.state == Download.STATE_COMPLETED) {
                        DownloadService.sendRemoveDownload(
                            context,
                            ExoDownloadService::class.java,
                            songUIState.videoId,
                            false
                        )
                        makeToast(getString(com.metrolist.music.R.string.removed_download))
                    } else {
                        val downloadRequest =
                            DownloadRequest.Builder(songUIState.videoId, songUIState.videoId.toUri())
                                .setCustomCacheKey(songUIState.videoId)
                                .setData(songUIState.title.toByteArray())
                                .build()
                        DownloadService.sendAddDownload(
                            context,
                            ExoDownloadService::class.java,
                            downloadRequest,
                            false
                        )
                        makeToast(getString(com.metrolist.music.R.string.downloading))
                    }
                }

                is NowPlayingBottomSheetUIEvent.AddToPlaylist -> {
                    val playlistId = "LP${ev.playlistId}"
                    database.addSongToPlaylist(playlistId, songUIState.videoId)
                    makeToast(getString(com.metrolist.music.R.string.added_to_playlist))
                }

                is NowPlayingBottomSheetUIEvent.PlayNext -> {
                    database.song(songUIState.videoId).collectLatest { songFull ->
                        songFull?.let {
                            playerConnection.playNext(it.toMediaItem())
                            makeToast(getString(com.metrolist.music.R.string.play_next))
                        }
                    }
                }

                is NowPlayingBottomSheetUIEvent.AddToQueue -> {
                    database.song(songUIState.videoId).collectLatest { songFull ->
                        songFull?.let {
                            playerConnection.addToQueue(it.toMediaItem())
                            makeToast(getString(com.metrolist.music.R.string.added_to_queue))
                        }
                    }
                }

                is NowPlayingBottomSheetUIEvent.ChangeLyricsProvider -> {
                    context.dataStore.edit { settings ->
                        settings[PreferredLyricsProviderKey] = ev.lyricsProvider
                    }
                }

                is NowPlayingBottomSheetUIEvent.SetSleepTimer -> {
                    if (ev.cancel) {
                        playerConnection.stopSleepTimer()
                        makeToast(getString(com.metrolist.music.R.string.sleep_timer_off_done))
                    } else if (ev.minutes > 0) {
                        playerConnection.startSleepTimer(ev.minutes * 60 * 1000L)
                    }
                }

                is NowPlayingBottomSheetUIEvent.ChangePlaybackSpeedPitch -> {
                    playerConnection.player.playbackParameters = PlaybackParameters(ev.speed, ev.pitch.toFloat() / 100f)
                }

                is NowPlayingBottomSheetUIEvent.Share -> {
                    val url = "https://music.youtube.com/watch?v=${songUIState.videoId}"
                    shareUrl(
                        context = context,
                        title = getString(com.metrolist.music.R.string.share_url),
                        url = url,
                    )
                }

                /*
                is NowPlayingBottomSheetUIEvent.StartRadio -> {
                    YouTube.next(
                        com.metrolist.innertube.models.WatchEndpoint(
                            videoId = ev.videoId,
                            playlistId = "RDAMVM${ev.videoId}"
                        )
                    ).onSuccess { next ->
                        val items = next.items.filterIsInstance<com.metrolist.innertube.models.SongItem>()
                        if (items.isNotEmpty()) {
                            playerConnection.playQueue(
                                com.metrolist.music.playback.queues.ListQueue(
                                    title = "Radio",
                                    items = items.map { it.toMediaItem() }
                                )
                            )
                        }
                    }
                }
                */
            }
        }
    }
}

data class NowPlayingBottomSheetUIState(
    val songUIState: SongUIState = SongUIState(),
    val listLocalPlaylist: List<LocalPlaylistEntity>,
    val listYouTubePlaylist: List<PlaylistsResult>,
    val mainLyricsProvider: String,
    val sleepTimer: SleepTimerState,
) {
    data class SongUIState(
        val videoId: String = "",
        val title: String = "",
        val listArtists: List<Artist> = emptyList(),
        val thumbnails: String? = null,
        val liked: Boolean = false,
        val isAddedToYouTubeLiked: Boolean = false,
        val downloadState: Int = DownloadState.STATE_NOT_DOWNLOADED,
        val album: Album? = null,
    )
}

sealed class NowPlayingBottomSheetUIEvent {
    data class DeleteFromPlaylist(
        val videoId: String,
        val playlistId: Long,
    ) : NowPlayingBottomSheetUIEvent()

    data object ToggleLike : NowPlayingBottomSheetUIEvent()

    data object Download : NowPlayingBottomSheetUIEvent()

    data class AddToPlaylist(
        val playlistId: Long,
    ) : NowPlayingBottomSheetUIEvent()

    data class AddToYouTubePlaylist(
        val browseId: String,
    ) : NowPlayingBottomSheetUIEvent()

    data object PlayNext : NowPlayingBottomSheetUIEvent()

    data object AddToQueue : NowPlayingBottomSheetUIEvent()

    data class ChangeLyricsProvider(
        val lyricsProvider: String,
    ) : NowPlayingBottomSheetUIEvent()

    data class SetSleepTimer(
        val cancel: Boolean = false,
        val minutes: Int = 0,
    ) : NowPlayingBottomSheetUIEvent()

    data class ChangePlaybackSpeedPitch(
        val speed: Float,
        val pitch: Int,
    ) : NowPlayingBottomSheetUIEvent()

    data class StartRadio(
        val videoId: String,
        val name: String,
    ) : NowPlayingBottomSheetUIEvent()

    data object Share : NowPlayingBottomSheetUIEvent()
}
  data object Share : NowPlayingBottomSheetUIEvent()
}
