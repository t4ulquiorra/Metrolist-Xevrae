package com.metrolist.music.ui.screens.xevrae.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.metrolist.music.viewmodels.LocalPlaylistViewModel
import com.metrolist.music.viewmodels.xevrae.SharedViewModel

@Composable
fun LocalPlaylistScreen(
    id: Long,
    sharedViewModel: SharedViewModel = hiltViewModel(),
    viewModel: LocalPlaylistViewModel = hiltViewModel(),
    navController: NavController,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("LocalPlaylist - stub")
    }
}
