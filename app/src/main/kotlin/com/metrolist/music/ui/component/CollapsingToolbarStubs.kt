package com.metrolist.music.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun CollapsingToolbarParallaxEffect(
    modifier: Modifier = Modifier,
    title: String = "",
    imageUrl: String? = null,
    onBack: () -> Unit = {},
    content: @Composable (Color) -> Unit,
) {
    Box(modifier = modifier) { content(Color.White) }
}
