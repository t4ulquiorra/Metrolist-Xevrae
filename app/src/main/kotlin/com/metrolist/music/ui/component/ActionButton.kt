package com.metrolist.music.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun ActionButton(
    icon: Painter,
    @StringRes text: Int?,
    textString: String? = null,
    textColor: Color? = null,
    iconColor: Color = MaterialTheme.colorScheme.onSurface,
    enable: Boolean = true,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .wrapContentHeight(Alignment.CenterVertically)
                .then(
                    if (enable) {
                        Modifier.clickable { onClick.invoke() }
                    } else {
                        Modifier
                    },
                )
                .padding(vertical = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .graphicsLayer {
                    alpha = if (enable) 1f else 0.38f
                },
        ) {
            Image(
                painter = icon,
                contentDescription = if (text != null) stringResource(text) else textString ?: "",
                modifier =
                    Modifier
                        .wrapContentSize(Alignment.Center)
                        .padding(12.dp),
                colorFilter = ColorFilter.tint(iconColor),
            )
            Text(
                text = if (text != null) stringResource(text) else textString ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = textColor ?: Color.Unspecified,
                modifier =
                    Modifier
                        .padding(start = 10.dp)
                        .wrapContentHeight(Alignment.CenterVertically),
            )
        }
    }
}
