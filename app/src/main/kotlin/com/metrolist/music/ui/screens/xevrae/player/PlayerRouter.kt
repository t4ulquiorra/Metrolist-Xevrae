package com.metrolist.music.ui.screens.xevrae.player

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.metrolist.music.viewmodels.xevrae.SharedViewModel
import com.metrolist.music.utils.isLandscape

@Composable
fun PlayerRouter(
    navController: NavController,
    sharedViewModel: SharedViewModel,
    onDismiss: () -> Unit
) {
    val isLandscape = isLandscape()

    if (isLandscape) {
        onDismiss()
    } else {
        NowPlayingScreen(
            navController = navController,
            hideNavBar = {},
            showNavBar = {}
        )
    }
}
