package com.metrolist.music.models.xevrae

import com.metrolist.innertube.models.SongItem

data class PlaylistState(
    val id: String = "",
    val title: String = "",
    val thumbnail: String? = null,
    val isRadio: Boolean = false,
    val listTracks: List<SongItem> = emptyList(),
)
