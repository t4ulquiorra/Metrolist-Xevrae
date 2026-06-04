package com.metrolist.music.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.metrolist.music.viewmodels.xevrae.NowPlayingBottomSheetViewModel
import com.metrolist.music.viewmodels.xevrae.NowPlayingBottomSheetUIEvent

@Composable
fun NowPlayingBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NowPlayingBottomSheetViewModel = hiltViewModel(),
) {
    // Stub — full implementation delegated to NowPlayingBottomSheetViewModel
}
