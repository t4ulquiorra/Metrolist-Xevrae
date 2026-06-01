package com.metrolist.music.ui.screens.xevrae.home.analytics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.metrolist.music.viewmodels.xevrae.AnalyticsViewModel
import com.metrolist.music.viewmodels.xevrae.SharedViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.ComponentActivity
import com.metrolist.music.LocalActivity

@Composable
fun AnalyticsScreen(
    innerPadding: PaddingValues,
    navController: NavController,
    analyticsViewModel: AnalyticsViewModel = hiltViewModel(),
    sharedViewModel: SharedViewModel = hiltViewModel(
        viewModelStoreOwner = LocalActivity.current as ComponentActivity
    ),
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Analytics - stub")
    }
}
