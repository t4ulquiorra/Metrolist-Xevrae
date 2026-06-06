package com.metrolist.music.models.xevrae

import com.metrolist.innertube.models.SongItem
import com.metrolist.innertube.models.WatchEndpoint

data class PlaylistState(
    val id: String = "",
    val title: String = "",
    val thumbnail: String? = null,
    val isRadio: Boolean = false,
    val listTracks: List<SongItem> = emptyList(),
    val author: com.metrolist.innertube.models.Artist? = null,
    val description: String? = null,
    val trackCount: Int? = null,
    val year: String? = null,
    val shuffleEndpoint: WatchEndpoint? = null,
    val radioEndpoint: WatchEndpoint? = null,
)
