package com.metrolist.music.models.xevrae

import com.metrolist.innertube.models.YTItem
import kotlinx.serialization.Serializable

@Serializable
data class HomeItem(
    val contents: List<Content>,
    val title: String,
    val subtitle: String? = null,
    val thumbnail: List<Thumbnail>? = null,
    val channelId: String? = null,
)

@Serializable
data class Content(
    val album: Album? = null,
    val artists: List<Artist>? = null,
    val description: String? = null,
    val isExplicit: Boolean? = null,
    val playlistId: String? = null,
    val browseId: String? = null,
    val thumbnails: List<Thumbnail>,
    val title: String,
    val videoId: String? = null,
    val views: String? = null,
    val durationSeconds: Int? = null,
    val radio: String? = null,
)

@Serializable
data class Mood(
    val genres: List<Genre>,
    val moodsMoments: List<MoodsMoment>,
)

@Serializable
data class Genre(
    val params: String,
    val title: String,
)

@Serializable
data class MoodsMoment(
    val params: String,
    val title: String,
)

@Serializable
data class Chart(
    val artists: List<ArtistItemCompat>,
    val countries: List<CountryCompat>? = null,
    val listChartItem: List<ChartItemPlaylist>,
)

@Serializable
data class ArtistItemCompat(
    val name: String,
    val id: String,
    val thumbnails: List<Thumbnail>,
)

@Serializable
data class CountryCompat(
    val name: String,
    val code: String,
)

@Serializable
data class ChartItemPlaylist(
    val title: String,
    val playlists: List<PlaylistCompat>,
)

@Serializable
data class PlaylistCompat(
    val title: String,
    val playlistId: String,
    val thumbnails: List<Thumbnail>,
    val description: String? = null,
)
