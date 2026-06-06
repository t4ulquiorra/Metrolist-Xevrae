package com.metrolist.music.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.StateFlow

@Composable
fun LyricsView(
    lyricsData: Any? = null,
    timeLine: StateFlow<com.metrolist.music.models.xevrae.TimeLine>? = null,
    sliderPositionProvider: (() -> Long?)? = null,
    modifier: Modifier = Modifier,
    showLyrics: Boolean = true,
    userScrollEnabled: Boolean = true,
    onLineClick: ((Float) -> Unit)? = null,
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (sliderPositionProvider != null) {
            Lyrics(
                sliderPositionProvider = sliderPositionProvider,
                modifier = modifier,
                showLyrics = showLyrics,
            )
        }
    }
}
