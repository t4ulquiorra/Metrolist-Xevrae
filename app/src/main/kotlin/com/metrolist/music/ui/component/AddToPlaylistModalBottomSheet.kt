package com.metrolist.music.ui.component

import androidx.compose.runtime.Composable
import com.metrolist.music.models.xevrae.LocalPlaylistEntity
import com.metrolist.music.models.xevrae.PlaylistsResult

@Composable
fun AddToPlaylistModalBottomSheet(
    isBottomSheetVisible: Boolean,
    listLocalPlaylist: List<LocalPlaylistEntity> = emptyList(),
    listYouTubePlaylist: List<PlaylistsResult> = emptyList(),
    onDismiss: () -> Unit = {},
    onClick: (LocalPlaylistEntity) -> Unit = {},
    onYTPlaylistClick: (PlaylistsResult) -> Unit = {},
    videoId: String? = null,
) {
    // Stub
}
