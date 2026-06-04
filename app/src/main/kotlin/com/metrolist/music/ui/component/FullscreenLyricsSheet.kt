package com.metrolist.music.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun FullscreenLyricsSheet(
    onDismiss: () -> Unit,
    sliderPositionProvider: () -> Long?,
    modifier: Modifier = Modifier,
) {
    LyricsView(
        sliderPositionProvider = sliderPositionProvider,
        modifier = modifier,
        showLyrics = true,
    )
}
