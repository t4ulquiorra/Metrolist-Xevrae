package com.metrolist.music.ui.screens.xevrae.other

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.metrolist.music.common.Config
import com.metrolist.music.models.xevrae.Track
import com.metrolist.music.models.xevrae.Content
import com.metrolist.music.models.xevrae.Artist
import com.metrolist.music.domain.mediaservice.handler.PlaylistType
import com.metrolist.music.domain.mediaservice.handler.QueueData
import com.metrolist.music.models.xevrae.toSongEntity
import com.metrolist.music.models.xevrae.toTrackCompat
import com.metrolist.music.models.xevrae.toSongItem
import com.metrolist.music.models.xevrae.toTrack
import com.metrolist.music.ui.utils.pressClickable
import com.metrolist.music.expect.ui.MediaPlayerView
import com.metrolist.music.extensions.getStringBlocking
import com.metrolist.music.extensions.rgbFactor
import com.metrolist.music.ui.component.CenterLoadingBox
import com.metrolist.music.ui.component.CollapsingToolbarParallaxEffect
import com.metrolist.music.ui.component.DescriptionView
import com.metrolist.music.ui.component.EndOfPage
import com.metrolist.music.ui.component.ArtistFullWidthItems
import com.metrolist.music.ui.component.HomeItemArtist
import com.metrolist.music.ui.component.HomeItemContentPlaylist
import com.metrolist.music.ui.component.HomeItemVideo
import com.metrolist.music.ui.component.LimitedBorderAnimationView
import com.metrolist.music.ui.component.NowPlayingBottomSheet
import com.metrolist.music.ui.component.SongFullWidthItems
import com.metrolist.music.ui.navigation.xevrae.destination.list.AlbumDestination
import com.metrolist.music.ui.navigation.xevrae.destination.list.ArtistDestination
import com.metrolist.music.ui.navigation.xevrae.destination.list.MoreAlbumsDestination
import com.metrolist.music.ui.navigation.xevrae.destination.list.PlaylistDestination
import com.metrolist.music.ui.theme.xevrae.md_theme_dark_background
import com.metrolist.music.ui.theme.xevrae.typo
import com.metrolist.music.viewmodels.xevrae.ArtistScreenState
import com.metrolist.music.viewmodels.xevrae.ArtistViewModel
import com.metrolist.music.viewmodels.xevrae.SharedViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import androidx.compose.ui.res.stringResource
import com.metrolist.music.extensions.getScreenSizeInfo
import androidx.activity.ComponentActivity
import com.metrolist.music.LocalActivity

