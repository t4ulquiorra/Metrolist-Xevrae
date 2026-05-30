package com.metrolist.music.ui.navigation.xevrae.graph

import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.metrolist.music.ui.navigation.xevrae.destination.login.DiscordLoginDestination
import com.metrolist.music.ui.navigation.xevrae.destination.login.LoginDestination
import com.metrolist.music.ui.navigation.xevrae.destination.login.SpotifyLoginDestination
import com.metrolist.music.ui.screens.xevrae.login.DiscordLoginScreen
import com.metrolist.music.ui.screens.xevrae.login.LoginScreen
import com.metrolist.music.ui.screens.xevrae.login.SpotifyLoginScreen

import com.metrolist.music.ui.screens.settings.DiscordLoginScreen as MetrolistDiscordLoginScreen

fun NavGraphBuilder.loginScreenGraph(
    innerPadding: PaddingValues,
    navController: NavController,
    hideBottomBar: () -> Unit,
    showBottomBar: () -> Unit,
) {
    composable<LoginDestination> {
        LoginScreen(
            innerPadding = innerPadding,
            navController = navController,
            hideBottomNavigation = hideBottomBar,
            showBottomNavigation = showBottomBar,
        )
    }

    composable<SpotifyLoginDestination> {
        SpotifyLoginScreen(
            innerPadding = innerPadding,
            navController = navController,
            hideBottomNavigation = hideBottomBar,
            showBottomNavigation = showBottomBar,
        )
    }

    composable<DiscordLoginDestination> {
        MetrolistDiscordLoginScreen(
            navController = navController,
        )
    }
}