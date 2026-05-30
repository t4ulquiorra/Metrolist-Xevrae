package com.metrolist.music.ui.screens.xevrae.player

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.metrolist.music.viewmodels.xevrae.SharedViewModel
import com.metrolist.music.utils.isLandscape
import com.metrolist.music.ui.component.SidePlayerPanel

@Composable
fun PlayerRouter(
    navController: NavController,
    sharedViewModel: SharedViewModel,
    onDismiss: () -> Unit
) {
    val isLandscape = isLandscape()

    if (isLandscape) {
        // In landscape, we might want to just dismiss the fullscreen player 
        // because the side panel will be visible anyway (if implemented in AdaptiveScaffold)
        // Or we show a landscape-optimized side panel.
        // For now, let's follow the plan.
        onDismiss() 
    } else {
        FullscreenPlayer(
            navController = navController,
            hideNavBar = {}, // Handled by nav graph
            showNavBar = {}
        )
    }
}
