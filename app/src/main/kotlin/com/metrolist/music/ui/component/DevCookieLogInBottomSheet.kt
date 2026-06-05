package com.metrolist.music.ui.component

import androidx.compose.runtime.Composable

enum class DevLogInType {
    YouTube,
    Spotify,
    Discord,
}

@Composable
fun DevLogInBottomSheet(
    onDismiss: () -> Unit,
    onDone: (cookie: String, netscapeCookie: String) -> Unit,
    type: DevLogInType,
) {
    // Stub
}

@Composable
fun DevCookieLogInBottomSheet(
    onDismiss: () -> Unit,
    type: DevLogInType,
    cookies: List<Pair<String, String?>> = emptyList(),
) {
    // Stub
}
