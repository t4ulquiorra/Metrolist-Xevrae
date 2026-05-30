package com.metrolist.music.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.window.core.layout.WindowWidthSizeClass
import com.metrolist.music.ui.screens.Screens
import com.metrolist.music.ui.screens.xevrae.player.NowPlayingScreenContent
import com.metrolist.music.viewmodels.xevrae.SharedViewModel
import com.metrolist.music.utils.isLandscape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveScaffold(
    navController: NavHostController,
    sharedViewModel: SharedViewModel,
    navigationItems: List<Screens>,
    currentRoute: String?,
    onNavItemClick: (Screens, Boolean) -> Unit,
    onSearchLongClick: () -> Unit,
    pureBlack: Boolean,
    slimNav: Boolean,
    isShowMiniPlayer: Boolean,
    isShowNowPlayingPanel: Boolean,
    onMiniPlayerClick: () -> Unit,
    onMiniPlayerClose: () -> Unit,
    onNowPlayingPanelDismiss: () -> Unit,
    topBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    bottomSheetPlayer: @Composable () -> Unit = {},
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit
) {
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val isLandscape = isLandscape()
    val isCompact = adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT
    
    // Determine if we should show the navigation rail instead of bottom bar
    val showRail = !isCompact || isLandscape

    if (!showRail) {
        // Portrait phone: bottom nav + MiniPlayer (handled by bottomSheetPlayer or manually)
        Scaffold(
            topBar = topBar,
            snackbarHost = snackbarHost,
            bottomBar = {
                Column {
                    // Metrolist's BottomSheetPlayer usually handles the MiniPlayer internally 
                    // or we can wrap it here.
                    bottomSheetPlayer()
                    
                    AppNavigationBar(
                        navigationItems = navigationItems,
                        currentRoute = currentRoute,
                        onItemClick = onNavItemClick,
                        pureBlack = pureBlack,
                        slimNav = slimNav,
                        onSearchLongClick = onSearchLongClick
                    )
                }
            },
            modifier = modifier,
            content = content
        )
    } else {
        // Landscape or Tablet: navigation rail + side player panel
        Scaffold(
            topBar = topBar,
            snackbarHost = snackbarHost,
            modifier = modifier
        ) { innerPadding ->
            Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                AppNavigationRail(
                    navigationItems = navigationItems,
                    currentRoute = currentRoute,
                    onItemClick = onNavItemClick,
                    pureBlack = pureBlack,
                    onSearchLongClick = onSearchLongClick
                )
                
                Box(modifier = Modifier.weight(1f)) {
                    content(PaddingValues(0.dp))
                }

                if (isLandscape || !isCompact) {
                    AnimatedVisibility(
                        visible = isShowNowPlayingPanel,
                        enter = expandHorizontally() + fadeIn(),
                        exit = shrinkHorizontally() + fadeOut()
                    ) {
                        SidePlayerPanel(
                            modifier = Modifier.width(360.dp),
                            navController = navController,
                            sharedViewModel = sharedViewModel,
                            onDismiss = onNowPlayingPanelDismiss
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SidePlayerPanel(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    sharedViewModel: SharedViewModel,
    onDismiss: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        NowPlayingScreenContent(
            navController = navController,
            sharedViewModel = sharedViewModel,
            isExpanded = true,
            dismissIcon = Icons.AutoMirrored.Rounded.ArrowForwardIos,
            onDismiss = onDismiss
        )
    }
}
