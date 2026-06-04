package com.metrolist.music.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.metrolist.music.viewmodels.xevrae.SharedViewModel

@Composable
fun FullscreenLyricsSheet(
    sharedViewModel: SharedViewModel,
    navController: NavController,
    color: Color = Color.Transparent,
    shouldHaze: Boolean = false,
    onDismiss: () -> Unit = {},
) {
    val screenDataState = sharedViewModel.nowPlayingScreenData.value
    LyricsView(
        lyricsData = screenDataState.lyricsData,
        timeLine = sharedViewModel.timeline,
        showLyrics = true,
    )
}
