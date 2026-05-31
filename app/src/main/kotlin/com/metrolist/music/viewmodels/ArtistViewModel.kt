/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 * Merged with Xevrae UI
 */

package com.metrolist.music.viewmodels

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.metrolist.innertube.YouTube
import com.metrolist.innertube.models.AlbumItem
import com.metrolist.innertube.models.PlaylistItem
import com.metrolist.innertube.models.SongItem
import com.metrolist.innertube.models.WatchEndpoint
import com.metrolist.music.db.MusicDatabase
import com.metrolist.music.db.entities.ArtistEntity
import com.metrolist.music.db.entities.SongEntity
import com.metrolist.music.models.xevrae.Albums
import com.metrolist.music.models.xevrae.Related
import com.metrolist.music.models.xevrae.Singles
import com.metrolist.music.models.xevrae.Videos
import com.metrolist.music.domain.mediaservice.handler.PlaylistType
import com.metrolist.music.domain.mediaservice.handler.QueueData
import com.metrolist.music.utils.SyncUtils
import com.metrolist.music.viewmodels.xevrae.ArtistScreenState
import com.metrolist.music.viewmodels.xevrae.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class ArtistViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val database: MusicDatabase,
    private val syncUtils: SyncUtils,
    private val savedStateHandle: SavedStateHandle,
) : BaseViewModel(context) {

    private val artistIdArg = savedStateHandle.get<String>("artistId") ?: savedStateHandle.get<String>("channelId") ?: ""
    private val isPodcastChannelArg = savedStateHandle.get<Boolean>("isPodcastChannel") ?: false

    private var _canvasUrl: MutableStateFlow<Pair<String, SongEntity>?> = MutableStateFlow(null)
    var canvasUrl: StateFlow<Pair<String, SongEntity>?> = _canvasUrl

    private var _followed: MutableStateFlow<Boolean> = MutableStateFlow(false)
    var followed: StateFlow<Boolean> = _followed

    private val _artistScreenState: MutableStateFlow<ArtistScreenState> = MutableStateFlow(ArtistScreenState.Loading)
    val artistScreenState: StateFlow<ArtistScreenState> = _artistScreenState

    init {
        if (artistIdArg.isNotEmpty()) {
            browseArtist(artistIdArg)
        }
    }

    fun browseArtist(channelId: String) {
        if (channelId.isEmpty()) return
        
        _artistScreenState.value = ArtistScreenState.Loading
        _canvasUrl.value = null
        
        viewModelScope.launch {
            // Observe local followed state
            database.artist(channelId).collectLatest { artist ->
                _followed.value = artist?.artist?.bookmarkedAt != null
            }
        }
        
        viewModelScope.launch {
            YouTube.artist(channelId).onSuccess { page ->
                val artist = page.artist
                
                // Map sections
                var popularSongs = emptyList<SongItem>()
                var albums: Albums? = null
                var singles: Singles? = null
                var related: Related? = null
                var videos: Videos? = null
                var featuredOn = emptyList<PlaylistItem>()

                page.sections.forEach { section ->
                    when {
                        section.title.contains("Songs", true) || section.title.contains("Popular", true) -> {
                            popularSongs = section.items.filterIsInstance<SongItem>()
                        }
                        section.title.contains("Albums", true) -> {
                            albums = Albums(section.items.filterIsInstance<AlbumItem>(), section.moreEndpoint?.params)
                        }
                        section.title.contains("Singles", true) -> {
                            singles = Singles(section.items.filterIsInstance<AlbumItem>(), section.moreEndpoint?.params)
                        }
                        section.title.contains("Related", true) || section.title.contains("Fans", true) -> {
                            related = Related(section.items)
                        }
                        section.title.contains("Videos", true) -> {
                            videos = Videos(section.items.filterIsInstance<SongItem>(), section.moreEndpoint?.params)
                        }
                        section.title.contains("Featured", true) -> {
                            featuredOn = section.items.filterIsInstance<PlaylistItem>()
                        }
                    }
                }

                val screenData = ArtistScreenData(
                    title = artist.title,
                    imageUrl = artist.thumbnail,
                    subscribers = page.subscriberCountText,
                    playCount = page.monthlyListenerCount,
                    isChannel = true,
                    channelId = channelId,
                    radioParam = artist.radioEndpoint,
                    shuffleParam = artist.shuffleEndpoint,
                    description = page.description,
                    popularSongs = popularSongs,
                    singles = singles,
                    albums = albums,
                    video = videos,
                    related = related,
                    featuredOn = featuredOn
                )
                
                _artistScreenState.value = ArtistScreenState.Success(screenData)
                
                // Sync with database
                database.transaction {
                    val existing = artist(channelId).firstOrNull()
                    if (existing == null) {
                        insert(ArtistEntity(
                            id = channelId,
                            name = artist.title,
                            thumbnailUrl = artist.thumbnail,
                            channelId = channelId,
                            isPodcastChannel = isPodcastChannelArg
                        ))
                    }
                }
            }.onFailure { res ->
                _artistScreenState.value = ArtistScreenState.Error(res.message ?: "Error")
            }
        }
    }

    fun updateFollowed(
        followed: Int,
        channelId: String,
    ) {
        val shouldFollow = (followed == 1)
        _followed.value = shouldFollow
        
        viewModelScope.launch(Dispatchers.IO) {
            database.transaction {
                val artist = artist(channelId).firstOrNull()
                if (artist != null) {
                    val newBookmark = if (shouldFollow) LocalDateTime.now() else null
                    update(artist.artist.copy(bookmarkedAt = newBookmark))
                } else if (shouldFollow) {
                    val screenData = artistScreenState.value.data
                    insert(ArtistEntity(
                        id = channelId,
                        name = screenData.title ?: "",
                        channelId = channelId,
                        thumbnailUrl = screenData.imageUrl,
                        bookmarkedAt = LocalDateTime.now(),
                        isPodcastChannel = isPodcastChannelArg
                    ))
                }
            }
            
            // Sync with YouTube using Metrolist's SyncUtils
            syncUtils.subscribeChannel(channelId, shouldFollow)
        }
    }

    fun onRadioClick(endpoint: WatchEndpoint) {
        viewModelScope.launch {
            YouTube.next(endpoint.videoId, endpoint.playlistId, endpoint.params).onSuccess { next ->
                val tracks = next.items.filterIsInstance<SongItem>()
                if (tracks.isNotEmpty()) {
                    setQueueData(
                        QueueData.Data(
                            listTracks = tracks,
                            firstPlayedTrack = tracks.first(),
                            playlistId = endpoint.playlistId,
                            playlistName = "\"${artistScreenState.value.data.title}\" ${getString(com.metrolist.music.R.string.radio)}",
                            playlistType = PlaylistType.RADIO,
                            continuation = next.continuation,
                        ),
                    )
                    loadMediaItem(
                        tracks.first(),
                        "PLAYLIST_CLICK",
                        0,
                    )
                }
            }.onFailure {
                makeToast(it.message)
            }
        }
    }

    fun onShuffleClick(endpoint: WatchEndpoint) {
        viewModelScope.launch {
            YouTube.next(endpoint.videoId, endpoint.playlistId, endpoint.params).onSuccess { next ->
                val tracks = next.items.filterIsInstance<SongItem>()
                if (tracks.isNotEmpty()) {
                    setQueueData(
                        QueueData.Data(
                            listTracks = tracks,
                            firstPlayedTrack = tracks.first(),
                            playlistId = endpoint.playlistId,
                            playlistName = "\"${artistScreenState.value.data.title}\" ${getString(com.metrolist.music.R.string.shuffle)}",
                            playlistType = PlaylistType.RADIO,
                            continuation = next.continuation,
                        ),
                    )
                    loadMediaItem(
                        tracks.first(),
                        "PLAYLIST_CLICK",
                        0,
                    )
                }
            }.onFailure {
                makeToast(it.message)
            }
        }
    }
}

data class ArtistScreenData(
    val title: String? = null,
    val imageUrl: String? = null,
    val subscribers: String? = null,
    val playCount: String? = null,
    val isChannel: Boolean = false,
    val channelId: String? = null,
    val radioParam: WatchEndpoint? = null,
    val shuffleParam: WatchEndpoint? = null,
    val description: String? = null,
    val listSongParam: String? = null,
    val popularSongs: List<SongItem> = emptyList(),
    val singles: Singles? = null,
    val albums: Albums? = null,
    val video: Videos? = null,
    val related: Related? = null,
    val featuredOn: List<PlaylistItem> = emptyList(),
)

sealed class ArtistScreenState(
    val data: ArtistScreenData = ArtistScreenData(),
    val message: String? = null,
) {
    data object Loading : ArtistScreenState()

    class Success(
        data: ArtistScreenData,
    ) : ArtistScreenState(data)

    class Error(
        message: String,
    ) : ArtistScreenState(message = message)
}
