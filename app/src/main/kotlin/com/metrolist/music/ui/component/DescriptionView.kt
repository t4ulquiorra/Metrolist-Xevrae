package com.metrolist.music.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DescriptionView(
    description: String?,
    modifier: Modifier = Modifier,
) {
    if (!description.isNullOrBlank()) {
        Text(
            text = description,
            modifier = modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
