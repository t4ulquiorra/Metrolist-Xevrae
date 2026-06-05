package com.metrolist.music.ui.component

import androidx.compose.runtime.Composable
import com.metrolist.innertube.models.SongItem

@Composable
fun PlaylistBottomSheet(
    onDismiss: () -> Unit,
    playlistId: String? = null,
    playlistName: String? = null,
    isYourYouTubePlaylist: Boolean = false,
    onSaveToLocal: (() -> Unit)? = null,
    onEditTitle: ((String) -> Unit)? = null,
    onAddToQueue: (() -> Unit)? = null,
) {
    // Stub — playlist options bottom sheet
}
