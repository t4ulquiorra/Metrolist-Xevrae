package com.metrolist.music.ui.component

import androidx.compose.runtime.Composable

@Composable
fun AddToPlaylistModalBottomSheet(
    isBottomSheetVisible: Boolean,
    listLocalPlaylist: List<Any> = emptyList(),
    listYouTubePlaylist: List<Any> = emptyList(),
    onDismiss: () -> Unit = {},
    onClick: (Any) -> Unit = {},
    onYTPlaylistClick: (Any) -> Unit = {},
    videoId: String? = null,
) {
    // Stub — add to playlist bottom sheet
}
