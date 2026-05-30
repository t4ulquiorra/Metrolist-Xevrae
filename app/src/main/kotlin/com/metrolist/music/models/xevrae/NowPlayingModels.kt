package com.metrolist.music.models.xevrae

import com.metrolist.music.db.entities.SongEntity

data class SleepTimerState(
    val isActive: Boolean,
    val timeRemaining: Int,
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
