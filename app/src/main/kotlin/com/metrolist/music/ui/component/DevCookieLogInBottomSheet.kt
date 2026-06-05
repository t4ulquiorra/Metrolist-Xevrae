package com.metrolist.music.ui.component

import androidx.compose.runtime.Composable

enum class DevLogInType {
    YouTube,
    Spotify,
    Discord,
}

@Composable
fun DevCookieLogInBottomSheet(
    onDismiss: () -> Unit,
    type: DevLogInType,
    cookies: String = "",
) {
    // Stub — developer cookie login bottom sheet
}
