package com.metrolist.music.ui.screens.xevrae.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.metrolist.music.viewmodels.LibraryDynamicPlaylistViewModel
import com.metrolist.music.viewmodels.xevrae.AnalyticsViewModel
import com.metrolist.music.viewmodels.xevrae.SharedViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.metrolist.music.LocalActivity
import androidx.activity.ComponentActivity
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
@ExperimentalMaterial3Api
fun LibraryDynamicPlaylistScreen(
    innerPadding: PaddingValues,
    navController: NavController,
    type: String,
    viewModel: LibraryDynamicPlaylistViewModel = hiltViewModel(),
    analyticsViewModel: AnalyticsViewModel = hiltViewModel(),
    sharedViewModel: SharedViewModel = hiltViewModel(viewModelStoreOwner = LocalActivity.current as ComponentActivity),
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("LibraryDynamicPlaylist - stub")
    }
}

sealed class LibraryDynamicPlaylistType {
    data object Favorite : LibraryDynamicPlaylistType()
    data object Followed : LibraryDynamicPlaylistType()
    data object MostPlayed : LibraryDynamicPlaylistType()
    data object Downloaded : LibraryDynamicPlaylistType()
    data object TopTracks : LibraryDynamicPlaylistType()
    data object TopArtists : LibraryDynamicPlaylistType()
    data object TopAlbums : LibraryDynamicPlaylistType()

    fun name(): Int =
        when (this) {
            Favorite -> com.metrolist.music.R.string.favorite
            Followed -> com.metrolist.music.R.string.followed
            MostPlayed -> com.metrolist.music.R.string.most_played
            Downloaded -> com.metrolist.music.R.string.downloaded
            TopAlbums -> com.metrolist.music.R.string.your_top_albums
            TopArtists -> com.metrolist.music.R.string.your_top_artists
            TopTracks -> com.metrolist.music.R.string.your_top_tracks
        }

    fun toStringParams(): String =
        when (this) {
            Favorite -> "favorite"
            Followed -> "followed"
            MostPlayed -> "most_played"
            Downloaded -> "downloaded"
            TopAlbums -> "top_albums"
            TopArtists -> "top_artists"
            TopTracks -> "top_tracks"
        }

    companion object {
        fun toType(input: String): LibraryDynamicPlaylistType =
            when (input) {
                "favorite" -> Favorite
                "followed" -> Followed
                "most_played" -> MostPlayed
                "downloaded" -> Downloaded
                "top_albums" -> TopAlbums
                "top_artists" -> TopArtists
                "top_tracks" -> TopTracks
                else -> throw IllegalArgumentException("Unknown type: $input")
            }
    }
}
