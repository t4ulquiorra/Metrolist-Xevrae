package com.metrolist.music.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.metrolist.music.models.xevrae.PodcastBrowse

@Composable
fun PodcastEpisodeFullWidthItem(
    episode: PodcastBrowse.EpisodeItem,
    onClick: () -> Unit = {},
    onMoreClickListener: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp),
    ) {
        Text(text = episode.title, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(8.dp))
    }
}
