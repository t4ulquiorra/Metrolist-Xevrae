package com.metrolist.music.ui.component

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.metrolist.music.db.entities.AlbumEntity
import com.metrolist.music.db.entities.PlaylistEntity
import com.metrolist.music.db.entities.SongEntity
import com.metrolist.music.models.xevrae.LibraryType
import com.metrolist.music.models.xevrae.PlaylistType
import com.metrolist.music.models.xevrae.PlaylistsResult
import com.metrolist.music.models.xevrae.RecentlyType
import com.metrolist.music.models.xevrae.connectArtists
import com.metrolist.music.ui.navigation.xevrae.destination.list.AlbumDestination
import com.metrolist.music.ui.navigation.xevrae.destination.list.ArtistDestination
import com.metrolist.music.ui.navigation.xevrae.destination.list.LocalPlaylistDestination
import com.metrolist.music.ui.navigation.xevrae.destination.list.PlaylistDestination
import com.metrolist.music.ui.navigation.xevrae.destination.list.PodcastDestination
import com.metrolist.music.ui.theme.xevrae.typo
import com.metrolist.music.ui.utils.pressClickable
import com.metrolist.music.viewmodels.LibraryViewModel
import com.metrolist.music.viewmodels.xevrae.SharedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryItem(
    state: LibraryItemState,
    viewModel: LibraryViewModel = hiltViewModel(),
    navController: NavController,
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedSong by remember { mutableStateOf<SongEntity?>(null) }
    
    val title =
        when (state.type) {
            is LibraryItemType.RecentlyAdded -> stringResource(com.metrolist.music.R.string.recently_added)
            is LibraryItemType.CanvasSong -> stringResource(com.metrolist.music.R.string.most_played)
            else -> ""
        }
    
    if (title.isEmpty()) return

    Box {
        // TODO: Implement NowPlayingBottomSheet or use existing Metrolist menu
        /*
        if (showBottomSheet) {
            NowPlayingBottomSheet(...)
        }
        */
        
        Column {
            Row(
                modifier = Modifier.padding(top = 15.dp, start = 10.dp, end = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = typo().headlineMedium,
                    color = Color.White,
                    maxLines = 1,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(35.dp)
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .weight(1f)
                            .basicMarquee(
                                iterations = Int.MAX_VALUE,
                                animationMode = MarqueeAnimationMode.Immediately,
                            ).focusable(),
                )
            }
            Crossfade(targetState = state.isLoading, label = "Loading") { isLoading ->
                if (!isLoading) {
                    if (state.type is LibraryItemType.RecentlyAdded) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            state.data.filterIsInstance<RecentlyType>().forEach { item ->
                                when (item.objectType()) {
                                    RecentlyType.Type.SONG -> {
                                        val song = item as SongEntity
                                        SongFullWidthItems(
                                            songEntity = song,
                                            isPlaying = song.id == state.type.playingVideoId,
                                            onMoreClickListener = {
                                                selectedSong = song
                                                showBottomSheet = true
                                            },
                                            onClickListener = {
                                                // TODO: Handle song click (play)
                                            }
                                        )
                                    }

                                    RecentlyType.Type.ARTIST -> {
                                        val artist = item as? com.metrolist.music.db.entities.ArtistEntity ?: return@forEach
                                        ArtistFullWidthItems(
                                            data = artist,
                                            onClickListener = {
                                                navController.navigate(
                                                    ArtistDestination(
                                                        channelId = artist.channelId ?: "",
                                                    ),
                                                )
                                            },
                                        )
                                    }

                                    else -> {
                                        if (item is PlaylistType) {
                                            PlaylistFullWidthItems(
                                                data = item,
                                                onClickListener = {
                                                    when (item) {
                                                        is AlbumEntity -> {
                                                            navController.navigate(
                                                                AlbumDestination(
                                                                    item.id,
                                                                ),
                                                            )
                                                        }

                                                        is PlaylistEntity -> {
                                                            navController.navigate(
                                                                PlaylistDestination(
                                                                    item.id,
                                                                ),
                                                            )
                                                        }
                                                    }
                                                },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else if (state.type is LibraryItemType.CanvasSong) {
                        LazyRow(
                            Modifier.padding(
                                top = 10.dp,
                            ),
                        ) {
                            items(state.data) { item ->
                                val song = item as? SongEntity ?: return@items
                                Box(
                                    Modifier
                                        .padding(horizontal = 10.dp)
                                        .height(300.dp)
                                        .width(170.dp)
                                        .pressClickable {
                                            // TODO: Handle canvas song click
                                        },
                                ) {
                                    AsyncImage(
                                        model =
                                            ImageRequest
                                                .Builder(LocalContext.current)
                                                .data(song.thumbnailUrl)
                                                .diskCachePolicy(CachePolicy.ENABLED)
                                                .diskCacheKey(song.thumbnailUrl)
                                                .crossfade(true)
                                                .build(),
                                        placeholder = ColorPainter(Color(0xFF2A2A2A)),
                                        error = ColorPainter(Color(0xFF2A2A2A)),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier =
                                            Modifier
                                                .fillMaxSize()
                                                .clip(
                                                    RoundedCornerShape(8.dp),
                                                ),
                                    )
                                    Column(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp)
                                            .align(Alignment.BottomStart),
                                    ) {
                                        Text(
                                            text = song.title,
                                            style = typo().labelSmall,
                                            color = Color.White,
                                            maxLines = 1,
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .wrapContentHeight(
                                                        align = Alignment.CenterVertically,
                                                    ).basicMarquee(
                                                        iterations = Int.MAX_VALUE,
                                                        animationMode = MarqueeAnimationMode.Immediately,
                                                    ).focusable(),
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (song.explicit) {
                                                ExplicitBadge(
                                                    modifier =
                                                        Modifier
                                                            .size(20.dp)
                                                            .padding(end = 4.dp),
                                                )
                                            }
                                            Text(
                                                text = (""),
                                                style = typo().bodySmall,
                                                maxLines = 1,
                                                modifier =
                                                    Modifier
                                                        .weight(1f)
                                                        .wrapContentHeight(
                                                            align = Alignment.CenterVertically,
                                                        ).basicMarquee(
                                                            iterations = Int.MAX_VALUE,
                                                            animationMode = MarqueeAnimationMode.Immediately,
                                                        ).focusable(),
                                            )
                                        }
                                        Spacer(Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                    } else {
                        if (state.data.isNotEmpty()) {
                            LazyRow {
                                items(items = state.data) { item ->
                                    HomeItemContentPlaylist(
                                        onClick = {
                                            when (item) {
                                                is PlaylistEntity -> {
                                                    navController.navigate(
                                                        LocalPlaylistDestination(
                                                            item.id,
                                                        ),
                                                    )
                                                }

                                                is PlaylistsResult -> {
                                                    navController.navigate(
                                                        PlaylistDestination(
                                                            item.id,
                                                            isYourYouTubePlaylist = true,
                                                        ),
                                                    )
                                                }

                                                is AlbumEntity -> {
                                                    navController.navigate(
                                                        AlbumDestination(
                                                            item.id,
                                                        ),
                                                    )
                                                }
                                            }
                                        },
                                        data = item as? PlaylistType ?: return@items,
                                        thumbSize = 125.dp,
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(130.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                // TODO: Add no content text if needed
                                // Text(..., style = typo().bodyMedium)
                            }
                        }
                    }
                } else {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CenterLoadingBox(Modifier.wrapContentSize())
                    }
                }
            }
        }
    }
}

sealed class LibraryItemType {
    data object CanvasSong : LibraryItemType()

    data class YouTubePlaylist(
        val isLoggedIn: Boolean,
        val onReload: () -> Unit = {},
    ) : LibraryItemType()

    data class LocalPlaylist(
        val onAddClick: (String) -> Unit,
    ) : LibraryItemType()

    data object FavoritePlaylist : LibraryItemType()

    data object DownloadedPlaylist : LibraryItemType()

    data object FavoritePodcasts : LibraryItemType()

    data class RecentlyAdded(
        val playingVideoId: String,
    ) : LibraryItemType()
}

data class LibraryItemState(
    val type: LibraryItemType,
    val data: List<LibraryType>,
    val isLoading: Boolean = true,
)
