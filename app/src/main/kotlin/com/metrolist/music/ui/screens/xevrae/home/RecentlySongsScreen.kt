package com.metrolist.music.ui.screens.xevrae.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavController
import com.metrolist.music.viewmodels.xevrae.RecentlySongsViewModel
import com.metrolist.music.viewmodels.xevrae.SharedViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.metrolist.music.LocalActivity
import androidx.activity.ComponentActivity

@Composable
fun RecentlySongsScreen(
    innerPadding: PaddingValues,
    navController: NavController,
    viewModel: RecentlySongsViewModel = hiltViewModel(),
    sharedViewModel: SharedViewModel = hiltViewModel(
        viewModelStoreOwner = LocalActivity.current as ComponentActivity
    ),
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("RecentlySongs - stub")
    }
}