@Composable
@ExperimentalMaterial3Api
fun ArtistScreen(
    channelId: String,
    viewModel: ArtistViewModel = hiltViewModel(),
    sharedViewModel: SharedViewModel = hiltViewModel(viewModelStoreOwner = LocalActivity.current as ComponentActivity),
    navController: NavController,
) {
    val screenInfo = getScreenSizeInfo()
    val artistScreenState by viewModel.artistScreenState.collectAsStateWithLifecycle()
    val isFollowed by viewModel.followed.collectAsStateWithLifecycle()
    val canvasUrl by viewModel.canvasUrl.collectAsStateWithLifecycle()

    val playingTrack by sharedViewModel.nowPlayingState.map { it?.track?.id }.collectAsState(null)

    // Choosing song to show Bottom sheet
    var choosingTrack by remember {
        mutableStateOf<com.metrolist.innertube.models.SongItem?>(null)
    }
    var showBottomSheet by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(channelId) {
        if (channelId != artistScreenState.data.channelId) {
            viewModel.browseArtist(channelId)
        }
    }

    Crossfade(artistScreenState) { state ->
        when (state) {
            is ArtistScreenState.Loading -> {
                Box(Modifier.fillMaxSize()) {
                    CenterLoadingBox(
                        Modifier
                            .align(Alignment.Center),
                    )
                }
            }

            is ArtistScreenState.Success -> {
                CollapsingToolbarParallaxEffect(
                    modifier = Modifier.fillMaxSize(),
                    title = state.data.title ?: "",
                    imageUrl = state.data.imageUrl,
                    onBack = {
                        navController.navigateUp()
                    },
                ) { color ->
                    Column {
                        Column(
                            Modifier
                                .padding(horizontal = 20.dp)
                                .padding(top = 16.dp)
                                .padding(bottom = 8.dp),
                        ) {
                            Row {
                                Text(
                                    text = state.data.subscribers ?: stringResource(com.metrolist.music.R.string.unknown),
                                    style = typo().bodySmall,
                                    color = Color.White,
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    text = state.data.playCount ?: stringResource(com.metrolist.music.R.string.unknown),
                                    style = typo().bodySmall,
                                    color = Color.White,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                AnimatedVisibility(canvasUrl != null) {
                                    Row {
                                        val canvas = canvasUrl ?: return@Row
                                        LimitedBorderAnimationView(
                                            isAnimated = true,
                                            brush = Brush.sweepGradient(listOf(Color.Transparent, Color.White)),
                                            backgroundColor = Color.Transparent,
                                            contentPadding = 2.dp,
                                            borderWidth = 1.dp,
                                            shape = RoundedCornerShape(4.dp),
                                            oneCircleDurationMillis = 3000,
                                            interactionNumber = 1,
                                        ) {
                                            MediaPlayerView(
                                                url = canvas.first,
                                                screenSize = screenInfo,
                                                modifier =
                                                    Modifier
                                                        .width(28.dp)
                                                        .height(ButtonDefaults.MinHeight)
                                                        .border(
                                                            width = 0.5.dp,
                                                            color =
                                                                Color.White.copy(
                                                                    alpha = 0.8f,
                                                                ),
                                                            shape = RoundedCornerShape(4.dp),
                                                        ).clip(RoundedCornerShape(4.dp))
                                                        .pressClickable {
                                                                                                                        viewModel.setQueueData(
                                                                QueueData.Data(
                                                                    listTracks = listOf(canvas.second.toTrack().toSongItem()),
                                                                    firstPlayedTrack = canvas.second.toTrack().toSongItem(),
                                                                    playlistId = "RDAMVM${canvas.second.toTrack().videoId}",
                                                                    playlistName = "\"${(state.data.title ?: "")}\" ${
                                                                        getStringBlocking(
                                                                            com.metrolist.music.R.string.popular,
                                                                        )
                                                                    }",
                                                                    playlistType = PlaylistType.RADIO,
                                                                    continuation = null,
                                                                ),
                                                            )
                                                            viewModel.loadMediaItem(
                                                                canvas.second,
                                                                type = Config.SONG_CLICK,
                                                            )
                                                        },
                                            )
                                        }
                                        Spacer(Modifier.width(12.dp))
                                    }
                                }
                                LimitedBorderAnimationView(
                                    isAnimated = !isFollowed,
                                    brush = Brush.sweepGradient(listOf(Color.Gray, Color.White)),
                                    backgroundColor = Color.Transparent,
                                    contentPadding = 0.dp,
                                    borderWidth = 2.dp,
                                    shape = ButtonDefaults.outlinedShape,
                                    oneCircleDurationMillis = 3000,
                                    interactionNumber = 1,
                                ) {
                                    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.updateFollowed(
                                                    if (isFollowed) 0 else 1,
                                                    state.data.channelId ?: return@OutlinedButton,
                                                )
                                            },
                                            colors =
                                                ButtonDefaults.outlinedButtonColors().copy(
                                                    contentColor = Color.White,
                                                    containerColor = Color.Transparent,
                                                ),
                                        ) {
                                            if (isFollowed) {
                                                Text(text = stringResource(com.metrolist.music.R.string.followed), color = Color.White)
                                            } else {
                                                Text(text = stringResource(com.metrolist.music.R.string.follow), color = Color.White)
                                            }
                                        }
                                    }
                                }
                                Spacer(Modifier.width(4.dp))
                                IconButton(
                                    onClick = {
                                        if (state.data.shuffleParam != null) {
                                            viewModel.onShuffleClick(state.data.shuffleParam)
                                        } else {
                                            viewModel.makeToast(getStringBlocking(com.metrolist.music.R.string.error))
                                        }
                                    },
                                ) {
                                    Icon(Icons.Outlined.Shuffle, "Shuffle")
                                }
                                Spacer(Modifier.weight(1f))
                                TextButton(
                                    onClick = {
                                        if (state.data.radioParam != null) {
                                            viewModel.onRadioClick(state.data.radioParam)
                                        } else {
                                            viewModel.makeToast(getStringBlocking(com.metrolist.music.R.string.error))
                                        }
                                    },
                                    colors =
                                        ButtonDefaults
                                            .textButtonColors()
                                            .copy(
                                                contentColor = Color.White,
                                            ),
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(Icons.Outlined.Sensors, "")
                                        if (canvasUrl == null) {
                                            Spacer(Modifier.width(6.dp))
                                            Text(text = stringResource(com.metrolist.music.R.string.start_radio))
                                        }
                                    }
                                }
                            }
                        }

                        // Popular Songs
                        AnimatedVisibility(state.data.popularSongs.isNotEmpty()) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 20.dp),
                                ) {
                                    Text(
                                        text = stringResource(com.metrolist.music.R.string.popular),
                                        style = typo().labelMedium,
                                        color = Color.White,
                                        modifier = Modifier.weight(1f),
                                    )
                                    TextButton(
                                        onClick = {
                                            val id = state.data.listSongParam
                                            if (id != null) {
                                                navController.navigate(PlaylistDestination(id))
                                            } else {
                                                viewModel.makeToast(getStringBlocking(com.metrolist.music.R.string.error))
                                            }
                                        },
                                        colors =
                                            ButtonDefaults
                                                .textButtonColors()
                                                .copy(
                                                    contentColor = Color.White,
                                                ),
                                    ) {
                                        Text(stringResource(com.metrolist.music.R.string.more), style = typo().bodySmall)
                                    }
                                }
                                state.data.popularSongs.forEach { song ->
                                    SongFullWidthItems(
                                        songEntity = song.toSongEntity(),
                                        isPlaying = song.id == playingTrack,
                                        modifier = Modifier.fillMaxWidth(),
                                        onMoreClickListener = {
                                            choosingTrack = song
                                            showBottomSheet = true
                                        },
                                        onClickListener = {
                                            viewModel.setQueueData(
                                                QueueData.Data(
                                                    listTracks = listOf(song),
                                                    firstPlayedTrack = song,
                                                    playlistId = "RDAMVM${song.id}",
                                                    playlistName = "\"${state.data.title ?: ""}\" ${getStringBlocking(com.metrolist.music.R.string.popular)}",
                                                    playlistType = PlaylistType.RADIO,
                                                    continuation = null,
                                                ),
                                            )
                                            viewModel.loadMediaItem(
                                                song,
                                                type = Config.SONG_CLICK,
                                            )
                                        },
                                        onAddToQueue = {
                                            sharedViewModel.addListToQueue(
                                                arrayListOf(song.toTrackCompat()),
                                            )
                                        },
                                    )
                                }
                            }
                        }

                        // Singles
                        AnimatedVisibility(
                            state.data.singles != null &&
                                state.data.singles.results
                                    .isNotEmpty(),
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 20.dp),
                                ) {
                                    Text(
                                        text = stringResource(com.metrolist.music.R.string.singles),
                                        style = typo().labelMedium,
                                        color = Color.White,
                                        modifier = Modifier.weight(1f),
                                    )
                                    TextButton(
                                        onClick = {
                                            if (state.data.channelId != null) {
                                                val id = "MPAD${state.data.channelId}"
                                                navController.navigate(
                                                    MoreAlbumsDestination(
                                                        id = id,
                                                        type = MoreAlbumsDestination.SINGLE_TYPE,
                                                    ),
                                                )
                                            } else {
                                                viewModel.makeToast(getStringBlocking(com.metrolist.music.R.string.error))
                                            }
                                        },
                                        colors =
                                            ButtonDefaults
                                                .textButtonColors()
                                                .copy(
                                                    contentColor = Color.White,
                                                ),
                                    ) {
                                        Text(stringResource(com.metrolist.music.R.string.more), style = typo().bodySmall)
                                    }
                                }
                                LazyRow(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    item {
                                        Spacer(Modifier.size(10.dp))
                                    }
                                    items(state.data.singles?.results ?: emptyList()) { single ->
                                        HomeItemContentPlaylist(
                                            onClick = {
                                                navController.navigate(
                                                    AlbumDestination(
                                                        single.browseId,
                                                    ),
                                                )
                                            },
                                            data = single,
                                            thumbSize = 180.dp,
                                        )
                                    }
                                    item {
                                        Spacer(Modifier.size(10.dp))
                                    }
                                }
                            }
                        }

                        // Albums
                        AnimatedVisibility(
                            state.data.albums != null &&
                                state.data.albums.results
                                    .isNotEmpty(),
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 20.dp),
                                ) {
                                    Text(
                                        text = stringResource(com.metrolist.music.R.string.albums),
                                        style = typo().labelMedium,
                                        color = Color.White,
                                        modifier = Modifier.weight(1f),
                                    )
                                    TextButton(
                                        onClick = {
                                            if (state.data.channelId != null) {
                                                val id = "MPAD${state.data.channelId}"
                                                navController.navigate(
                                                    MoreAlbumsDestination(
                                                        id = id,
                                                        type = MoreAlbumsDestination.ALBUM_TYPE,
                                                    ),
                                                )
                                            } else {
                                                viewModel.makeToast(getStringBlocking(com.metrolist.music.R.string.error))
                                            }
                                        },
                                        colors =
                                            ButtonDefaults
                                                .textButtonColors()
                                                .copy(
                                                    contentColor = Color.White,
                                                ),
                                    ) {
                                        Text(stringResource(com.metrolist.music.R.string.more), style = typo().bodySmall)
                                    }
                                }
                                LazyRow(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    item {
                                        Spacer(Modifier.size(10.dp))
                                    }
                                    items(state.data.albums?.results ?: emptyList()) { album ->
                                        HomeItemContentPlaylist(
                                            onClick = {
                                                navController.navigate(
                                                    AlbumDestination(
                                                        browseId = album.browseId,
                                                    ),
                                                )
                                            },
                                            data = album,
                                            thumbSize = 180.dp,
                                        )
                                    }
                                    item {
                                        Spacer(Modifier.size(10.dp))
                                    }
                                }
                            }
                        }

                        // Videos
                        AnimatedVisibility(
                            state.data.video != null &&
                                state.data.video.results
                                    .isNotEmpty(),
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 20.dp),
                                ) {
                                    Text(
                                        text = stringResource(com.metrolist.music.R.string.videos),
                                        style = typo().labelMedium,
                                        color = Color.White,
                                        modifier = Modifier.weight(1f),
                                    )
                                    TextButton(
                                        onClick = {
                                            val videoListParam = state.data.video?.params
                                            if (videoListParam != null) {
                                                navController.navigate(
                                                    PlaylistDestination(
                                                        videoListParam,
                                                    ),
                                                )
                                            } else {
                                                viewModel.makeToast(getStringBlocking(com.metrolist.music.R.string.error))
                                            }
                                        },
                                        colors =
                                            ButtonDefaults
                                                .textButtonColors()
                                                .copy(
                                                    contentColor = Color.White,
                                                ),
                                    ) {
                                        Text(stringResource(com.metrolist.music.R.string.more), style = typo().bodySmall)
                                    }
                                }
                                LazyRow(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    item {
                                        Spacer(Modifier.size(10.dp))
                                    }
                                    items(state.data.video?.results ?: emptyList()) { video ->
                                        HomeItemVideo(
                                            onClick = {
                                                                                                viewModel.setQueueData(
                                                    QueueData.Data(
                                                        listTracks = listOf(video),
                                                        firstPlayedTrack = video,
                                                        playlistId = "RDAMVM${video.id}",
                                                        playlistName = (state.data.title ?: "") + getStringBlocking(com.metrolist.music.R.string.videos),
                                                        playlistType = PlaylistType.RADIO,
                                                        continuation = null,
                                                    ),
                                                )
                                                viewModel.loadMediaItem(
                                                    video,
                                                    type = Config.VIDEO_CLICK,
                                                )
                                            },
                                            onLongClick = {
                                                choosingTrack = video
                                            showBottomSheet = true
                                            },
                                            data =
                                                Content(
                                                    album = null,
                                                    artists = video.artists.map { com.metrolist.music.models.xevrae.Artist(it.name, it.id ?: "") },
                                                    description = null,
                                                    isExplicit = video.explicit,
                                                    playlistId = null,
                                                    browseId = null,
                                                    thumbnails = listOf(com.metrolist.music.models.xevrae.Thumbnail(video.thumbnail)),
                                                    title = video.title,
                                                    videoId = video.id,
                                                    views = null,
                                                ),
                                        )
                                    }
                                    item {
                                        Spacer(Modifier.size(10.dp))
                                    }
                                }
                            }
                        }

                        // Feature on
                        AnimatedVisibility(state.data.featuredOn.isNotEmpty()) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 20.dp),
                                ) {
                                    Text(
                                        text = stringResource(com.metrolist.music.R.string.featured_inArtist),
                                        style = typo().labelMedium,
                                        color = Color.White,
                                        modifier =
                                            Modifier
                                                .weight(1f)
                                                .padding(vertical = 10.dp),
                                    )
                                }
                                LazyRow(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    item {
                                        Spacer(Modifier.size(10.dp))
                                    }
                                    items(state.data.featuredOn) { feature ->
                                        HomeItemContentPlaylist(
                                            onClick = {
                                                navController.navigate(
                                                    PlaylistDestination(
                                                        feature.id,
                                                    ),
                                                )
                                            },
                                            data = feature,
                                            thumbSize = 180.dp,
                                        )
                                    }
                                    item {
                                        Spacer(Modifier.size(10.dp))
                                    }
                                }
                            }
                        }

                        // Related
                        AnimatedVisibility(
                            state.data.related != null &&
                                state.data.related.results
                                    .isNotEmpty(),
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 20.dp),
                                ) {
                                    Text(
                                        text = stringResource(com.metrolist.music.R.string.related_artists),
                                        style = typo().labelMedium,
                                        color = Color.White,
                                        modifier =
                                            Modifier
                                                .weight(1f)
                                                .padding(vertical = 10.dp),
                                    )
                                }
                                LazyRow(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    item {
                                        Spacer(Modifier.size(10.dp))
                                    }
                                    items(state.data.related?.results ?: emptyList()) { related ->
                                        ArtistFullWidthItems(
                                            onClickListener = {
                                                navController.navigate(
                                                    ArtistDestination(
                                                        channelId = (related as? com.metrolist.innertube.models.ArtistItem)?.id ?: "",
                                                    ),
                                                )
                                            },
                                            data = com.metrolist.music.db.entities.ArtistEntity(
                                                id = (related as? com.metrolist.innertube.models.ArtistItem)?.id ?: "",
                                                name = (related as? com.metrolist.innertube.models.ArtistItem)?.title ?: "",
                                                thumbnailUrl = (related as? com.metrolist.innertube.models.ArtistItem)?.thumbnail,
                                                channelId = (related as? com.metrolist.innertube.models.ArtistItem)?.id,
                                            ),
                                        )
                                    }
                                    item {
                                        Spacer(Modifier.size(10.dp))
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 20.dp),
                        ) {
                            Text(
                                text = stringResource(com.metrolist.music.R.string.description),
                                style = typo().labelMedium,
                                color = Color.White,
                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .padding(vertical = 12.dp),
                            )
                        }
                        val urlHandler = LocalUriHandler.current
                        ElevatedCard(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors =
                                CardDefaults.elevatedCardColors().copy(
                                    containerColor = color.rgbFactor(0.5f),
                                ),
                        ) {
                            DescriptionView(
                                modifier = Modifier.padding(16.dp),
                                text = state.data.description ?: stringResource(com.metrolist.music.R.string.no_description),
                                onTimeClicked = {},
                                onURLClicked = { url ->
                                    urlHandler.openUri(url)
                                },
                            )
                        }
                        EndOfPage()
                    }
                    if (showBottomSheet && choosingTrack != null) {
                        NowPlayingBottomSheet(
                            onDismiss = {
                                showBottomSheet = false
                                choosingTrack = null
                            },
                            navController = navController,
                            song = choosingTrack?.toSongEntity(),
                        )
                    }
                }
            }

            is ArtistScreenState.Error -> {
                viewModel.makeToast(state.message ?: stringResource(com.metrolist.music.R.string.error))
                navController.navigateUp()
            }
        }
    }
}
