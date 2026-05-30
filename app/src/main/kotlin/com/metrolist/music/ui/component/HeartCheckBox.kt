package com.metrolist.music.ui.component

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.metrolist.music.R

@Composable
fun HeartCheckBox(
    size: Int = 24,
    checked: Boolean,
    onStateChange: (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val alpha by animateFloatAsState(if (isPressed) 0.4f else 1f, label = "heart_alpha")

    Box(
        modifier =
            Modifier
                .size(size.dp)
                .graphicsLayer { this.alpha = alpha }
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                ) {
                    onStateChange?.invoke()
                },
    ) {
        Crossfade(targetState = checked, modifier = Modifier.fillMaxSize(), label = "HeartCheckedCrossfade") {
            if (it) {
                Image(
                    painter = painterResource(R.drawable.baseline_favorite_24),
                    contentDescription = "Favorite checked",
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.baseline_favorite_border_24),
                    contentDescription = "Favorite unchecked",
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                    colorFilter = ColorFilter.tint(Color.White),
                )
            }
        }
    }
}
