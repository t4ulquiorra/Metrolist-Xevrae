package com.metrolist.music.models.xevrae

import com.metrolist.innertube.models.Artist
import com.metrolist.innertube.models.WatchEndpoint

data class PlaylistState(
    val id: String = "",
    val title: String = "",
    val isRadio: Boolean = false,
    val author: Artist? = null,
    val thumbnail: String? = null,
    val description: String? = null,
    val trackCount: Int? = null,
    val year: String? = null,
    val shuffleEndpoint: WatchEndpoint? = null,
    val radioEndpoint: WatchEndpoint? = null,
)
