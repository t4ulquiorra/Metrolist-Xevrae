package com.metrolist.music.ui.screens.xevrae.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.metrolist.music.models.xevrae.LibraryChipType
import com.metrolist.music.utils.LocalResource
import com.metrolist.music.utils.Logger
import com.metrolist.music.extensions.isScrollingUp
import com.metrolist.music.ui.component.Chip
import com.metrolist.music.ui.component.EndOfPage
import com.metrolist.music.ui.component.GridLibraryPlaylist
import com.metrolist.music.ui.component.LibraryItem
import com.metrolist.music.ui.component.LibraryItemState
import com.metrolist.music.ui.component.LibraryItemType
import com.metrolist.music.ui.component.LibraryTilingBox
import com.metrolist.music.ui.navigation.xevrae.destination.home.AnalyticsDestination
import com.metrolist.music.ui.theme.xevrae.transparent
import com.metrolist.music.ui.theme.xevrae.typo
import com.metrolist.music.viewmodels.LibraryViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
@Composable
fun LibraryScreen(
    innerPadding: PaddingValues,
    viewModel: LibraryViewModel = hiltViewModel(),
    navController: NavController,
    onScrolling: (onTop: Boolean) -> Unit = {},
) {
    val density = LocalDensity.current

    val loggedIn by viewModel.youtubeLoggedIn.collectAsStateWithLifecycle(initialValue = false)
    val nowPlaying by viewModel.nowPlayingVideoId.collectAsStateWithLifecycle()
    val youTubePlaylist by viewModel.youTubePlaylist.collectAsStateWithLifecycle()
    val youTubeMixForYou by viewModel.youTubeMixForYou.collectAsStateWithLifecycle()
    val listCanvasSong by viewModel.listCanvasSong.collectAsStateWithLifecycle()
    val yourLocalPlaylist by viewModel.yourLocalPlaylist.collectAsStateWithLifecycle()
    val favoritePlaylist by viewModel.favoritePlaylist.collectAsStateWithLifecycle()
    val downloadedPlaylist by viewModel.downloadedPlaylist.collectAsStateWithLifecycle()
    val favoritePodcasts by viewModel.favoritePodcasts.collectAsStateWithLifecycle()
    val chartPlaylists by viewModel.chartPlaylists.collectAsStateWithLifecycle()
    val recentlyAdded by viewModel.recentlyAdded.collectAsStateWithLifecycle()
    val accountThumbnail by viewModel.accountThumbnail.collectAsStateWithLifecycle()

    val playlistNameCannotBeEmpty = stringResource(com.metrolist.music.R.string.playlist_name_cannot_be_empty)
    val libraryStr = stringResource(com.metrolist.music.R.string.library)

    val hazeState =
        remember { HazeState() }

    var topAppBarHeight by remember {
        mutableStateOf(0.dp)
    }
    var showAddSheet by remember { mutableStateOf(false) }

    LaunchedEffect(nowPlaying) {
        Logger.w("LibraryScreen", "Check nowPlaying: $nowPlaying")
        viewModel.getRecentlyAdded()
    }

    val chipRowState = rememberScrollState()
    val currentFilter by viewModel.currentScreen.collectAsStateWithLifecycle()

    LaunchedEffect(currentFilter) {
        when (currentFilter) {
            LibraryChipType.YOUTUBE_MUSIC_PLAYLIST -> {
                if (youTubePlaylist.data.isNullOrEmpty()) {
                    viewModel.getYouTubePlaylist()
                }
            }

            LibraryChipType.YOUTUBE_MIX_FOR_YOU -> {
                if (youTubeMixForYou.data.isNullOrEmpty()) {
                    viewModel.getYouTubeMixedForYou()
                }
            }

            LibraryChipType.YOUR_LIBRARY -> {
                viewModel.getCanvasSong()
                viewModel.getRecentlyAdded()
            }

            LibraryChipType.LOCAL_PLAYLIST -> {
                viewModel.getLocalPlaylist()
            }

            LibraryChipType.FAVORITE_PLAYLIST -> {
                viewModel.getPlaylistFavorite()
            }

            LibraryChipType.DOWNLOADED_PLAYLIST -> {
                viewModel.getDownloadedPlaylist()
            }

            LibraryChipType.FAVORITE_PODCAST -> {
                viewModel.getFavoritePodcasts()
            }

            LibraryChipType.CHART -> {
                if (chartPlaylists.data.isNullOrEmpty()) {
                    viewModel.getChartPlaylists()
                }
            }
        }
    }

    Crossfade(
        modifier = Modifier.haze(hazeState),
        targetState = currentFilter,
    ) { filter ->
        when (filter) {
            LibraryChipType.YOUR_LIBRARY -> {
                val state = rememberLazyListState()
                val isScrollingUp by state.isScrollingUp()
                LaunchedEffect(state) {
                    snapshotFlow { state.firstVisibleItemIndex }
                        .collect {
                            if (it <= 1) {
                                onScrolling.invoke(true)
                            } else {
                                onScrolling.invoke(isScrollingUp)
                            }
                        }
                }
                LazyColumn(
                    contentPadding =
                        PaddingValues(top = innerPadding.calculateTopPadding() + topAppBarHeight,
                        , bottom = innerPadding.calculateBottomPadding()),
                    state = state,
                ) {
                    item {
                        LibraryTilingBox(navController)
                    }

                    if (!listCanvasSong.data.isNullOrEmpty()) {
                        item {
                            LibraryItem(
                                state =
                                    LibraryItemState(
                                        type = LibraryItemType.CanvasSong,
                                        data = (listCanvasSong.data ?: emptyList<com.metrolist.music.models.xevrae.LibraryType>()) as List<com.metrolist.music.models.xevrae.LibraryType>,
                                        isLoading = listCanvasSong is LocalResource.Loading,
                                    ),
                                navController = navController,
                            )
                        }
                    }

                    item {
                        LibraryItem(
                            state =
                                LibraryItemState(
                                    type =
                                        LibraryItemType.RecentlyAdded(
                                            playingVideoId = nowPlaying,
                                        ),
                                    data = recentlyAdded.data ?: emptyList(),
                                    isLoading = recentlyAdded is LocalResource.Loading,
                                ),
                            navController = navController,
                        )
                    }
                    item {
                        EndOfPage()
                    }
                }
            }

            LibraryChipType.YOUTUBE_MUSIC_PLAYLIST -> {
                GridLibraryPlaylist(
                    navController,
                    PaddingValues(top = innerPadding.calculateTopPadding() + topAppBarHeight, bottom = innerPadding.calculateBottomPadding()),
                    youTubePlaylist,
                    emptyText = com.metrolist.music.R.string.no_YouTube_playlists,
                    onScrolling = onScrolling,
                ) {
                    viewModel.getYouTubePlaylist()
                }
            }

            LibraryChipType.YOUTUBE_MIX_FOR_YOU -> {
                GridLibraryPlaylist(
                    navController,
                    PaddingValues(top = innerPadding.calculateTopPadding() + topAppBarHeight, bottom = innerPadding.calculateBottomPadding()),
                    youTubeMixForYou,
                    emptyText = com.metrolist.music.R.string.no_mixes_found,
                    onScrolling = onScrolling,
                ) {
                    viewModel.getYouTubeMixedForYou()
                }
            }

            LibraryChipType.LOCAL_PLAYLIST -> {
                GridLibraryPlaylist(
                    navController,
                    PaddingValues(top = innerPadding.calculateTopPadding() + topAppBarHeight, bottom = innerPadding.calculateBottomPadding()),
                    yourLocalPlaylist,
                    onScrolling = onScrolling,
                    emptyText = com.metrolist.music.R.string.no_playlists_added,
                    createNewPlaylist = {
                        showAddSheet = true
                    },
                ) {
                    viewModel.getLocalPlaylist()
                }
            }

            LibraryChipType.FAVORITE_PLAYLIST -> {
                GridLibraryPlaylist(
                    navController,
                    PaddingValues(top = innerPadding.calculateTopPadding() + topAppBarHeight, bottom = innerPadding.calculateBottomPadding()),
                    favoritePlaylist,
                    emptyText = com.metrolist.music.R.string.no_favorite_playlists,
                    onScrolling = onScrolling,
                ) {
                    viewModel.getPlaylistFavorite()
                }
            }

            LibraryChipType.DOWNLOADED_PLAYLIST -> {
                GridLibraryPlaylist(
                    navController,
                    PaddingValues(top = innerPadding.calculateTopPadding() + topAppBarHeight, bottom = innerPadding.calculateBottomPadding()),
                    downloadedPlaylist,
                    emptyText = com.metrolist.music.R.string.no_playlists_downloaded,
                    onScrolling = onScrolling,
                ) {
                    viewModel.getDownloadedPlaylist()
                }
            }

            LibraryChipType.FAVORITE_PODCAST -> {
                GridLibraryPlaylist(
                    navController,
                    PaddingValues(top = innerPadding.calculateTopPadding() + topAppBarHeight, bottom = innerPadding.calculateBottomPadding()),
                    favoritePodcasts,
                    emptyText = com.metrolist.music.R.string.no_favorite_podcasts,
                    onScrolling = onScrolling,
                ) {
                    viewModel.getFavoritePodcasts()
                }
            }

            LibraryChipType.CHART -> {
                GridLibraryPlaylist(
                    navController,
                    PaddingValues(top = innerPadding.calculateTopPadding() + topAppBarHeight, bottom = innerPadding.calculateBottomPadding()),
                    chartPlaylists,
                    emptyText = com.metrolist.music.R.string.no_charts_found,
                    onScrolling = onScrolling,
                ) {
                    viewModel.getChartPlaylists()
                }
            }
        }
    }
    val coroutineScope = rememberCoroutineScope()
    if (showAddSheet) {
        var newTitle by remember { mutableStateOf("") }
        val showAddSheetState =
            rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
            )
        val hideEditTitleBottomSheet: () -> Unit =
            {
                coroutineScope.launch {
                    showAddSheetState.hide()
                    showAddSheet = false
                }
            }
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = showAddSheetState,
            containerColor = Color.Transparent,
            contentColor = Color.Transparent,
            dragHandle = null,
            scrimColor = Color(0xFF121212).copy(alpha = .5f),
        ) {
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                colors = CardDefaults.cardColors().copy(containerColor = Color(0xFF242424)),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.height(5.dp))
                    Card(
                        modifier =
                            Modifier
                                .width(60.dp)
                                .height(4.dp),
                        colors =
                            CardDefaults.cardColors().copy(
                                containerColor = Color(0xFF474545),
                            ),
                        shape = RoundedCornerShape(50),
                    ) {}
                    Spacer(modifier = Modifier.height(5.dp))
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { s -> newTitle = s },
                        label = {
                            Text(text = stringResource(com.metrolist.music.R.string.playlist_name))
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    TextButton(
                        onClick = {
                            if (newTitle.isBlank()) {
                                viewModel.makeToast(playlistNameCannotBeEmpty)
                            } else {
                                viewModel.createPlaylist(newTitle)
                                hideEditTitleBottomSheet()
                            }
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally),
                    ) {
                        Text(text = stringResource(com.metrolist.music.R.string.create))
                    }
                }
            }
        }
    }
    Column(
        Modifier
            .background(transparent)
            .hazeChild(hazeState, style = HazeMaterials.ultraThin())
            .onGloballyPositioned { coordinates ->
                topAppBarHeight = with(density) { coordinates.size.height.toDp() }
            },
    ) {
        TopAppBar(
            title = {
                Text(
                    text = libraryStr,
                    style = typo().titleMedium,
                    color = Color.White,
                )
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),

            actions = {
                IconButton(
                    onClick = {
                        navController.navigate(AnalyticsDestination)
                    },
                ) {
                    Box {
                        Icon(Icons.Rounded.AutoGraph, "Analytics", tint = Color.White)
                        Text(
                            "NEW",
                            Modifier.align(Alignment.BottomEnd),
                            style =
                                typo().bodySmall.copy(
                                    fontSize = 5.sp,
                                ),
                        )
                    }
                }
            },
            navigationIcon = {
                AnimatedVisibility(
                    !accountThumbnail.isNullOrEmpty(),
                    modifier = Modifier.padding(horizontal = 12.dp),
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    AsyncImage(
                        model =
                            ImageRequest
                                .Builder(LocalPlatformContext.current)
                                .data(accountThumbnail)
                                .crossfade(550)
                                .build(),
                        placeholder = painterResource(com.metrolist.music.R.drawable.baseline_people_alt_24),
                        error = painterResource(com.metrolist.music.R.drawable.baseline_people_alt_24),
                        contentDescription = null,
                        modifier =
                            Modifier
                                .size(26.dp)
                                .clip(CircleShape),
                    )
                }
            },
        )
        Row(
            modifier =
                Modifier
                    .horizontalScroll(chipRowState)
                    .padding(horizontal = 15.dp)
                    .padding(bottom = 8.dp)
                    .background(Color.Transparent),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            LibraryChipType.entries.forEach { type ->
                if ((type == LibraryChipType.YOUTUBE_MUSIC_PLAYLIST || type == LibraryChipType.YOUTUBE_MIX_FOR_YOU) && !loggedIn) {
                    return@forEach
                }
                Chip(
                    isAnimated = false,
                    isSelected = type == currentFilter,
                    text =
                        when (type) {
                            LibraryChipType.YOUR_LIBRARY -> stringResource(com.metrolist.music.R.string.your_library)
                            LibraryChipType.YOUTUBE_MUSIC_PLAYLIST -> stringResource(com.metrolist.music.R.string.your_youtube_playlists)
                            LibraryChipType.YOUTUBE_MIX_FOR_YOU -> stringResource(com.metrolist.music.R.string.mix_for_you)
                            LibraryChipType.LOCAL_PLAYLIST -> stringResource(com.metrolist.music.R.string.your_playlists)
                            LibraryChipType.FAVORITE_PLAYLIST -> stringResource(com.metrolist.music.R.string.favorite_playlists)
                            LibraryChipType.DOWNLOADED_PLAYLIST -> stringResource(com.metrolist.music.R.string.downloaded_playlists)
                            LibraryChipType.FAVORITE_PODCAST -> stringResource(com.metrolist.music.R.string.favorite_podcasts)
                            LibraryChipType.CHART -> stringResource(com.metrolist.music.R.string.xevrae_charts)
                        },
                ) {
                    viewModel.setCurrentScreen(type)
                }
            }
        }
    }
}