package com.metrolist.music.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.metrolist.music.viewmodels.xevrae.NowPlayingBottomSheetViewModel

@Composable
fun NowPlayingBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    onNavigateToOtherScreen: (() -> Unit)? = null,
    song: Any? = null,
    setSleepTimerEnable: Boolean = false,
    changeMainLyricsProviderEnable: Boolean = false,
    viewModel: NowPlayingBottomSheetViewModel = hiltViewModel(),
) {
    // Stub — full implementation delegated to NowPlayingBottomSheetViewModel
}
