package com.metrolist.music.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.metrolist.music.ui.screens.xevrae.player.NowPlayingScreenContent
import com.metrolist.music.viewmodels.xevrae.SharedViewModel

@Composable
fun SidePlayerPanel(
    modifier: Modifier = Modifier,
    navController: NavController,
    sharedViewModel: SharedViewModel,
    onDismiss: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        NowPlayingScreenContent(
            navController = navController,
            sharedViewModel = sharedViewModel,
            isExpanded = true,
            dismissIcon = Icons.AutoMirrored.Rounded.ArrowForwardIos,
            onDismiss = onDismiss
        )
    }
}
