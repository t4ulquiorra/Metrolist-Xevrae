package com.metrolist.music.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Gradient angle expressed as degrees (0 = top→bottom, 90 = left→right). */
enum class GradientAngle { CW0, CW45, CW90, CW135, CW180, CW225, CW270, CW315 }

/**
 * Precomputed start/end offsets for a linear gradient brush given a [GradientAngle].
 * Usage: val offset = GradientOffset(GradientAngle.CW135)
 */
data class GradientOffset(val start: Offset, val end: Offset) {
    companion object {
        operator fun invoke(angle: GradientAngle = GradientAngle.CW0): GradientOffset {
            val deg = angle.ordinal * 45.0
            val rad = (deg - 90.0) * (PI / 180.0)
            val x = cos(rad).toFloat()
            val y = sin(rad).toFloat()
            return GradientOffset(
                start = Offset(0.5f - x * 0.5f, 0.5f - y * 0.5f),
                end   = Offset(0.5f + x * 0.5f, 0.5f + y * 0.5f),
            )
        }
    }
}

/** Convert HSV floats to a Compose [Color]. */
fun hsvToColor(hue: Float, saturation: Float, value: Float): Color =
    Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value)))
