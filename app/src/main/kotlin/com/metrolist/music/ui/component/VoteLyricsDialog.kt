package com.metrolist.music.ui.component

import androidx.compose.runtime.Composable

@Composable
fun VoteLyricsDialog(
    canVoteLyrics: Boolean = false,
    canVoteTranslatedLyrics: Boolean = false,
    lyricsVoteState: Any? = null,
    translatedLyricsVoteState: Any? = null,
    onVoteLyrics: (Boolean) -> Unit = {},
    onVoteTranslatedLyrics: (Boolean) -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    // Stub — lyrics voting dialog
}
