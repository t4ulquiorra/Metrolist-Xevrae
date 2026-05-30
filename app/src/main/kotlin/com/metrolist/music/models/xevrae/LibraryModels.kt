package com.metrolist.music.models.xevrae

import com.metrolist.music.db.entities.AlbumEntity
import com.metrolist.music.db.entities.PlaylistEntity
import com.metrolist.music.db.entities.SongEntity
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

interface LibraryType

interface PlaylistType : LibraryType {
    enum class Type {
        YOUTUBE_PLAYLIST,
        RADIO,
        LOCAL,
        ALBUM,
        PODCAST,
    }

    fun playlistType(): Type
}

interface RecentlyType : LibraryType {
    enum class Type {
        SONG,
        ALBUM,
        ARTIST,
        PLAYLIST,
    }

    fun objectType(): Type
}

sealed interface SearchResultType {
    enum class Type {
        SONG,
        VIDEO,
        PLAYLIST,
        ALBUM,
        ARTIST,
        PODCAST,
    }

    fun objectType(): Type
}

@Serializable
data class SongsResult(
    val album: Album?,
    val artists: List<Artist>?,
    val category: String? = null,
    val duration: String? = null,
    val durationSeconds: Int? = null,
    val feedbackTokens: FeedbackTokens? = null,
    val isExplicit: Boolean? = false,
    val resultType: String? = null,
    val thumbnails: List<Thumbnail>? = null,
    val title: String? = null,
    val videoId: String,
    val videoType: String? = null,
    val year: Any? = null,
) : SearchResultType {
    override fun objectType(): SearchResultType.Type = SearchResultType.Type.SONG
}

@Serializable
data class VideosResult(
    val artists: List<Artist>?,
    val category: String? = null,
    val duration: String? = null,
    val durationSeconds: Int? = null,
    val resultType: String? = null,
    val thumbnails: List<Thumbnail>? = null,
    val title: String,
    val videoId: String,
    val videoType: String? = null,
    val views: String? = null,
    val year: Any? = null,
) : SearchResultType {
    override fun objectType(): SearchResultType.Type = SearchResultType.Type.VIDEO
}

@Serializable
data class ArtistsResult(
    val artist: String,
    val browseId: String,
    val category: String? = null,
    val radioId: String? = null,
    val resultType: String? = null,
    val shuffleId: String? = null,
    val thumbnails: List<Thumbnail>? = null,
    val title: String? = null,
) : SearchResultType {
    override fun objectType(): SearchResultType.Type = SearchResultType.Type.ARTIST
}

@Serializable
data class AlbumsResult(
    val browseId: String,
    val thumbnails: List<Thumbnail>? = null,
    val title: String,
    val year: String? = null,
    val playlistId: String? = null,
) : SearchResultType {
    override fun objectType(): SearchResultType.Type = SearchResultType.Type.ALBUM
}

@Serializable
data class PlaylistsResult(
    val browseId: String,
    val title: String,
    val thumbnails: List<Thumbnail>? = null,
    val author: String? = null,
    val trackCount: String? = null,
    val resultType: String? = null,
) : SearchResultType {
    override fun objectType(): SearchResultType.Type = SearchResultType.Type.PLAYLIST
}

@Serializable
data class Artist(
    val name: String,
    val id: String,
)

@Serializable
data class Album(
    val name: String,
    val id: String,
)

@Serializable
data class Thumbnail(
    val url: String,
)

@Serializable
data class FeedbackTokens(
    val add: String? = null,
    val remove: String? = null,
)

data class SearchSuggestions(
    val queries: List<String>,
    val recommendedItems: List<SearchResultType>,
)

data class ChartItem(
    val country: Country,
    val ytPlaylistId: String,
) : PlaylistType {
    override fun playlistType(): PlaylistType.Type = PlaylistType.Type.YOUTUBE_PLAYLIST

    enum class Country {
        GLOBAL,
        VIETNAM,
        ITALY,
        INDIA,
        INDONESIA,
        BRAZIL,
        MEXICO,
        UNITED_STATE,
    }
}

enum class LibraryChipType {
    YOUR_LIBRARY,
    CHART,
    YOUTUBE_MUSIC_PLAYLIST,
    YOUTUBE_MIX_FOR_YOU,
    LOCAL_PLAYLIST,
    FAVORITE_PLAYLIST,
    DOWNLOADED_PLAYLIST,
    FAVORITE_PODCAST,
    ;

    fun toStringValue(): String =
        when (this) {
            YOUR_LIBRARY -> "your_library"
            YOUTUBE_MUSIC_PLAYLIST -> "youtube_music_playlist"
            YOUTUBE_MIX_FOR_YOU -> "youtube_mix_for_you"
            LOCAL_PLAYLIST -> "local_playlist"
            FAVORITE_PLAYLIST -> "favorite_playlist"
            DOWNLOADED_PLAYLIST -> "downloaded_playlist"
            FAVORITE_PODCAST -> "favorite_podcast"
            CHART -> "chart"
        }

    companion object {
        fun fromStringValue(value: String): LibraryChipType? =
            when (value) {
                "your_library" -> YOUR_LIBRARY
                "youtube_music_playlist" -> YOUTUBE_MUSIC_PLAYLIST
                "youtube_mix_for_you" -> YOUTUBE_MIX_FOR_YOU
                "local_playlist" -> LOCAL_PLAYLIST
                "favorite_playlist" -> FAVORITE_PLAYLIST
                "downloaded_playlist" -> DOWNLOADED_PLAYLIST
                "favorite_podcast" -> FAVORITE_PODCAST
                "chart" -> CHART
                else -> null
            }
    }
}

data class XevraePlaylist(
    val entity: PlaylistEntity? = null,
    val albumEntity: AlbumEntity? = null,
    val type: PlaylistType.Type
) : PlaylistType {
    override fun playlistType(): PlaylistType.Type = type
}

data class XevraeRecently(
    val song: SongEntity? = null,
    val album: AlbumEntity? = null,
    val playlist: PlaylistEntity? = null,
    val type: RecentlyType.Type
) : RecentlyType {
    override fun objectType(): RecentlyType.Type = type
}
