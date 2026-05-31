package com.metrolist.music.ui.screens.xevrae.other

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import com.metrolist.music.expect.pressClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.toBitmap
import com.kmpalette.rememberPaletteState
import com.metrolist.music.models.xevrae.DownloadState
import com.metrolist.music.models.xevrae.Track
import com.metrolist.music.models.xevrae.toSongEntity
import com.metrolist.music.utils.Logger
import com.metrolist.music.expect.ui.drawBackdropCustomShape
import com.metrolist.music.expect.ui.layerBackdrop
import com.metrolist.music.expect.ui.rememberBackdrop
import com.metrolist.music.expect.ui.toImageBitmap
import com.metrolist.music.extensions.angledGradientBackground
import com.metrolist.music.extensions.getColorFromPalette
import com.metrolist.music.extensions.getScreenSizeInfo
import com.metrolist.music.extensions.getStringBlocking
import com.metrolist.music.ui.component.CenterLoadingBox
import com.metrolist.music.ui.component.DescriptionView
import com.metrolist.music.ui.component.EndOfPage
import com.metrolist.music.ui.component.HeartCheckBox
import com.metrolist.music.ui.component.LoadingDialog
import com.metrolist.music.ui.component.NowPlayingBottomSheet
import com.metrolist.music.ui.component.PlaylistBottomSheet
import com.metrolist.music.ui.component.RippleIconButton
import com.metrolist.music.ui.component.SongFullWidthItems
import com.metrolist.music.ui.navigation.xevrae.destination.list.ArtistDestination
import com.metrolist.music.ui.theme.xevrae.md_theme_dark_background
import com.metrolist.music.ui.theme.xevrae.seed
import com.metrolist.music.ui.theme.xevrae.typo
import com.metrolist.music.viewmodels.xevrae.ListState
import com.metrolist.music.viewmodels.xevrae.PlaylistUIEvent
import com.metrolist.music.viewmodels.xevrae.PlaylistUIState
import com.metrolist.music.viewmodels.xevrae.PlaylistViewModel
import com.metrolist.music.viewmodels.xevrae.SharedViewModel
import com.metrolist.music.viewmodels.xevrae.UIEvent
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.activity.ComponentActivity
import com.metrolist.music.LocalActivity
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
@Composable
fun PlaylistScreen(
    viewModel: PlaylistViewModel = hiltViewModel(),
    sharedViewModel: SharedViewModel = hiltViewModel(viewModelStoreOwner = LocalActivity.current as ComponentActivity),
    playlistId: String,
    isYourYouTubePlaylist: Boolean,
    navController: NavController,
) {
    val tag = "PlaylistScreen"

    val composition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            com.metrolist.music.R.readBytes("files/downloading_animation.json").decodeToString(),
        )
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val continuation by viewModel.continuation.collectAsStateWithLifecycle()
    val listColors by viewModel.listColors.collectAsStateWithLifecycle()
    val downloadState by viewModel.downloadState.collectAsStateWithLifecycle()
    val liked by viewModel.liked.collectAsStateWithLifecycle()
    val tracks by viewModel.tracks.collectAsStateWithLifecycle()
    val tracksListState by viewModel.tracksListState.collectAsStateWithLifecycle()

    var showSearchBar by rememberSaveable { mutableStateOf(false) }
    var searchBarHeightPx by remember { mutableStateOf(0) }

    val lazyState = rememberLazyListState()
    val firstItemVisible by remember {
        derivedStateOf {
            lazyState.firstVisibleItemIndex == 0
        }
    }
    var shouldHideTopBar by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }

    val filteredTrack by remember {
        derivedStateOf {
            if (query.isEmpty() || !showSearchBar) {
                tracks
            } else {
                tracks.filter {
                    it.title.contains(query, ignoreCase = true) ||
                        it.artists?.joinToString(", ")?.contains(query, ignoreCase = true) == true
                }
            }
        }
    }

    LaunchedEffect(uiState) {
        Logger.d(tag, "uiState hash: ${uiState.hashCode()}")
        Logger.d(tag, "uiState data: ${uiState.data}")
    }

    LaunchedEffect(showSearchBar) {
        if (showSearchBar) {
            viewModel.getFullTracks {}
            lazyState.animateScrollToItem(0)
        }
    }

    val shouldStartPaginate =
        remember {
            derivedStateOf {
                tracksListState != ListState.PAGINATION_EXHAUST &&
                    (
                        lazyState.layoutInfo.visibleItemsInfo
                            .lastOrNull()
                            ?.index ?: -9
                    ) >= (lazyState.layoutInfo.totalItemsCount - 6)
            }
        }

    LaunchedEffect(key1 = shouldStartPaginate.value) {
        Logger.d(tag, "shouldStartPaginate: ${shouldStartPaginate.value}")
        Logger.d(tag, "tracksListState: $tracksListState")
        Logger.d(tag, "Continuation: $continuation")
        if (shouldStartPaginate.value && tracksListState == ListState.IDLE) {
            viewModel.getContinuationTrack(
                playlistId,
                continuation,
            )
        }
    }

    val queueData by sharedViewModel.getQueueDataState().collectAsStateWithLifecycle()
    val playingPlaylistId by remember {
        derivedStateOf {
            queueData?.data?.playlistId
        }
    }

    val playingTrack by sharedViewModel.nowPlayingState
        .mapLatest {
            it?.songEntity
        }.collectAsState(initial = null)
    val isPlaying by sharedViewModel.controllerState.map { it.isPlaying }.collectAsState(initial = false)

    var currentItem by remember {
        mutableStateOf<Track?>(null)
    }

    var itemBottomSheetShow by remember {
        mutableStateOf(false)
    }
    var playlistBottomSheetShow by remember {
        mutableStateOf(false)
    }

    val onPlaylistItemClick: (videoId: String) -> Unit = { videoId ->
        viewModel.onUIEvent(
            PlaylistUIEvent.ItemClick(
                videoId = videoId,
            ),
        )
    }
    val onItemMoreClick: (videoId: String) -> Unit = { videoId ->
        currentItem = tracks.firstOrNull { it.videoId == videoId }
        if (currentItem != null) {
            itemBottomSheetShow = true
        }
    }
    val onPlaylistMoreClick: () -> Unit = {
        playlistBottomSheetShow = true
    }

    LaunchedEffect(key1 = playlistId) {
        if (playlistId != uiState.data?.id) {
            Logger.w(tag, "new id: $playlistId")
            viewModel.getData(playlistId)
        }
    }
    LaunchedEffect(key1 = firstItemVisible) {
        shouldHideTopBar = !firstItemVisible
    }
    val paletteState = rememberPaletteState()
    val hazeState =
        rememberHazeState(
        )
    var bitmap by remember {
        mutableStateOf<ImageBitmap?>(null)
    }
    // Track which thumbnail URL we've already extracted a palette from.
    // Prevents palette flash when LazyColumn recycles the header item on scroll —
    // AsyncImage re-mount fires onSuccess again, but we skip the regenerate.
    var paletteGeneratedFor by remember {
        mutableStateOf<String?>(null)
    }
    val currentThumbnail = (uiState as? PlaylistUIState.Success)?.data?.thumbnail

    LaunchedEffect(bitmap) {
        val bm = bitmap
        if (bm != null && currentThumbnail != null && paletteGeneratedFor != currentThumbnail) {
            paletteState.generate(bm)
            paletteGeneratedFor = currentThumbnail
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { paletteState.palette }
            .distinctUntilChanged()
            .collectLatest {
                viewModel.setBrush(listOf(it.getColorFromPalette(), md_theme_dark_background))
            }
    }

    // Apple Music-inspired immersive treatment: gated to mobile portrait so tablets,
    // foldable open state, landscape orientation, and Desktop keep the existing layout.
    val screenInfo = getScreenSizeInfo()
    val isMobilePortrait = true && screenInfo.wDP < screenInfo.hDP
    val dominantColor = listColors.firstOrNull() ?: md_theme_dark_background
    // Apple Music-style page background: derived from palette's Muted swatch (medium-bright,
    // unlike getColorFromPalette which prefers DarkVibrant/DarkMuted and turns near-black for
    // B&W artwork). Slight darkening for white-text readability.
    val mutedPaletteBg =
        run {
            val p = paletteState.palette
            val rgb =
                p
                    ?.getMutedColor(0)
                    ?.takeIf { it != 0 }
                    ?: p?.getDarkMutedColor(0)?.takeIf { it != 0 }
                    ?: p?.getDominantColor(0)?.takeIf { it != 0 }
            if (rgb != null) {
                lerp(Color(rgb), md_theme_dark_background, 0.45f)
            } else {
                md_theme_dark_background
            }
        }
    val artworkSizeDp =
        if (isMobilePortrait) {
            (screenInfo.wDP * 0.85f).coerceIn(280f, 380f).toInt()
        } else {
            250
        }

    // Loading dialog
    val showLoadingDialog by viewModel.showLoadingDialog.collectAsStateWithLifecycle()
    if (showLoadingDialog.first) {
        LoadingDialog(
            true,
            showLoadingDialog.second,
        )
    }
//    Box {
    Crossfade(
        targetState = uiState,
    ) { state ->
        Logger.w(tag, "State hash: ${state.hashCode()}")
        when (state) {
            is PlaylistUIState.Success -> {
                val data = state.data
                Logger.d(tag, "data: $data")
                if (data == null) return@Crossfade
                val hazeState =
                    rememberHazeState(
                    )
                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(if (isMobilePortrait) mutedPaletteBg else Color.Black)
                            .haze(hazeState),
                    state = lazyState,
                ) {
                    if (!showSearchBar) {
                        item(contentType = "header") {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                        .background(Color.Transparent)
                                        .animateItem(),
                            ) {
                                if (!isMobilePortrait) {
                                    Box(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth(),
                                    ) {
                                        Box(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .height(260.dp)
                                                    .clip(
                                                        RoundedCornerShape(8.dp),
                                                    ).angledGradientBackground(listColors, 25f),
                                        )
                                        Box(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .height(180.dp)
                                                    .align(Alignment.BottomCenter)
                                                    .background(
                                                        brush =
                                                            Brush.verticalGradient(
                                                                listOf(
                                                                    Color.Transparent,
                                                                    Color(0x75000000),
                                                                    Color.Black,
                                                                ),
                                                            ),
                                                    ),
                                        )
                                    }
                                }
                                Column(
                                    Modifier
                                        .background(Color.Transparent),
                                ) {
                                    if (!isMobilePortrait) {
                                        Row(
                                            modifier =
                                                Modifier
                                                    .wrapContentWidth()
                                                    .padding(16.dp)
                                                    .windowInsetsPadding(WindowInsets.statusBars),
                                        ) {
                                            RippleIconButton(
                                                resId = com.metrolist.music.R.drawable.baseline_arrow_back_ios_new_24,
                                            ) {
                                                navController.navigateUp()
                                            }
                                            Spacer(Modifier.weight(1f))
                                            IconButton(
                                                onClick = {
                                                    showSearchBar = !showSearchBar
                                                },
                                            ) {
                                                Icon(Icons.Rounded.Search, null, tint = Color.White)
                                            }
                                        }
                                    }
                                    Column(
                                        horizontalAlignment = Alignment.Start,
                                    ) {
                                        if (isMobilePortrait) {
                                            // Apple Music-style: edge-to-edge artwork + liquid glass buttons.
                                            // Glass buttons MUST be siblings of the backdrop source (not children)
                                            // to avoid render feedback loop / RuntimeShader crash.
                                            val artworkBackdrop = rememberBackdrop()
                                            val backBtnLayer = rememberGraphicsLayer()
                                            val rightGroupLayer = rememberGraphicsLayer()
                                            Box(
                                                modifier =
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .height((screenInfo.hDP / 2).dp),
                                            ) {
                                                // Inner Box — backdrop SOURCE (artwork + overlays only, NO glass)
                                                Box(modifier = Modifier.fillMaxSize().layerBackdrop(artworkBackdrop)) {
                                                    AsyncImage(
                                                        model =
                                                            ImageRequest
                                                                .Builder(LocalPlatformContext.current)
                                                                .data(data.thumbnail)
                                                                .diskCachePolicy(CachePolicy.ENABLED)
                                                                .memoryCachePolicy(CachePolicy.ENABLED)
                                                                .diskCacheKey(data.thumbnail)
                                                                .memoryCacheKey(data.thumbnail)
                                                                .crossfade(false)
                                                                .build(),
                                                        placeholder = painterResource(com.metrolist.music.R.drawable.holder),
                                                        error = painterResource(com.metrolist.music.R.drawable.holder),
                                                        contentDescription = null,
                                                        contentScale = ContentScale.Crop,
                                                        onSuccess = {
                                                            bitmap = it.result.image.toImageBitmap()
                                                        },
                                                        modifier = Modifier.fillMaxSize(),
                                                    )
                                                    Box(
                                                        modifier =
                                                            Modifier
                                                                .fillMaxWidth()
                                                                .height(200.dp)
                                                                .align(Alignment.BottomCenter)
                                                                .background(
                                                                    Brush.verticalGradient(
                                                                        listOf(
                                                                            Color.Transparent,
                                                                            Color.Transparent,
                                                                            mutedPaletteBg.copy(alpha = 0.5f),
                                                                            mutedPaletteBg,
                                                                        ),
                                                                    ),
                                                                ),
                                                    )
                                                    Column(
                                                        modifier =
                                                            Modifier
                                                                .align(Alignment.BottomCenter)
                                                                .fillMaxWidth()
                                                                .padding(horizontal = 20.dp)
                                                                .padding(bottom = 16.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                    ) {
                                                        Text(
                                                            text = data.title,
                                                            style = typo().titleLarge,
                                                            color = Color.White,
                                                            maxLines = 2,
                                                            textAlign = TextAlign.Center,
                                                        )
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        CompositionLocalProvider(
                                                            LocalMinimumInteractiveComponentSize provides Dp.Unspecified,
                                                        ) {
                                                            TextButton(
                                                                modifier =
                                                                    Modifier
                                                                        .wrapContentHeight()
                                                                        .defaultMinSize(minHeight = 1.dp, minWidth = 1.dp),
                                                                contentPadding = PaddingValues(vertical = 1.dp),
                                                                onClick = {
                                                                    if (data.author.id.isNotEmpty()) {
                                                                        navController.navigate(
                                                                            ArtistDestination(
                                                                                data.author.id,
                                                                            ),
                                                                        )
                                                                    }
                                                                },
                                                            ) {
                                                                Text(
                                                                    text = data.author.name,
                                                                    style = typo().titleSmall,
                                                                    color = Color.White,
                                                                    textAlign = TextAlign.Center,
                                                                )
                                                            }
                                                        }
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(
                                                            text = "${
                                                                if (data.isRadio) {
                                                                    stringResource(com.metrolist.music.R.string.radio)
                                                                } else {
                                                                    stringResource(com.metrolist.music.R.string.playlist)
                                                                }
                                                            } • ${data.year}",
                                                            style = typo().bodyMedium,
                                                            color = Color(0xC4FFFFFF),
                                                            textAlign = TextAlign.Center,
                                                        )
                                                    }
                                                }
                                                // Back + Heart + Search button overlays on artwork top — liquid glass
                                                Row(
                                                    modifier =
                                                        Modifier
                                                            .align(Alignment.TopCenter)
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                                            .windowInsetsPadding(WindowInsets.statusBars),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                ) {
                                                    Box(
                                                        modifier =
                                                            Modifier
                                                                .size(48.dp)
                                                                .drawBackdropCustomShape(
                                                                    artworkBackdrop,
                                                                    backBtnLayer,
                                                                    0.5f,
                                                                    CircleShape,
                                                                ),
                                                        contentAlignment = Alignment.Center,
                                                    ) {
                                                        RippleIconButton(
                                                            resId = com.metrolist.music.R.drawable.baseline_arrow_back_ios_new_24,
                                                        ) {
                                                            navController.navigateUp()
                                                        }
                                                    }
                                                    Spacer(Modifier.weight(1f))
                                                    Row(
                                                        modifier =
                                                            Modifier
                                                                .height(48.dp)
                                                                .drawBackdropCustomShape(
                                                                    artworkBackdrop,
                                                                    rightGroupLayer,
                                                                    0.5f,
                                                                    RoundedCornerShape(24.dp),
                                                                ),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                    ) {
                                                        if (!data.isRadio) {
                                                            Box(
                                                                modifier = Modifier.size(48.dp),
                                                                contentAlignment = Alignment.Center,
                                                            ) {
                                                                HeartCheckBox(
                                                                    size = 28,
                                                                    checked = liked,
                                                                    onStateChange = {
                                                                        viewModel.onUIEvent(PlaylistUIEvent.Favorite)
                                                                    },
                                                                )
                                                            }
                                                        }
                                                        IconButton(
                                                            onClick = {
                                                                showSearchBar = !showSearchBar
                                                            },
                                                        ) {
                                                            Icon(Icons.Rounded.Search, null, tint = Color.White)
                                                        }
                                                        IconButton(
                                                            onClick = onPlaylistMoreClick,
                                                        ) {
                                                            Icon(
                                                                painter = painterResource(com.metrolist.music.R.drawable.baseline_more_vert_24),
                                                                contentDescription = "More",
                                                                tint = Color.White,
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            AsyncImage(
                                                model =
                                                    ImageRequest
                                                        .Builder(LocalPlatformContext.current)
                                                        .data(data.thumbnail)
                                                        .diskCachePolicy(CachePolicy.ENABLED)
                                                        .diskCacheKey(data.thumbnail)
                                                        .crossfade(true)
                                                        .build(),
                                                placeholder = painterResource(com.metrolist.music.R.drawable.holder),
                                                error = painterResource(com.metrolist.music.R.drawable.holder),
                                                contentDescription = null,
                                                contentScale = ContentScale.FillHeight,
                                                onSuccess = {
                                                    bitmap = it.result.image.toImageBitmap()
                                                },
                                                modifier =
                                                    Modifier
                                                        .height(artworkSizeDp.dp)
                                                        .wrapContentWidth()
                                                        .align(Alignment.CenterHorizontally)
                                                        .clip(RoundedCornerShape(8.dp)),
                                            )
                                        }
                                        Box(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .wrapContentHeight(),
                                        ) {
                                            Column(Modifier.padding(horizontal = 32.dp)) {
                                                if (!isMobilePortrait) {
                                                    Spacer(modifier = Modifier.size(25.dp))
                                                    Text(
                                                        text = data.title,
                                                        style = typo().titleMedium,
                                                        color = Color.White,
                                                        maxLines = 2,
                                                    )
                                                    Column(
                                                        modifier = Modifier.padding(vertical = 4.dp),
                                                    ) {
                                                        CompositionLocalProvider(
                                                            LocalMinimumInteractiveComponentSize provides Dp.Unspecified,
                                                        ) {
                                                            TextButton(
                                                                modifier =
                                                                    Modifier
                                                                        .wrapContentHeight()
                                                                        .defaultMinSize(minHeight = 1.dp, minWidth = 1.dp),
                                                                contentPadding = PaddingValues(vertical = 1.dp),
                                                                onClick = {
                                                                    if (data.author.id.isNotEmpty()) {
                                                                        navController.navigate(
                                                                            ArtistDestination(
                                                                                data.author.id,
                                                                            ),
                                                                        )
                                                                    }
                                                                },
                                                            ) {
                                                                Text(
                                                                    text = data.author.name,
                                                                    style = typo().labelSmall,
                                                                    color = Color.White,
                                                                )
                                                            }
                                                        }
                                                        Spacer(modifier = Modifier.size(4.dp))
                                                        Text(
                                                            text = "${
                                                                if (data.isRadio) {
                                                                    stringResource(com.metrolist.music.R.string.radio)
                                                                } else {
                                                                    stringResource(com.metrolist.music.R.string.playlist)
                                                                }
                                                            } • ${data.year}",
                                                            style = typo().bodyMedium,
                                                        )
                                                    }
                                                }
                                                if (isMobilePortrait) {
                                                    // Apple Music-style action row:
                                                    // [Shuffle][Play pill][Download/More] (cluster centered, all 48dp matching size)
                                                    val isThisPlaying = isPlaying && playingPlaylistId == data.id
                                                    Row(
                                                        modifier =
                                                            Modifier
                                                                .fillMaxWidth()
                                                                .padding(vertical = 8.dp),
                                                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                    ) {
                                                        if (!data.isRadio) {
                                                            Box(
                                                                modifier =
                                                                    Modifier
                                                                        .size(48.dp)
                                                                        .clip(CircleShape)
                                                                        .background(Color.White.copy(alpha = 0.12f))
                                                                        .clickable {
                                                                            viewModel.onUIEvent(PlaylistUIEvent.Shuffle)
                                                                        },
                                                                contentAlignment = Alignment.Center,
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Rounded.Shuffle,
                                                                    contentDescription = "Shuffle",
                                                                    tint = Color.White,
                                                                    modifier = Modifier.size(22.dp),
                                                                )
                                                            }
                                                        }
                                                        Box(
                                                            modifier =
                                                                Modifier
                                                                    .height(48.dp)
                                                                    .widthIn(min = 110.dp)
                                                                    .clip(CircleShape)
                                                                    .background(Color.White)
                                                                    .clickable {
                                                                        if (isThisPlaying) {
                                                                            sharedViewModel.onUIEvent(UIEvent.PlayPause)
                                                                        } else {
                                                                            viewModel.onUIEvent(PlaylistUIEvent.PlayAll)
                                                                        }
                                                                    }.padding(horizontal = 20.dp),
                                                            contentAlignment = Alignment.Center,
                                                        ) {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Icon(
                                                                    imageVector =
                                                                        if (isThisPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                                                    contentDescription = null,
                                                                    tint = Color.Black,
                                                                    modifier = Modifier.size(22.dp),
                                                                )
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text(
                                                                    text = if (isThisPlaying) "Pause" else "Play",
                                                                    color = Color.Black,
                                                                    style = typo().labelLarge,
                                                                )
                                                            }
                                                        }
                                                        if (!data.isRadio) {
                                                            Box(
                                                                modifier =
                                                                    Modifier
                                                                        .size(48.dp)
                                                                        .clip(CircleShape)
                                                                        .background(Color.White.copy(alpha = 0.12f)),
                                                                contentAlignment = Alignment.Center,
                                                            ) {
                                                                Crossfade(targetState = downloadState) { state ->
                                                                    when (state) {
                                                                        DownloadState.STATE_DOWNLOADED -> {
                                                                            Box(
                                                                                modifier =
                                                                                    Modifier
                                                                                        .fillMaxSize()
                                                                                        .clickable {
                                                                                            viewModel.makeToast(
                                                                                                getStringBlocking(com.metrolist.music.R.string.downloaded),
                                                                                            )
                                                                                        },
                                                                                contentAlignment = Alignment.Center,
                                                                            ) {
                                                                                Icon(
                                                                                    painter = painterResource(com.metrolist.music.R.drawable.baseline_downloaded),
                                                                                    tint = Color(0xFF00A0CB),
                                                                                    contentDescription = "",
                                                                                    modifier = Modifier.size(22.dp),
                                                                                )
                                                                            }
                                                                        }

                                                                        DownloadState.STATE_DOWNLOADING -> {
                                                                            Box(
                                                                                modifier =
                                                                                    Modifier
                                                                                        .fillMaxSize()
                                                                                        .clickable {
                                                                                            viewModel.makeToast(
                                                                                                getStringBlocking(com.metrolist.music.R.string.downloading),
                                                                                            )
                                                                                        },
                                                                                contentAlignment = Alignment.Center,
                                                                            ) {
                                                                                Image(
                                                                                    painter =
                                                                                        rememberLottiePainter(
                                                                                            composition = composition,
                                                                                            iterations = Compottie.IterateForever,
                                                                                        ),
                                                                                    contentDescription = "Lottie animation",
                                                                                    modifier = Modifier.size(28.dp),
                                                                                )
                                                                            }
                                                                        }

                                                                        else -> {
                                                                            Box(
                                                                                modifier =
                                                                                    Modifier
                                                                                        .fillMaxSize()
                                                                                        .clickable {
                                                                                            Logger.w(
                                                                                                "PlaylistScreen",
                                                                                                "downloadState: $downloadState",
                                                                                            )
                                                                                            viewModel.onUIEvent(PlaylistUIEvent.Download)
                                                                                        },
                                                                                contentAlignment = Alignment.Center,
                                                                            ) {
                                                                                Icon(
                                                                                    painter = painterResource(com.metrolist.music.R.drawable.download_button),
                                                                                    tint = Color.White,
                                                                                    contentDescription = "Download",
                                                                                    modifier = Modifier.size(22.dp),
                                                                                )
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    Row(
                                                        modifier =
                                                            Modifier.fillMaxWidth(),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                    ) {
                                                        Crossfade(isPlaying && playingPlaylistId == data.id) { isThisPlaying ->
                                                            if (isThisPlaying) {
                                                                RippleIconButton(
                                                                    resId = com.metrolist.music.R.drawable.baseline_pause_circle_24,
                                                                    fillMaxSize = true,
                                                                    tint = seed,
                                                                    modifier = Modifier.size(48.dp),
                                                                ) {
                                                                    sharedViewModel.onUIEvent(UIEvent.PlayPause)
                                                                }
                                                            } else {
                                                                RippleIconButton(
                                                                    resId = com.metrolist.music.R.drawable.baseline_play_circle_24,
                                                                    fillMaxSize = true,
                                                                    tint = seed,
                                                                    modifier = Modifier.size(48.dp),
                                                                ) {
                                                                    viewModel.onUIEvent(PlaylistUIEvent.PlayAll)
                                                                }
                                                            }
                                                        }
                                                        if (!data.isRadio) {
                                                            HeartCheckBox(
                                                                size = 32,
                                                                checked = liked,
                                                                onStateChange = {
                                                                    viewModel.onUIEvent(PlaylistUIEvent.Favorite)
                                                                },
                                                            )
                                                            Crossfade(targetState = downloadState) {
                                                                when (it) {
                                                                    DownloadState.STATE_DOWNLOADED -> {
                                                                        Box(
                                                                            modifier =
                                                                                Modifier
                                                                                    .size(36.dp)
                                                                                    .clip(
                                                                                        CircleShape,
                                                                                    ).clickable {
                                                                                        viewModel.makeToast(getStringBlocking(com.metrolist.music.R.string.downloaded))
                                                                                    },
                                                                        ) {
                                                                            Icon(
                                                                                painter = painterResource(com.metrolist.music.R.drawable.baseline_downloaded),
                                                                                tint = Color(0xFF00A0CB),
                                                                                contentDescription = "",
                                                                                modifier =
                                                                                    Modifier
                                                                                        .size(36.dp)
                                                                                        .padding(2.dp),
                                                                            )
                                                                        }
                                                                    }

                                                                    DownloadState.STATE_DOWNLOADING -> {
                                                                        Box(
                                                                            modifier =
                                                                                Modifier
                                                                                    .size(36.dp)
                                                                                    .clip(
                                                                                        CircleShape,
                                                                                    ).clickable {
                                                                                        viewModel.makeToast(getStringBlocking(com.metrolist.music.R.string.downloading))
                                                                                    },
                                                                        ) {
                                                                            Image(
                                                                                painter =
                                                                                    rememberLottiePainter(
                                                                                        composition = composition,
                                                                                        iterations = Compottie.IterateForever,
                                                                                    ),
                                                                                contentDescription = "Lottie animation",
                                                                                modifier = Modifier.fillMaxSize(),
                                                                            )
                                                                        }
                                                                    }

                                                                    else -> {
                                                                        RippleIconButton(
                                                                            fillMaxSize = true,
                                                                            resId = com.metrolist.music.R.drawable.download_button,
                                                                            modifier = Modifier.size(36.dp),
                                                                        ) {
                                                                            Logger.w("PlaylistScreen", "downloadState: $downloadState")
                                                                            viewModel.onUIEvent(PlaylistUIEvent.Download)
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        Spacer(Modifier.weight(1f))
                                                        if (!data.isRadio) {
                                                            RippleIconButton(
                                                                modifier =
                                                                    Modifier.size(36.dp),
                                                                resId = com.metrolist.music.R.drawable.baseline_sensors_24,
                                                                fillMaxSize = true,
                                                            ) {
                                                                viewModel.onUIEvent(PlaylistUIEvent.StartRadio)
                                                            }
                                                            Spacer(Modifier.size(5.dp))
                                                            RippleIconButton(
                                                                modifier =
                                                                    Modifier.size(36.dp),
                                                                resId = com.metrolist.music.R.drawable.baseline_shuffle_24,
                                                                fillMaxSize = true,
                                                            ) {
                                                                viewModel.onUIEvent(PlaylistUIEvent.Shuffle)
                                                            }
                                                            Spacer(Modifier.size(5.dp))
                                                        }
                                                        RippleIconButton(
                                                            modifier =
                                                                Modifier.size(36.dp),
                                                            resId = com.metrolist.music.R.drawable.baseline_more_vert_24,
                                                            fillMaxSize = true,
                                                        ) {
                                                            onPlaylistMoreClick()
                                                        }
                                                    }
                                                }
                                                val uriHandler = LocalUriHandler.current
                                                DescriptionView(
                                                    modifier =
                                                        Modifier
                                                            .padding(
                                                                top = 8.dp,
                                                            ),
                                                    text =
                                                        state.data.description.let {
                                                            if (!it.isNullOrEmpty()) {
                                                                it
                                                            } else {
                                                                stringResource(com.metrolist.music.R.string.no_description)
                                                            }
                                                        },
                                                    limitLine = 3,
                                                    onTimeClicked = {},
                                                    onURLClicked = { url ->
                                                        uriHandler.openUri(url)
                                                    },
                                                )
                                                Text(
                                                    text =
                                                        if (data.isRadio) {
                                                            stringResource(com.metrolist.music.R.string.unlimited)
                                                        } else {
                                                            stringResource(
                                                                com.metrolist.music.R.string.album_length,
                                                                (data.trackCount).toString(),
                                                                "",
                                                            )
                                                        },
                                                    color = Color.White,
                                                    style = typo().bodyMedium,
                                                    modifier = Modifier.padding(vertical = 8.dp),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        item {
                            val density = LocalDensity.current
                            Spacer(
                                Modifier.height(
                                    with(density) { searchBarHeightPx.toDp() },
                                ),
                            )
                        }
                    }
                    items(count = filteredTrack.size, key = { index ->
                        val item = filteredTrack.getOrNull(index)
                        (item?.videoId ?: "") + "item_$index"
                    }) { index ->
                        val item = filteredTrack.getOrNull(index)
                        if (item != null) {
                            Column(modifier = Modifier.animateItem()) {
                                if (playingTrack?.videoId == item.videoId && isPlaying) {
                                    SongFullWidthItems(
                                        isPlaying = true,
                                        track = item,
                                        onMoreClickListener = { onItemMoreClick(it) },
                                        onClickListener = {
                                            Logger.w("PlaylistScreen", "index: $index")
                                            onPlaylistItemClick(it)
                                        },
                                        onAddToQueue = {
                                            sharedViewModel.addListToQueue(
                                                arrayListOf(item),
                                            )
                                        },
                                        modifier = Modifier,
                                    )
                                } else {
                                    SongFullWidthItems(
                                        isPlaying = false,
                                        track = item,
                                        onMoreClickListener = { onItemMoreClick(it) },
                                        onClickListener = {
                                            Logger.w("PlaylistScreen", "index: $index")
                                            onPlaylistItemClick(it)
                                        },
                                        onAddToQueue = {
                                            sharedViewModel.addListToQueue(
                                                arrayListOf(item),
                                            )
                                        },
                                        modifier = Modifier,
                                    )
                                }
                                if (isMobilePortrait && index < filteredTrack.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 72.dp, end = 16.dp),
                                        thickness = 0.5.dp,
                                        color = Color.White.copy(alpha = 0.12f),
                                    )
                                }
                            }
                        }
                    }
                    when (tracksListState) {
                        ListState.IDLE -> {
                            // DO NOTHING
                            item {
                                EndOfPage()
                            }
                        }

                        ListState.LOADING, ListState.PAGINATING -> {
                            item {
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CenterLoadingBox(
                                        modifier = Modifier.size(80.dp),
                                    )
                                }
                            }
                            item {
                                EndOfPage()
                            }
                        }

                        ListState.ERROR -> {
                            item {
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .height(64.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = stringResource(com.metrolist.music.R.string.error),
                                        style = typo().bodyMedium,
                                    )
                                }
                            }
                            item {
                                EndOfPage()
                            }
                        }

                        ListState.PAGINATION_EXHAUST -> {
                            item {
                                EndOfPage()
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = showSearchBar,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically(),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { searchBarHeightPx = it.size.height }
                            .then(
                                if (isMobilePortrait) {
                                    Modifier.hazeChild(hazeState) {
                                        blurRadius = 24.dp
                                        backgroundColor = mutedPaletteBg
                                        tints = listOf(HazeTint(mutedPaletteBg.copy(alpha = 0.55f)))
                                    }
                                } else {
                                    Modifier.background(Color.Black)
                                },
                            ),
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .windowInsetsPadding(WindowInsets.statusBars),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RippleIconButton(
                                resId = com.metrolist.music.R.drawable.baseline_arrow_back_ios_new_24,
                            ) {
                                navController.navigateUp()
                            }
                            SearchBar(
                                modifier =
                                    Modifier
                                        .height(50.dp)
                                        .padding(horizontal = 12.dp)
                                        .weight(1f),
                                colors =
                                    SearchBarDefaults.colors().copy(
                                        containerColor = Color.Transparent,
                                    ),
                                inputField = {
                                    CompositionLocalProvider(LocalTextStyle provides typo().bodySmall) {
                                        SearchBarDefaults.InputField(
                                            query = query,
                                            onQueryChange = { query = it },
                                            onSearch = { showSearchBar = false },
                                            expanded = showSearchBar,
                                            onExpandedChange = { showSearchBar = it },
                                            placeholder = {
                                                Text(
                                                    stringResource(com.metrolist.music.R.string.search),
                                                    style = typo().bodyMedium,
                                                )
                                            },
                                        )
                                    }
                                },
                                expanded = false,
                                onExpandedChange = {},
                                windowInsets = WindowInsets(0, 0, 0, 0),
                            ) {
                            }
                            IconButton(
                                onClick = {
                                    showSearchBar = !showSearchBar
                                },
                            ) {
                                Icon(Icons.Rounded.Close, null, tint = Color.White)
                            }
                        }
                    }
                }

                if (itemBottomSheetShow && currentItem != null) {
                    val track = currentItem?.toSongEntity() ?: return@Crossfade
                    NowPlayingBottomSheet(
                        onDismiss = {
                            itemBottomSheetShow = false
                            currentItem = null
                        },
                        navController = navController,
                        song = track,
                    )
                }
                if (playlistBottomSheetShow) {
                    Logger.w("PlaylistScreen", "PlaylistBottomSheet")
                    val addToQueue = {
                        viewModel.getFullTracks { track ->
                            sharedViewModel.addListToQueue(
                                track.toCollection(arrayListOf()),
                            )
                        }
                    }
                    PlaylistBottomSheet(
                        onDismiss = { playlistBottomSheetShow = false },
                        playlistId = data.id,
                        playlistName = data.title,
                        isYourYouTubePlaylist = isYourYouTubePlaylist && !data.isRadio,
                        onSaveToLocal = {
                            viewModel.getFullTracks { track ->
                                viewModel.saveToLocal(track)
                            }
                        },
                        onEditTitle = { newTitle ->
                            viewModel.updatePlaylistTitle(newTitle, data.id)
                        },
                        onAddToQueue = if (data.isRadio) null else addToQueue,
                    )
                }
                AnimatedVisibility(
                    visible = shouldHideTopBar && !showSearchBar,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically(),
                ) {
                    TopAppBar(
                        windowInsets =
                            TopAppBarDefaults.windowInsets.exclude(
                                TopAppBarDefaults.windowInsets.only(WindowInsetsSides.Start),
                            ),
                        title = {
                            Text(
                                text = data.title,
                                style = typo().titleMedium,
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
                        },
                        navigationIcon = {
                            Box(Modifier.padding(horizontal = 5.dp)) {
                                RippleIconButton(
                                    com.metrolist.music.R.drawable.baseline_arrow_back_ios_new_24,
                                    Modifier
                                        .size(32.dp),
                                    true,
                                ) {
                                    navController.navigateUp()
                                }
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = {
                                    showSearchBar = !showSearchBar
                                },
                            ) {
                                Icon(Icons.Rounded.Search, null, tint = Color.White)
                            }
                        },
                        colors =
                            TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent,
                            ),
                        modifier =
                            if (isMobilePortrait) {
                                Modifier.hazeChild(hazeState) {
                                    blurRadius = 24.dp
                                    backgroundColor = mutedPaletteBg
                                    tints = listOf(HazeTint(mutedPaletteBg.copy(alpha = 0.55f)))
                                }
                            } else {
                                Modifier.angledGradientBackground(listColors, 90f)
                            },
                    )
                }
            }

            is PlaylistUIState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CenterLoadingBox(
                        modifier = Modifier.size(80.dp),
                    )
                }
            }

            is PlaylistUIState.Error -> {
                viewModel.makeToast("Error: ${state.message}")
                navController.navigateUp()
            }
        }
    }
}