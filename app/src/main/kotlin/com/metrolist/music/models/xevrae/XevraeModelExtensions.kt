package com.metrolist.music.models.xevrae
import androidx.core.net.toUri

import com.metrolist.music.db.entities.SongEntity
import com.metrolist.music.db.entities.LyricsEntity

fun SongsResult.toTrack(): Track =
    Track(
        album = this.album,
        artists = this.artists,
        duration = this.duration ?: "",
        durationSeconds = this.durationSeconds ?: 0,
        isAvailable = true,
        isExplicit = this.isExplicit ?: false,
        likeStatus = "",
        thumbnails = this.thumbnails,
        title = this.title ?: "",
        videoId = this.videoId,
        videoType = this.videoType ?: "",
        category = this.category,
        feedbackTokens = this.feedbackTokens,
        resultType = this.resultType,
        year = "",
    )

fun VideosResult.toTrack(): Track {
    val thumb = Thumbnail("http://i.ytimg.com/vi/${this.videoId}/maxresdefault.jpg")
    val thumbList = this.thumbnails ?: listOf(thumb)
    return Track(
        album = null,
        artists = this.artists,
        duration = this.duration ?: "",
        durationSeconds = this.durationSeconds ?: 0,
        isAvailable = true,
        isExplicit = false,
        likeStatus = "INDIFFERENT",
        thumbnails = thumbList,
        title = this.title,
        videoId = this.videoId,
        videoType = this.videoType ?: "",
        category = this.category,
        feedbackTokens = null,
        resultType = this.resultType,
        year = "",
    )
}

fun Track.toSongEntity(): SongEntity {
    return SongEntity(
        id = this.videoId,
        title = this.title,
        duration = this.durationSeconds ?: -1,
        thumbnailUrl = this.thumbnails?.lastOrNull()?.url,
        albumId = this.album?.id,
        albumName = this.album?.name,
        explicit = this.isExplicit,
    )
}

fun SongEntity.toTrack(): Track {
    // Note: Metrolist SongEntity doesn't have artists directly.
    // This is a simplified conversion for compatibility.
    return Track(
        album = albumId?.let { albumName?.let { name -> Album(name, it) } },
        artists = emptyList(), // Artists are handled via SongArtistMap in Metrolist
        duration = "", // Could be formatted if needed
        durationSeconds = duration,
        isAvailable = true,
        isExplicit = explicit,
        likeStatus = if (liked) "LIKE" else "INDIFFERENT",
        thumbnails = thumbnailUrl?.let { listOf(Thumbnail(it)) },
        title = title,
        videoId = id,
        videoType = if (isVideo) "VIDEO" else "MUSIC",
        category = null,
        feedbackTokens = null,
        resultType = null,
        year = year?.toString(),
    )
}

fun Content.toTrack(): Track =
    Track(
        album = album,
        artists = artists ?: emptyList(),
        duration = "",
        durationSeconds = durationSeconds,
        isAvailable = true,
        isExplicit = isExplicit ?: false,
        likeStatus = "INDIFFERENT",
        thumbnails = thumbnails,
        title = title,
        videoId = videoId ?: "",
        videoType = "",
        category = null,
        feedbackTokens = null,
        resultType = null,
        year = "",
    )

fun List<Artist>.toListName(): List<String> = map { it.name }

fun List<String>.connectArtists(): String {
    return joinToString(", ")
}

fun Track.toMediaItem() = androidx.media3.common.MediaItem.Builder()
    .setMediaId(videoId)
    .setUri(videoId)
    .setCustomCacheKey(videoId)
    .setMediaMetadata(
        androidx.media3.common.MediaMetadata.Builder()
            .setTitle(title)
            .setSubtitle(artists?.joinToString { it.name } ?: "")
            .setArtist(artists?.joinToString { it.name } ?: "")
            .setArtworkUri(thumbnails?.lastOrNull()?.url?.toUri())
            .setDisplayTitle(title)
            .setIsBrowsable(false)
            .setIsPlayable(true)
            .build()
    )
    .build()

fun Lyrics.toLyricsEntity(videoId: String): LyricsEntity =
    LyricsEntity(
        id = videoId,
        lyrics = lines?.joinToString("\n") { it.words } ?: "",
    )

fun LyricsEntity.toLyrics(): Lyrics = Lyrics(
    lines = lyrics.split("\n").map { Line(words = it, endTimeMs = "", startTimeMs = "") },
    syncType = null
)



fun Track.toSongItem(): com.metrolist.innertube.models.SongItem =
    com.metrolist.innertube.models.SongItem(
        id = videoId,
        title = title,
        artists = artists?.map {
            com.metrolist.innertube.models.Artist(name = it.name, id = it.id)
        } ?: emptyList(),
        album = album?.let {
            com.metrolist.innertube.models.Album(name = it.name, id = it.id)
        },
        duration = durationSeconds,
        thumbnail = thumbnails?.lastOrNull()?.url ?: "",
        explicit = isExplicit,
    )

fun ArrayList<Track>.toSongItemList(): List<com.metrolist.innertube.models.SongItem> =
    map { it.toSongItem() }

fun PodcastBrowse.EpisodeItem.toTrack(): Track =
    Track(
        album = null,
        artists = listOf(author),
        duration = durationString,
        durationSeconds = null,
        isAvailable = true,
        isExplicit = false,
        likeStatus = null,
        thumbnails = thumbnail,
        title = title,
        videoId = videoId,
        videoType = null,
        category = null,
        feedbackTokens = null,
        resultType = null,
    )

fun com.metrolist.innertube.models.SongItem.toMetadataSongEntity(): com.metrolist.music.db.entities.SongEntity =
    com.metrolist.music.db.entities.SongEntity(
        id = id,
        title = title,
        thumbnailUrl = thumbnail,
        explicit = explicit,
        duration = duration ?: -1,
    )

fun com.metrolist.innertube.models.SongItem.toTrackCompat(): Track =
    Track(
        album = album?.let { Album(it.name, it.id) },
        artists = artists.map { Artist(it.name, it.id ?: "") },
        duration = duration?.toString(),
        durationSeconds = duration,
        isAvailable = true,
        isExplicit = explicit,
        likeStatus = null,
        thumbnails = listOf(Thumbnail(thumbnail)),
        title = title,
        videoId = id,
        videoType = musicVideoType,
        category = null,
        feedbackTokens = null,
        resultType = null,
    )
