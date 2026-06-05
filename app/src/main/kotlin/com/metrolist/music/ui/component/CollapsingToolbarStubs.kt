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

@Composable
fun LimitedBorderAnimationView(
    isAnimated: Boolean = false,
    brush: androidx.compose.ui.graphics.Brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color.White, Color.White)),
    backgroundColor: Color = Color.Transparent,
    contentPadding: androidx.compose.ui.unit.Dp = androidx.compose.ui.unit.Dp(0f),
    borderWidth: androidx.compose.ui.unit.Dp = androidx.compose.ui.unit.Dp(1f),
    shape: androidx.compose.ui.graphics.Shape = androidx.compose.foundation.shape.RoundedCornerShape(0),
    oneCircleDurationMillis: Int = 1000,
    interactionNumber: Int = 1,
    content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit,
) { Box { content() } }
