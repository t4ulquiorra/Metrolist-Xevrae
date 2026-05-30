package com.metrolist.music.models.xevrae

import com.metrolist.innertube.models.YTItem

data class MoodsMomentObject(
    val header: String,
    val items: List<MoodItem>,
)

data class MoodItem(
    val header: String,
    val contents: List<MoodContent>,
)

data class MoodContent(
    val title: String,
    val subtitle: String?,
    val thumbnails: List<Thumbnail>?,
    val playlistBrowseId: String,
)

data class Thumbnail(
    val url: String,
)
