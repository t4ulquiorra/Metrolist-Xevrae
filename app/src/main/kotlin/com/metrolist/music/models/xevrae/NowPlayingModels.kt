package com.metrolist.music.models.xevrae

import com.metrolist.music.db.entities.SongEntity

object DownloadState {
    const val STATE_NOT_DOWNLOADED = 0
    const val STATE_PREPARING = 1
    const val STATE_DOWNLOADING = 2
    const val STATE_DOWNLOADED = 3
}

data class LocalPlaylistEntity(
    val id: Long,
    val title: String,
    val thumbnail: String? = null,
    val tracks: List<String>? = null,
)

data class PlaylistsResult(
    val browseId: String,
    val title: String,
    val thumbnails: List<Thumbnail>? = null,
)

data class Artist(
    val name: String,
    val id: String,
)

data class Album(
    val name: String,
    val id: String,
)

data class SleepTimerState(
    val isActive: Boolean,
    val timeRemaining: Int,
)

data class Thumbnail(
    val url: String,
)

data class PodcastBrowse(
    val title: String,
    val author: Artist,
    val authorThumbnail: String?,
    val thumbnail: List<Thumbnail>,
    val description: String?,
    val listEpisode: List<EpisodeItem>,
) {
    data class EpisodeItem(
        val title: String,
        val author: Artist,
        val description: String?,
        val thumbnail: List<Thumbnail>,
        val createdDay: String?,
        val durationString: String?,
        val videoId: String,
    )
}

data class PodcastsEntity(
    val podcastId: String,
    val title: String,
    val authorId: String,
    val authorName: String,
    val authorThumbnail: String?,
    val description: String?,
    val thumbnail: String?,
    val listEpisodes: List<String>?,
    val isFavorite: Boolean = false,
)

data class EpisodeEntity(
    val videoId: String,
    val podcastId: String,
    val title: String,
    val authorId: String,
    val authorName: String,
    val description: String?,
    val createdDay: String?,
    val durationString: String?,
    val thumbnail: String?,
)
