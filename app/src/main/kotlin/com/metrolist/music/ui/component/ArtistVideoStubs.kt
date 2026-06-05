package com.metrolist.music.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun HomeItemVideo(
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    data: com.metrolist.music.models.xevrae.Content,
    modifier: Modifier = Modifier,
) { Box(modifier = modifier.fillMaxWidth().clickable { onClick() }) }

@Composable
fun ArtistFullWidthItems(
    onClickListener: () -> Unit,
    data: com.metrolist.music.db.entities.ArtistEntity,
    modifier: Modifier = Modifier,
) { Box(modifier = modifier.fillMaxWidth().clickable { onClickListener() }) }
