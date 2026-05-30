package com.metrolist.music.models.xevrae

data class AlbumsResult(
    val browseId: String,
    val thumbnails: List<Thumbnail>,
    val title: String,
    val year: String? = null,
    val playlistId: String? = null,
)
