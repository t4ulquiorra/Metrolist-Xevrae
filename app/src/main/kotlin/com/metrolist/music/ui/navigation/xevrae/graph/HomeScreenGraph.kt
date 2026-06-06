package com.metrolist.music.ui.navigation.xevrae.graph

import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.metrolist.music.ui.navigation.xevrae.destination.home.AnalyticsDestination
import com.metrolist.music.ui.navigation.xevrae.destination.home.CreditDestination
import com.metrolist.music.ui.navigation.xevrae.destination.home.MoodDestination
import com.metrolist.music.ui.navigation.xevrae.destination.home.NotificationDestination
import com.metrolist.music.ui.navigation.xevrae.destination.home.RecentlySongsDestination
import com.metrolist.music.ui.navigation.xevrae.destination.home.SettingsDestination
import com.metrolist.music.ui.screens.xevrae.home.MoodScreen
import com.metrolist.music.ui.screens.xevrae.home.NotificationScreen
import com.metrolist.music.ui.screens.xevrae.home.RecentlySongsScreen
import com.metrolist.music.ui.screens.xevrae.home.SettingScreen
import com.metrolist.music.ui.screens.xevrae.home.analytics.AnalyticsScreen
import com.metrolist.music.ui.screens.xevrae.other.CreditScreen

import com.metrolist.music.ui.screens.xevrae.home.SettingScreen

fun NavGraphBuilder.homeScreenGraph(
    innerPadding: PaddingValues,
    navController: NavController,
    latestVersionName: String,
) {
    composable<CreditDestination> {
        CreditScreen(
            paddingValues = innerPadding,
            navController = navController,
        )
    }
    composable<MoodDestination> { entry ->
        val params = entry.toRoute<MoodDestination>().params
        MoodScreen(
            navController = navController,
            params = params,
        )
    }
    composable<NotificationDestination> {
        NotificationScreen(
            navController = navController,
        )
    }
    composable<RecentlySongsDestination> {
        RecentlySongsScreen(
            navController = navController,
            innerPadding = innerPadding,
        )
    }
    composable<SettingsDestination> {
        SettingScreen(
            innerPadding = innerPadding,
            navController = navController,
        )
    }
    composable<AnalyticsDestination> {
        AnalyticsScreen(
            navController = navController,
            innerPadding = innerPadding,
        )
    }
}