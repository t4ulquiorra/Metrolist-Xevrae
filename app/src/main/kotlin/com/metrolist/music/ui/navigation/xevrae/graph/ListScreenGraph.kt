package com.metrolist.music.ui.navigation.xevrae.graph

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.metrolist.music.ui.navigation.xevrae.destination.list.AlbumDestination
import com.metrolist.music.ui.navigation.xevrae.destination.list.ArtistDestination
import com.metrolist.music.ui.navigation.xevrae.destination.list.LocalPlaylistDestination
import com.metrolist.music.ui.navigation.xevrae.destination.list.MoreAlbumsDestination
import com.metrolist.music.ui.navigation.xevrae.destination.list.PlaylistDestination
import com.metrolist.music.ui.navigation.xevrae.destination.list.PodcastDestination
import com.metrolist.music.ui.screens.xevrae.library.LocalPlaylistScreen
import com.metrolist.music.ui.screens.xevrae.other.AlbumScreen
import com.metrolist.music.ui.screens.xevrae.other.ArtistScreen
import com.metrolist.music.ui.screens.xevrae.other.MoreAlbumsScreen
import com.metrolist.music.ui.screens.xevrae.other.PlaylistScreen
import com.metrolist.music.ui.screens.xevrae.other.PodcastScreen

@ExperimentalMaterial3Api
@ExperimentalFoundationApi
fun NavGraphBuilder.listScreenGraph(
    innerPadding: PaddingValues,
    navController: NavController,
) {
    composable<AlbumDestination> { entry ->
        val data = entry.toRoute<AlbumDestination>()
        AlbumScreen(
            browseId = data.browseId,
            navController = navController,
        )
    }
    composable<ArtistDestination> { entry ->
        val data = entry.toRoute<ArtistDestination>()
        ArtistScreen(
            channelId = data.channelId,
            navController = navController,
        )
    }
    composable<LocalPlaylistDestination> { entry ->
        val data = entry.toRoute<LocalPlaylistDestination>()
        LocalPlaylistScreen(
            id = data.id,
            navController = navController,
        )
    }
    composable<MoreAlbumsDestination> { entry ->
        val data = entry.toRoute<MoreAlbumsDestination>()
        MoreAlbumsScreen(
            innerPadding = innerPadding,
            navController = navController,
            type = data.type,
            id = data.id,
        )
    }
    composable<PlaylistDestination> { entry ->
        val data = entry.toRoute<PlaylistDestination>()
        PlaylistScreen(
            playlistId = data.playlistId,
            isYourYouTubePlaylist = data.isYourYouTubePlaylist,
            navController = navController,
        )
    }
    composable<PodcastDestination> { entry ->
        val data = entry.toRoute<PodcastDestination>()
        PodcastScreen(
            podcastId = data.podcastId,
            navController = navController,
        )
    }
}