package com.metrolist.music.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.metrolist.music.R
import com.metrolist.music.db.entities.AlbumEntity
import com.metrolist.music.db.entities.PlaylistEntity
import com.metrolist.music.db.entities.SongEntity
import com.metrolist.music.db.entities.ArtistEntity
import com.metrolist.music.models.xevrae.ArtistItemCompat
import com.metrolist.music.models.xevrae.ChartItem
import com.metrolist.music.models.xevrae.Content
import com.metrolist.music.models.xevrae.PlaylistsResult
import com.metrolist.music.models.xevrae.PlaylistType
import com.metrolist.music.models.xevrae.RecentlyType
import com.metrolist.music.models.xevrae.Thumbnail
import com.metrolist.music.models.xevrae.connectArtists
import com.metrolist.music.models.xevrae.toListName
import com.metrolist.music.models.xevrae.toTrack
import com.metrolist.music.ui.theme.xevrae.typo
import com.metrolist.music.ui.theme.xevrae.white

@Composable
fun HomeItem(
    navController: NavController,
    data: HomeItem,
    onTrackClick: (videoId: String) -> Unit = {},
) {
    Column(
        Modifier
            .padding(vertical = 10.dp)
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {
        Text(
            text = data.title,
            style = typo().headlineMedium,
            color = white,
            maxLines = 1,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
        )
        LazyRow(
            modifier = Modifier.padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Spacer(modifier = Modifier.width(10.dp))
            }
            items(data.contents, key = { it.videoId ?: it.browseId ?: it.title }) { content ->
                HomeItemContentPlaylist(
                    data = content,
                    navController = navController,
                    onTrackClick = onTrackClick,
                )
            }
            item {
                Spacer(modifier = Modifier.width(10.dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeItemContentPlaylist(
    data: Content,
    navController: NavController,
    onTrackClick: (videoId: String) -> Unit,
) {
    val thumb = data.thumbnails?.lastOrNull()?.url
    Column(
        Modifier
            .width(160.dp)
            .wrapContentHeight()
            .clip(RoundedCornerShape(10.dp))
            .clickable {
                if (data.videoId != null) {
                    onTrackClick(data.videoId)
                } else if (data.browseId != null) {
                    if (data.browseId.startsWith("VL") || data.browseId.startsWith("PL")) {
                        navController.navigate("playlist/${data.browseId}")
                    } else if (data.browseId.startsWith("MPRE") || data.browseId.startsWith("FMAN")) {
                        navController.navigate("album/${data.browseId}")
                    } else {
                        navController.navigate("artist/${data.browseId}")
                    }
                }
            },
    ) {
        Box(
            modifier =
                Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(10.dp)),
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(thumb)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                placeholder = painterResource(R.drawable.baseline_album_24),
                error = painterResource(R.drawable.baseline_album_24),
            )
            if (data.videoId != null) {
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = data.title,
            style = typo().titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        Text(
            text = data.artists?.toListName()?.connectArtists() ?: "",
            style = typo().bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

@Composable
fun HomeItemContentPlaylist(
    onClick: () -> Unit,
    data: Any,
    thumbSize: Dp = 160.dp,
) {
    val thumb = when (data) {
        is Content -> data.thumbnails?.lastOrNull()?.url
        is PlaylistEntity -> data.thumbnail
        is PlaylistsResult -> data.thumbnails?.lastOrNull()?.url
        is AlbumEntity -> data.thumbnails
        is ChartItem -> null
        is SongEntity -> data.thumbnail
        else -> null
    }

    val title = when (data) {
        is Content -> data.title
        is PlaylistEntity -> data.name
        is PlaylistsResult -> data.title
        is AlbumEntity -> data.title
        is ChartItem -> "Top 50 Weekly"
        is SongEntity -> data.title
        else -> ""
    }

    Column(
        Modifier
            .width(thumbSize)
            .wrapContentHeight()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
    ) {
        Box(
            modifier = Modifier
                .size(thumbSize)
                .clip(RoundedCornerShape(10.dp)),
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(thumb)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                placeholder = painterResource(R.drawable.baseline_album_24),
                error = painterResource(R.drawable.baseline_album_24),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = typo().titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

@Composable
fun SongFullWidthItems(
    songEntity: SongEntity,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    onMoreClickListener: (String) -> Unit = {},
    onClickListener: (String) -> Unit = {},
    onAddToQueue: (String) -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClickListener(songEntity.id) }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(songEntity.thumbnail)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = songEntity.title,
                style = typo().titleSmall,
                color = Color.White,
                maxLines = 1
            )
            Text(
                text = songEntity.artistName ?: "",
                style = typo().bodySmall,
                color = Color.LightGray,
                maxLines = 1
            )
        }
        IconButton(onClick = { onMoreClickListener(songEntity.id) }) {
            Icon(
                painter = painterResource(R.drawable.more_vert),
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

@Composable
fun ArtistFullWidthItems(
    data: ArtistEntity,
    modifier: Modifier = Modifier,
    onClickListener: (String) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClickListener(data.channelId ?: "") }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(data.thumbnail)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = data.name,
            style = typo().titleSmall,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun PlaylistFullWidthItems(
    data: PlaylistType,
    modifier: Modifier = Modifier,
    onClickListener: () -> Unit,
) {
    val title = when (data) {
        is PlaylistEntity -> data.name
        is AlbumEntity -> data.title
        else -> ""
    }
    val thumb = when (data) {
        is PlaylistEntity -> data.thumbnail
        is AlbumEntity -> data.thumbnails
        else -> null
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClickListener() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(thumb)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = title,
            style = typo().titleSmall,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun QuickPicksItem(
    onClick: () -> Unit,
    onMoreClickListener: (String) -> Unit = {},
    data: Content,
    widthDp: Dp,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .width(widthDp)
                .height(65.dp)
                .padding(vertical = 5.dp, horizontal = 10.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable {
                    onClick()
                },
    ) {
        val thumb = data.thumbnails?.lastOrNull()?.url
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(thumb)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .size(55.dp)
                    .clip(RoundedCornerShape(10.dp)),
            placeholder = painterResource(R.drawable.baseline_album_24),
            error = painterResource(R.drawable.baseline_album_24),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = data.title,
                style = typo().titleSmall,
                maxLines = 1,
                modifier =
                    Modifier
                        .basicMarquee(animationMode = MarqueeAnimationMode.Immediately)
                        .focusable(),
            )
            Text(
                text = data.artists?.toListName()?.connectArtists() ?: "",
                style = typo().bodySmall,
                maxLines = 1,
                modifier =
                    Modifier
                        .basicMarquee(animationMode = MarqueeAnimationMode.Immediately)
                        .focusable(),
            )
        }
        IconButton(onClick = { data.videoId?.let { onMoreClickListener(it) } }) {
            Icon(
                painter = painterResource(R.drawable.more_vert),
                contentDescription = null,
                tint = Color.White,
            )
        }
    }
}

@Composable
fun ItemArtistChart(
    onClick: () -> Unit,
    data: ArtistItemCompat,
    widthDp: Dp = 120.dp,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier
                .width(120.dp)
                .clickable { onClick() }
                .padding(8.dp),
    ) {
        val thumb = data.thumbnails?.lastOrNull()?.url
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(thumb)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .size(100.dp)
                    .clip(CircleShape),
            placeholder = painterResource(R.drawable.baseline_people_alt_24),
            error = painterResource(R.drawable.baseline_people_alt_24),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = data.name,
            style = typo().bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun MoodMomentAndGenreHomeItem(
    title: String,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .padding(4.dp)
                .width(160.dp)
                .height(60.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.DarkGray.copy(alpha = 0.5f))
                .clickable { onClick() },
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.White),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = typo().labelSmall,
                color = white,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun IconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    androidx.compose.material3.IconButton(
        onClick = onClick,
        modifier = modifier,
        content = content,
    )
}
