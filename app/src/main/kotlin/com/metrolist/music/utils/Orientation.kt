package com.metrolist.music.utils

import android.content.Context
import android.content.res.Configuration

enum class Orientation {
    PORTRAIT, LANDSCAPE, UNSPECIFIED
}

fun currentOrientation(context: Context): Orientation {
    val orientation = context.resources.configuration.orientation
    return when (orientation) {
        Configuration.ORIENTATION_PORTRAIT -> Orientation.PORTRAIT
        Configuration.ORIENTATION_LANDSCAPE -> Orientation.LANDSCAPE
        else -> Orientation.UNSPECIFIED
    }
}

@Composable
fun isLandscape(): Boolean {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}
