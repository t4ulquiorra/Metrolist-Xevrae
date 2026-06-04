package com.metrolist.music.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import com.metrolist.music.models.xevrae.ScreenSizeInfo

@Composable
fun getScreenSizeInfo(): ScreenSizeInfo {
    val config = LocalConfiguration.current
    val density = LocalDensity.current
    val hDP = config.screenHeightDp
    val wDP = config.screenWidthDp
    val hPX = with(density) { hDP * density.density }.toInt()
    val wPX = with(density) { wDP * density.density }.toInt()
    return ScreenSizeInfo(hDP = hDP, wDP = wDP, hPX = hPX, wPX = wPX)
}

@Composable
fun rememberIsInPipMode(): Boolean {
    val activity = LocalContext.current.findActivity() ?: return false
    var isInPip by remember { mutableStateOf(activity.isInPictureInPictureMode) }
    DisposableEffect(activity) {
        val listener = object : android.app.Application.ActivityLifecycleCallbacks {
            override fun onActivityPictureInPictureModeChanged(a: Activity, inPip: Boolean) {
                if (a === activity) isInPip = inPip
            }
            override fun onActivityCreated(a: Activity, b: android.os.Bundle?) = Unit
            override fun onActivityStarted(a: Activity) = Unit
            override fun onActivityResumed(a: Activity) = Unit
            override fun onActivityPaused(a: Activity) = Unit
            override fun onActivityStopped(a: Activity) = Unit
            override fun onActivitySaveInstanceState(a: Activity, b: android.os.Bundle) = Unit
            override fun onActivityDestroyed(a: Activity) = Unit
        }
        activity.application.registerActivityLifecycleCallbacks(listener)
        onDispose { activity.application.unregisterActivityLifecycleCallbacks(listener) }
    }
    return isInPip
}

private fun Context.findActivity(): Activity? {
    var c = this
    while (c is ContextWrapper) { if (c is Activity) return c; c = c.baseContext }
    return null
}
