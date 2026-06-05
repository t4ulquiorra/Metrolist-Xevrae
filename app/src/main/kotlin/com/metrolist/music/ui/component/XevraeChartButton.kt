package com.metrolist.music.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun XevraeChartButton(
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Button(onClick = onClick, modifier = modifier.padding(4.dp)) {
        Text("Charts")
    }
}
