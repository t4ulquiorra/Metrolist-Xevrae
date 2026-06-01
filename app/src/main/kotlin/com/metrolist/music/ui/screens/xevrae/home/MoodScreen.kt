package com.metrolist.music.ui.screens.xevrae.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.metrolist.music.viewmodels.MoodViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun MoodScreen(
    navController: NavController,
    viewModel: MoodViewModel = hiltViewModel(),
    params: String?,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Mood - stub")
    }
}
