package com.metrolist.music.ui.navigation.xevrae.graph

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.metrolist.music.ui.component.LiquidGlassNavBar
import com.metrolist.music.ui.navigation.xevrae.destination.home.HomeDestination
import com.metrolist.music.ui.navigation.xevrae.destination.library.LibraryDestination
import com.metrolist.music.ui.navigation.xevrae.destination.player.FullscreenDestination
import com.metrolist.music.ui.navigation.xevrae.destination.search.SearchDestination
import com.metrolist.music.ui.screens.xevrae.home.HomeScreen
import com.metrolist.music.ui.screens.xevrae.library.LibraryScreen
import com.metrolist.music.ui.screens.xevrae.other.SearchScreen
import com.metrolist.music.ui.screens.xevrae.player.FullscreenPlayer
import com.metrolist.music.ui.utils.rememberBackdrop
import com.metrolist.music.viewmodels.xevrae.SharedViewModel
import kotlin.reflect.KClass

@Composable
@ExperimentalMaterial3Api
@ExperimentalFoundationApi
fun AppNavigationGraph(
    innerPadding: PaddingValues,
    navController: NavHostController,
    sharedViewModel: SharedViewModel,
    startDestination: Any = HomeDestination,
    hideNavBar: () -> Unit = { },
    showNavBar: (shouldShowNowPlayingSheet: Boolean) -> Unit = { },
    showNowPlayingSheet: () -> Unit = {},
    onScrolling: (onTop: Boolean) -> Unit = {},
) {
    val backdrop = rememberBackdrop()
    var isScrolledToTop by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController,
            startDestination = startDestination,
            enterTransition = { fadeIn() + slideInHorizontally { -it } },
            exitTransition = { fadeOut() + slideOutHorizontally { it } },
            popEnterTransition = { fadeIn() + slideInHorizontally { -it } },
            popExitTransition = { fadeOut() + slideOutHorizontally { it } },
        ) {
            composable<HomeDestination> {
                HomeScreen(
                    onScrolling = { onTop ->
                        isScrolledToTop = onTop
                        onScrolling(onTop)
                    },
                    navController = navController,
                )
            }
            composable<SearchDestination> {
                SearchScreen(navController = navController)
            }
            composable<LibraryDestination> {
                LibraryScreen(
                    innerPadding = innerPadding,
                    navController = navController,
                    onScrolling = { onTop ->
                        isScrolledToTop = onTop
                        onScrolling(onTop)
                    },
                )
            }
            composable<FullscreenDestination> {
                FullscreenPlayer(
                    navController,
                    hideNavBar = hideNavBar,
                    showNavBar = {
                        showNavBar.invoke(true)
                        showNowPlayingSheet.invoke()
                    },
                )
            }
            homeScreenGraph(innerPadding = innerPadding, navController = navController, latestVersionName = "")
            libraryScreenGraph(innerPadding = innerPadding, navController = navController)
            listScreenGraph(innerPadding = innerPadding, navController = navController)
            loginScreenGraph(
                innerPadding = innerPadding,
                navController = navController,
                hideBottomBar = hideNavBar,
                showBottomBar = { showNavBar(false) },
            )
        }

        LiquidGlassNavBar(
            startDestination = startDestination,
            navController = navController,
            backdrop = backdrop,
            viewModel = sharedViewModel,
            isScrolledToTop = isScrolledToTop,
            onOpenNowPlaying = { navController.navigate(FullscreenDestination) },
            reloadDestinationIfNeeded = { kClass: KClass<*> ->
                val current = navController.currentBackStackEntry?.destination?.route
                if (current != null && current.contains(kClass.simpleName ?: "")) {
                    navController.currentBackStackEntry?.savedStateHandle?.set("scrollToTop", true)
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}
