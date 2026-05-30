package com.metrolist.music.models.xevrae

import com.metrolist.innertube.models.AlbumItem
import com.metrolist.innertube.models.PlaylistItem
import com.metrolist.innertube.models.SongItem
import com.metrolist.innertube.models.YTItem

data class Singles(
    val results: List<AlbumItem> = emptyList(),
    val params: String? = null
)

data class Albums(
    val results: List<AlbumItem> = emptyList(),
    val params: String? = null
)

data class Related(
    val results: List<YTItem> = emptyList()
)

data class Videos(
    val results: List<SongItem> = emptyList(),
    val params: String? = null
)
