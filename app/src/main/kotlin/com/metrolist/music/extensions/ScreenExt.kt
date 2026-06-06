package com.metrolist.music.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import com.metrolist.music.models.xevrae.ScreenSizeInfo

@Composable
fun getScreenSizeInfo(): ScreenSizeInfo {
    val config = LocalConfiguration.current
    val density = androidx.compose.ui.platform.LocalDensity.current
    val hDP = config.screenHeightDp
    val wDP = config.screenWidthDp
    val hPX = (hDP * density.density).toInt()
    val wPX = (wDP * density.density).toInt()
    return ScreenSizeInfo(hDP = hDP, wDP = wDP, hPX = hPX, wPX = wPX)
}

@Composable
fun rememberIsInPipMode(): Boolean {
    val context = LocalContext.current
    var isInPip by remember { mutableStateOf(false) }
    DisposableEffect(context) {
        val activity = context.findActivity()
        if (activity != null) {
            isInPip = activity.isInPictureInPictureMode
        }
        onDispose { }
    }
    return isInPip
}

fun Context.findActivity(): Activity? {
    var c = this
    while (c is ContextWrapper) { if (c is Activity) return c; c = c.baseContext }
    return null
}
