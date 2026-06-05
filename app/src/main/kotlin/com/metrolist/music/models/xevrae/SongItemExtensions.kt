package com.metrolist.music.models.xevrae

import com.metrolist.innertube.models.SongItem
import com.metrolist.music.db.entities.SongEntity
import java.time.LocalDateTime

fun SongItem.toSongEntity(): SongEntity = SongEntity(
    id = id,
    title = title,
    duration = duration ?: 0,
    thumbnailUrl = thumbnail,
    albumId = album?.id,
    albumName = album?.name,
    liked = false,
    inLibrary = null,
)

fun SongItem.toTrackCompat(): Track = Track(
    videoId = id,
    title = title,
    thumbnails = listOf(Thumbnail(thumbnail)),
    artists = artists.map { Artist(it.name, it.id ?: "") },
    album = album?.let { com.metrolist.music.models.xevrae.Album(it.name, it.id ?: "") },
    duration = duration ?: 0,
    explicit = explicit,
)
