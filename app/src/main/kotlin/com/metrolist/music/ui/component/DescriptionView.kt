package com.metrolist.music.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString

@Composable
fun DescriptionView(
    text: String,
    modifier: Modifier = Modifier,
    onTimeClicked: ((String) -> Unit)? = null,
    onURLClicked: ((String) -> Unit)? = null,
) {
    if (text.isNotBlank()) {
        Text(
            text = text,
            modifier = modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
