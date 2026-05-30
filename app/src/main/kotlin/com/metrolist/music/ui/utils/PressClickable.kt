package com.metrolist.music.ui.utils

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch

fun Modifier.pressClickable(
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier = composed {
    val scale = remember { Animatable(1f) }
    val alpha = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    this
        .graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
            this.alpha = alpha.value
        }
        .pointerInput(enabled) {
            detectTapGestures(
                onPress = {
                    if (!enabled) return@detectTapGestures
                    scope.launch { scale.animateTo(0.96f, tween(80)) }
                    scope.launch { alpha.animateTo(0.75f, tween(80)) }
                    val released = tryAwaitRelease()
                    scope.launch { scale.animateTo(1f, tween(80)) }
                    scope.launch { alpha.animateTo(1f, tween(80)) }
                    if (released) onClick()
                }
            )
        }
}

fun Modifier.lightPressClickable(
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier = composed {
    val scale = remember { Animatable(1f) }
    val alpha = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    this
        .graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
            this.alpha = alpha.value
        }
        .pointerInput(enabled) {
            detectTapGestures(
                onPress = {
                    if (!enabled) return@detectTapGestures
                    scope.launch { scale.animateTo(0.98f, tween(80)) }
                    scope.launch { alpha.animateTo(0.75f, tween(80)) }
                    val released = tryAwaitRelease()
                    scope.launch { scale.animateTo(1f, tween(80)) }
                    scope.launch { alpha.animateTo(1f, tween(80)) }
                    if (released) onClick()
                }
            )
        }
}
