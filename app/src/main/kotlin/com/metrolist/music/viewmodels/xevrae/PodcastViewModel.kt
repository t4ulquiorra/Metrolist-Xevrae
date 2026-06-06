package com.metrolist.music.viewmodels.xevrae

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.metrolist.music.common.Config
import com.metrolist.music.db.MusicDatabase
import com.metrolist.music.db.entities.PodcastEntity
import com.metrolist.music.db.entities.SongEntity
import com.metrolist.music.models.xevrae.*
import com.metrolist.music.playback.PlayerConnection
import com.metrolist.music.playback.PlayerConnectionProvider
import com.metrolist.music.utils.shareUrl
import com.metrolist.music.viewmodels.xevrae.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.metrolist.innertube.YouTube
import com.metrolist.music.models.toMediaMetadata
import java.time.LocalDateTime

// UI state cho podcast
sealed class PodcastUIState {
    object Loading : PodcastUIState()

    data class Success(
        val id: String,
        val data: PodcastBrowse,
    ) : PodcastUIState()

    data class Error(
        val message: String,
    ) : PodcastUIState()
}

sealed class PodcastUIEvent {
    data class PlayAll(
        val podcastId: String,
    ) : PodcastUIEvent()

    data class Shuffle(
        val podcastId: String,
    ) : PodcastUIEvent()

    data class EpisodeClick(
        val videoId: String,
        val podcastId: String,
    ) : PodcastUIEvent()

    data class ToggleFavorite(
        val podcastId: String,
        val isFavorite: Boolean,
    ) : PodcastUIEvent()

    data class Share(
        val podcastId: String,
    ) : PodcastUIEvent()
}

@HiltViewModel
class PodcastViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val database: MusicDatabase,
    playerConnectionProvider: PlayerConnectionProvider,
) : BaseViewModel(context, playerConnectionProvider) {
    private val _uiState = MutableStateFlow<PodcastUIState>(PodcastUIState.Loading)
    val uiState: StateFlow<PodcastUIState> = _uiState.asStateFlow()

    private val _podcastEntity = MutableStateFlow<PodcastsEntity?>(null)
    val podcastEntity: StateFlow<PodcastsEntity?> = _podcastEntity.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    fun clearPodcastBrowse() {
        _uiState.value = PodcastUIState.Loading
        _podcastEntity.value = null
    }

    fun getPodcastBrowse(id: String) {
        _isFavorite.value = false
        _uiState.value = PodcastUIState.Loading
        viewModelScope.launch {
            // Check local database first
            database.podcast(id).collectLatest { entity ->
                if (entity != null) {
                    _podcastEntity.value = PodcastsEntity(
                        podcastId = entity.id,
                        title = entity.title,
                        authorId = entity.channelId ?: "",
                        authorName = entity.author ?: "",
                        authorThumbnail = null,
                        description = null,
                        thumbnail = entity.thumbnailUrl,
                        listEpisodes = emptyList(),
                        isFavorite = entity.inLibrary
                    )
                    _isFavorite.value = entity.inLibrary
                }

                // Fetch from YouTube
                YouTube.podcast(id).onSuccess { page ->
                    val podcastBrowse = PodcastBrowse(
                        title = page.podcast.title,
                        author = Artist(page.podcast.author?.name ?: "", page.podcast.author?.id ?: ""),
                        authorThumbnail = null,
                        thumbnail = listOf(Thumbnail(page.podcast.thumbnail ?: "")),
                        description = null,
                        listEpisode = page.episodes.map {
                            PodcastBrowse.EpisodeItem(
                                title = it.title,
                                author = Artist(it.author?.name ?: "", it.author?.id ?: ""),
                                description = null,
                                thumbnail = listOf(Thumbnail(it.thumbnail)),
                                createdDay = it.publishDateText,
                                durationString = it.duration?.let { d -> "${d / 60}:${d % 60}" },
                                videoId = it.id
                            )
                        }
                    )
                    _uiState.value = PodcastUIState.Success(id, podcastBrowse)
                    
                    // Update database
                    savePodcastToDatabase(id, podcastBrowse)
                }.onFailure {
                    if (_podcastEntity.value == null) {
                        _uiState.value = PodcastUIState.Error(it.message ?: "Unknown error")
                    }
                }
            }
        }
    }

    private fun savePodcastToDatabase(
        id: String,
        podcastBrowse: PodcastBrowse,
    ) {
        viewModelScope.launch {
            val entity = PodcastEntity(
                id = id,
                title = podcastBrowse.title,
                author = podcastBrowse.author.name,
                thumbnailUrl = podcastBrowse.thumbnail.firstOrNull()?.url,
                channelId = podcastBrowse.author.id,
                bookmarkedAt = if (_isFavorite.value) LocalDateTime.now() else null
            )
            database.upsert(entity)
            
            // Save episodes as songs with isEpisode = true
            podcastBrowse.listEpisode.forEach { episode ->
                val song = SongEntity(
                    id = episode.videoId,
                    title = episode.title,
                    thumbnailUrl = episode.thumbnail.firstOrNull()?.url,
                    albumId = id,
                    albumName = podcastBrowse.title,
                    isEpisode = true
                )
                database.insert(song)
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            _podcastEntity.value?.let { podcast ->
                val newFavoriteState = !podcast.isFavorite
                _isFavorite.value = newFavoriteState

                YouTube.savePodcast(podcast.podcastId, newFavoriteState).onSuccess {
                    database.podcast(podcast.podcastId).collectLatest { entity ->
                        entity?.let {
                            database.upsert(it.copy(bookmarkedAt = if (newFavoriteState) LocalDateTime.now() else null))
                        }
                    }
                    _podcastEntity.update { it?.copy(isFavorite = newFavoriteState) }
                }
            }
        }
    }

    fun onUIEvent(event: PodcastUIEvent) {
        val currentState = _uiState.value
        if (currentState !is PodcastUIState.Success) return

        val podcastData = currentState.data
        when (event) {
            is PodcastUIEvent.PlayAll -> {
                viewModelScope.launch {
                    val metadataList = podcastData.listEpisode.map { episode ->
                        database.getSongByIdBlocking(episode.videoId)?.toMediaMetadata()
                    }.filterNotNull()
                    playerConnection.play()
                }
            }

            is PodcastUIEvent.Shuffle -> {
                viewModelScope.launch {
                    val metadataList = podcastData.listEpisode.shuffled().map { episode ->
                        database.getSongByIdBlocking(episode.videoId)?.toMediaMetadata()
                    }.filterNotNull()
                    playerConnection.play()
                    playerConnection.player.shuffleModeEnabled = true
                }
            }

            is PodcastUIEvent.EpisodeClick -> {
                viewModelScope.launch {
                    val metadataList = podcastData.listEpisode.map { episode ->
                        database.getSongByIdBlocking(episode.videoId)?.toMediaMetadata()
                    }.filterNotNull()
                    val index = metadataList.indexOfFirst { it.id == event.videoId }.coerceAtLeast(0)
                    playerConnection.play()
                }
            }

            is PodcastUIEvent.ToggleFavorite -> {
                toggleFavorite()
            }

            is PodcastUIEvent.Share -> {
                val url = "https://music.youtube.com/playlist?list=${event.podcastId.removePrefix("MPSP")}"
                shareUrl(
                    context = context,
                    title = getString(com.metrolist.music.R.string.share_url),
                    url = url,
                )
            }
        }
    }
}
