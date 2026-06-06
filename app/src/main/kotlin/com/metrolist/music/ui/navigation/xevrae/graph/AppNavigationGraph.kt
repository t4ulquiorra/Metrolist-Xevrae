package com.metrolist.music.ui.navigation.xevrae.graph

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.metrolist.music.ui.component.AppNavigationBar
import com.metrolist.music.ui.component.AppNavigationRail
import com.metrolist.music.ui.navigation.xevrae.destination.home.HomeDestination
import com.metrolist.music.ui.navigation.xevrae.destination.library.LibraryDestination
import com.metrolist.music.ui.navigation.xevrae.destination.player.FullscreenDestination
import com.metrolist.music.ui.navigation.xevrae.destination.search.SearchDestination
import com.metrolist.music.ui.screens.Screens
import com.metrolist.music.ui.screens.xevrae.home.HomeScreen
import com.metrolist.music.ui.screens.xevrae.library.LibraryScreen
import com.metrolist.music.ui.screens.xevrae.other.SearchScreen
import com.metrolist.music.ui.screens.xevrae.player.FullscreenPlayer
import com.metrolist.music.viewmodels.xevrae.SharedViewModel

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
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute by remember {
        derivedStateOf { navBackStackEntry?.destination?.route }
    }

    val navigationItems = remember { Screens.MainScreens }

    val isLandscape = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp >
        androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp

    val showNavigation by remember(currentRoute) {
        derivedStateOf {
            currentRoute != FullscreenDestination::class.qualifiedName &&
            currentRoute != null
        }
    }

    if (isLandscape) {
        Row(modifier = Modifier.fillMaxSize()) {
            if (showNavigation) {
                AppNavigationRail(
                    navigationItems = navigationItems,
                    currentRoute = currentRoute,
                    onItemClick = { screen, _ ->
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.weight(1f),
                enterTransition = { fadeIn() + slideInHorizontally { -it } },
                exitTransition = { fadeOut() + slideOutHorizontally { it } },
                popEnterTransition = { fadeIn() + slideInHorizontally { -it } },
                popExitTransition = { fadeOut() + slideOutHorizontally { it } },
            ) {
                buildNavGraph(innerPadding, navController, hideNavBar, showNavBar, showNowPlayingSheet, onScrolling)
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.weight(1f),
                enterTransition = { fadeIn() + slideInHorizontally { -it } },
                exitTransition = { fadeOut() + slideOutHorizontally { it } },
                popEnterTransition = { fadeIn() + slideInHorizontally { -it } },
                popExitTransition = { fadeOut() + slideOutHorizontally { it } },
            ) {
                buildNavGraph(innerPadding, navController, hideNavBar, showNavBar, showNowPlayingSheet, onScrolling)
            }
            if (showNavigation) {
                AppNavigationBar(
                    navigationItems = navigationItems,
                    currentRoute = currentRoute,
                    onItemClick = { screen, _ ->
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        }
    }
}

@ExperimentalMaterial3Api
@ExperimentalFoundationApi
private fun androidx.navigation.NavGraphBuilder.buildNavGraph(
    innerPadding: PaddingValues,
    navController: NavHostController,
    hideNavBar: () -> Unit,
    showNavBar: (Boolean) -> Unit,
    showNowPlayingSheet: () -> Unit,
    onScrolling: (Boolean) -> Unit,
) {
    composable<HomeDestination> {
        HomeScreen(onScrolling = onScrolling, navController = navController)
    }
    composable<SearchDestination> {
        SearchScreen(navController = navController)
    }
    composable<LibraryDestination> {
        LibraryScreen(innerPadding = innerPadding, navController = navController, onScrolling = onScrolling)
    }
    composable<FullscreenDestination> {
        FullscreenPlayer(
            navController,
            hideNavBar = hideNavBar,
            showNavBar = { showNavBar(true); showNowPlayingSheet() },
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
