package com.metrolist.music.ui.navigation.xevrae.destination.list

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistDestination(
    val playlistId: String,
    val isYourYouTubePlaylist: Boolean = false,
)