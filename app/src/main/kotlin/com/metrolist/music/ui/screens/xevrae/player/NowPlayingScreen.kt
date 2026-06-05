@file:OptIn(ExperimentalMaterial3Api::class)

package com.metrolist.music.ui.screens.xevrae.player

import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.SubtitlesOff
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Forward5
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Replay5
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.ThumbsUpDown
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import com.metrolist.music.ui.component.DimIconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
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
import com.metrolist.music.common.Config.MAIN_PLAYER
import com.metrolist.music.utils.Logger
import com.metrolist.music.expect.ui.MediaPlayerView
import com.metrolist.music.expect.ui.MediaPlayerViewWithSubtitle
import com.metrolist.music.expect.ui.toImageBitmap
import com.metrolist.music.extensions.GradientAngle
import com.metrolist.music.extensions.GradientOffset
import com.metrolist.music.extensions.KeepScreenOn
import com.metrolist.music.extensions.formatDuration
import com.metrolist.music.extensions.getColorFromPalette
import com.metrolist.music.extensions.getScreenSizeInfo
import com.metrolist.music.extensions.getStringBlocking
import com.metrolist.music.extensions.hsvToColor
import com.metrolist.music.extensions.isElementVisible
import com.metrolist.music.extensions.parseTimestampToMilliseconds
import com.metrolist.music.extensions.rememberIsInPipMode
import com.metrolist.music.ui.component.AIBadge
import com.metrolist.music.ui.component.AddToPlaylistModalBottomSheet
import com.metrolist.music.ui.component.DescriptionView
import com.metrolist.music.ui.component.ExplicitBadge
import com.metrolist.music.ui.component.FullscreenLyricsSheet
import com.metrolist.music.ui.component.HeartCheckBox
import com.metrolist.music.ui.component.InfoPlayerBottomSheet
import com.metrolist.music.ui.component.LyricsView
import com.metrolist.music.ui.component.NowPlayingBottomSheet
import com.metrolist.music.ui.component.PlayPauseButton
import com.metrolist.music.ui.component.PlayerControlLayout
import com.metrolist.music.ui.component.QueueBottomSheet
import com.metrolist.music.ui.component.VoteLyricsDialog
import com.metrolist.music.ui.navigation.xevrae.destination.list.ArtistDestination
import com.metrolist.music.ui.navigation.xevrae.destination.player.FullscreenDestination
import com.metrolist.music.ui.theme.xevrae.blackMoreOverlay
import com.metrolist.music.ui.theme.xevrae.md_theme_dark_background
import com.metrolist.music.ui.theme.xevrae.overlay
import com.metrolist.music.ui.theme.xevrae.typo
import com.metrolist.music.viewmodels.xevrae.LyricsProvider
import com.metrolist.music.viewmodels.xevrae.NowPlayingBottomSheetUIEvent
import com.metrolist.music.viewmodels.xevrae.NowPlayingBottomSheetViewModel
import com.metrolist.music.viewmodels.xevrae.SharedViewModel
import com.metrolist.music.viewmodels.xevrae.UIEvent
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.materials.CupertinoMaterials
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.activity.ComponentActivity
import com.metrolist.music.LocalActivity
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.abs
import kotlin.math.roundToLong

private const val TAG = "NowPlayingScreen"

private fun deriveOrderIndex(
    queue: List<com.metrolist.innertube.models.SongItem>,
    videoId: String?,
): Int {
    if (videoId == null) return 0
    val idx = queue.indexOfFirst { it.id == videoId }
    return if (idx == -1) 0 else idx
}
private val RICH_SYNC_TIMESTAMP_REGEX = Regex("""<\d{2}:\d{2}\.\d{2,3}>\s*""")

@OptIn(ExperimentalFoundationApi::class, ExperimentalHazeMaterialsApi::class)
@ExperimentalMaterial3Api
@Composable
fun NowPlayingScreen(
    sharedViewModel: SharedViewModel = hiltViewModel(viewModelStoreOwner = LocalActivity.current as ComponentActivity),
    navController: NavController,
    onDismiss: () -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        )

    val hideSheet: () -> Unit = {
        coroutineScope.launch {
            sheetState.hide()
            onDismiss()
        }
    }

    ModalBottomSheet(
        modifier =
            Modifier
                .fillMaxHeight(),
        onDismissRequest = {
            onDismiss()
        },
        containerColor = Color(0xFF121212),
        dragHandle = {},
        scrimColor = Color(0xFF121212),
        sheetState = sheetState,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        shape = RectangleShape,
    ) {
        NowPlayingScreenContent(
            sharedViewModel = sharedViewModel,
            navController = navController,
            isExpanded = sheetState.currentValue == SheetValue.Expanded,
            dismissIcon = Icons.Rounded.KeyboardArrowDown,
            onDismiss = {
                hideSheet()
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalHazeMaterialsApi::class)
@Composable
fun NowPlayingScreenContent(
    sharedViewModel: SharedViewModel = hiltViewModel(viewModelStoreOwner = LocalActivity.current as ComponentActivity),
    navController: NavController,
    isExpanded: Boolean,
    dismissIcon: ImageVector,
    onDismiss: () -> Unit = {},
) {
    val screenInfo = getScreenSizeInfo()

    val localDensity = LocalDensity.current
    val uriHandler = LocalUriHandler.current

    // ViewModel State
    val controllerState by sharedViewModel.controllerState.collectAsStateWithLifecycle()
    val screenDataState by sharedViewModel.nowPlayingScreenData.collectAsStateWithLifecycle()
    val timelineState by sharedViewModel.timeline.collectAsStateWithLifecycle()
    val likeStatus by sharedViewModel.likeStatus.collectAsStateWithLifecycle()

    val shouldShowVideo by sharedViewModel.getVideo.collectAsStateWithLifecycle()
    val translatedVoteState by sharedViewModel.translatedVoteState.collectAsStateWithLifecycle()
    val lyricsVoteState by sharedViewModel.lyricsVoteState.collectAsStateWithLifecycle()

    // Artwork Pager state — Spotify-style horizontal swipe between queue tracks.
    val nowPlayingState by sharedViewModel.nowPlayingState.collectAsStateWithLifecycle()
    val queueDataState by sharedViewModel.getQueueDataState().collectAsStateWithLifecycle()
    val artworkQueue by remember {
        derivedStateOf { queueDataState?.data?.listTracks ?: emptyList() }
    }
    // ⚠️ Use track.videoId (already prefix-stripped at MediaServiceHandlerImpl.kt:386).
    // Do NOT use mediaItem.mediaId — it carries the "Video" prefix for video items.
    val nowPlayingVideoId: String? = nowPlayingState?.songEntity?.id ?: nowPlayingState?.track?.id
    val currentOrderIndex by remember(artworkQueue, nowPlayingVideoId) {
        derivedStateOf { deriveOrderIndex(artworkQueue, nowPlayingVideoId) }
    }
    val isRepeatOne = controllerState.repeatState == com.metrolist.music.models.xevrae.RepeatState.One
    // Single PagerState — the unified ArtworkPager renders BOTH the fullscreen canvas
    // background and the centered square thumbnail in each page.
    val artworkPagerState =
        rememberPagerState(
            initialPage = currentOrderIndex.coerceAtLeast(0),
            pageCount = { artworkQueue.size.coerceAtLeast(1) },
        )
    var isAnimatingFromPlayer by remember { mutableStateOf(false) }
    var isUserDraggingActive by remember { mutableStateOf(false) }

    LaunchedEffect(artworkPagerState) {
        snapshotFlow {
            artworkPagerState.isScrollInProgress to isAnimatingFromPlayer
        }.collect { (scrolling, animating) ->
            isUserDraggingActive = scrolling && !animating
        }
    }

    // ① Player → Pager: animate to new track when player advances.
    LaunchedEffect(currentOrderIndex, artworkQueue.size) {
        val target = currentOrderIndex
        if (!isUserDraggingActive &&
            artworkQueue.isNotEmpty() &&
            target in 0 until artworkQueue.size &&
            target != artworkPagerState.currentPage
        ) {
            isAnimatingFromPlayer = true
            try {
                artworkPagerState.animateScrollToPage(target)
            } finally {
                isAnimatingFromPlayer = false
            }
        }
    }

    // ② Pager → Player: seek when user settles on a different page.
    LaunchedEffect(artworkPagerState, currentOrderIndex, artworkQueue.size) {
        snapshotFlow { artworkPagerState.settledPage }
            .distinctUntilChanged()
            .collect { settled ->
                if (isAnimatingFromPlayer) return@collect
                if (artworkQueue.isEmpty()) return@collect
                if (settled !in 0 until artworkQueue.size) return@collect
                if (settled == currentOrderIndex) return@collect

                runCatching {
                    when (val action = computeSeekAction(settled, currentOrderIndex)) {
                        ArtworkSeekAction.Next -> {
                            sharedViewModel.onUIEvent(UIEvent.Next)
                        }
                        ArtworkSeekAction.Previous -> {
                            sharedViewModel.onUIEvent(UIEvent.SkipToPrevious)
                        }
                        is ArtworkSeekAction.Skip -> {
                            sharedViewModel.onUIEvent(UIEvent.Next)
                        }
                        ArtworkSeekAction.NoOp -> {
                            Unit
                        }
                    }
                }.onFailure { error ->
                    Logger.w(TAG, "ArtworkPager seek failed: ${error.message}")
                }
            }
    }

    // ③ Queue mutation guard
    LaunchedEffect(artworkQueue.size) {
        if (artworkQueue.isNotEmpty() && artworkPagerState.currentPage >= artworkQueue.size) {
            runCatching { artworkPagerState.scrollToPage(artworkQueue.lastIndex) }
        }
    }

    // State
    val isInPipMode = rememberIsInPipMode()

    val mainScrollState = rememberScrollState()

    var showHideMiddleLayout by rememberSaveable {
        mutableStateOf(true)
    }

    var showSheet by rememberSaveable {
        mutableStateOf(false)
    }

    var showFullscreenLyrics by rememberSaveable {
        mutableStateOf(false)
    }

    var showQueueBottomSheet by rememberSaveable {
        mutableStateOf(false)
    }

    var showInfoBottomSheet by rememberSaveable {
        mutableStateOf(false)
    }

    var showVoteDialog by rememberSaveable {
        mutableStateOf(false)
    }

    // NEW: Add to Playlist state
    var showAddToPlaylistDirectly by rememberSaveable {
        mutableStateOf(false)
    }

    var shouldShowToolbar by remember {
        mutableStateOf(false)
    }

    // Palette state
    val paletteState = rememberPaletteState()

    val startColor =
        remember {
            Animatable(md_theme_dark_background)
        }
    val endColor =
        remember {
            Animatable(md_theme_dark_background)
        }
    val gradientOffset by remember {
        mutableStateOf(GradientOffset(GradientAngle.CW135))
    }

    var spotShadowColor by remember {
        mutableStateOf(Color.White)
    }

    val blurBg by sharedViewModel.blurBg.collectAsStateWithLifecycle()

    LaunchedEffect(screenDataState) {
        Logger.d(TAG, "ScreenDataState: $screenDataState")
        showHideMiddleLayout = screenDataState.canvasData == null
        snapshotFlow { screenDataState.bitmap }.collectLatest {
            if (it != null) {
                paletteState.generate(it)
            }
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { paletteState.palette }
            .distinctUntilChanged()
            .collectLatest {
                spotShadowColor = it.getColorFromPalette()
                startColor.animateTo(it.getColorFromPalette())
                endColor.animateTo(md_theme_dark_background)
            }
    }

    LaunchedEffect(spotShadowColor) {
        Logger.d(TAG, "spotShadowColor: $spotShadowColor")
    }
    // Height
    var topAppBarHeightDp by rememberSaveable {
        mutableIntStateOf(0)
    }
    var middleLayoutHeightDp by rememberSaveable {
        mutableIntStateOf(0)
    }
    var infoLayoutHeightDp by rememberSaveable {
        mutableIntStateOf(0)
    }
    var middleLayoutPaddingDp by rememberSaveable {
        mutableIntStateOf(0)
    }
    val minimumPaddingDp by rememberSaveable {
        mutableIntStateOf(
            30,
        )
    }
    LaunchedEffect(
        topAppBarHeightDp,
        screenInfo,
        infoLayoutHeightDp,
        minimumPaddingDp,
    ) {
        if (topAppBarHeightDp > 0 && middleLayoutHeightDp > 0 && infoLayoutHeightDp > 0 && screenInfo.hDP > 0) {
            val result = (screenInfo.hDP - topAppBarHeightDp - middleLayoutHeightDp - infoLayoutHeightDp - minimumPaddingDp) / 2
            middleLayoutPaddingDp =
                if (result > minimumPaddingDp) {
                    result
                } else {
                    minimumPaddingDp
                }
        }
    }

    var isSliding by rememberSaveable {
        mutableStateOf(false)
    }
    var sliderValue by rememberSaveable {
        mutableFloatStateOf(0f)
    }
    LaunchedEffect(key1 = timelineState, key2 = isSliding) {
        if (!isSliding) {
            sliderValue =
                if (timelineState.total > 0L) {
                    timelineState.current.toFloat() * 100 / timelineState.total.toFloat()
                } else {
                    0f
                }
        }
    }

    // Crossfade: RGB rainbow color cycling when transitioning between tracks
    val infiniteTransition = rememberInfiniteTransition(label = "crossfadeRainbow")
    val rainbowHue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "rainbowHue",
    )
    val rainbowColor = hsvToColor(rainbowHue, 1f, 1f)
    val sliderTrackColor by animateColorAsState(
        targetValue = if (timelineState.isCrossfading) rainbowColor else Color.White,
        animationSpec = tween(300),
        label = "sliderCrossfadeColor",
    )

    // Show ControlLayout Or Show Artist Badge
    var showHideControlLayout by rememberSaveable {
        mutableStateOf(true)
    }
    val controlLayoutAlpha: Float by animateFloatAsState(
        targetValue = if (showHideControlLayout) 1f else 0f,
        animationSpec =
            tween(
                durationMillis = 500,
                easing = LinearEasing,
            ),
        label = "ControlLayoutAlpha",
    )

    var showHideJob by remember {
        mutableStateOf(true)
    }

    LaunchedEffect(key1 = showHideJob) {
        if (!showHideJob) {
            delay(5000)
            if (mainScrollState.value == 0) showHideControlLayout = false
            showHideJob = true
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow {
            screenDataState
        }.distinctUntilChangedBy {
            it.canvasData?.url
        }.collectLatest {
            if (it.canvasData != null && mainScrollState.value == 0) {
                showHideJob = false
            } else {
                showHideJob = true
                showHideControlLayout = true
            }
        }
    }

    LaunchedEffect(key1 = showHideControlLayout) {
        if (showHideControlLayout && screenDataState.canvasData != null && mainScrollState.value == 0) {
            showHideJob = false
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { mainScrollState.value }
            .distinctUntilChanged()
            .collect {
                if (it > 0 && !showHideControlLayout && screenDataState.canvasData != null) {
                    showHideJob = true
                    showHideControlLayout = true
                } else if (showHideControlLayout && it == 0 && screenDataState.canvasData != null) {
                    showHideJob = false
                }
            }
    }

    // Fullscreen overlay
    var showHideFullscreenOverlay by rememberSaveable {
        mutableStateOf(false)
    }

    var canvasSubtitleLineIndex by rememberSaveable {
        mutableIntStateOf(-1)
    }

    LaunchedEffect(key1 = showHideFullscreenOverlay) {
        if (showHideFullscreenOverlay) {
            delay(3000)
            showHideFullscreenOverlay = false
        }
    }

    // Canvas subtitle sync
    LaunchedEffect(timelineState, screenDataState.lyricsData?.lyrics) {
        val lyrics = screenDataState.lyricsData?.lyrics
        if (lyrics == null || lyrics.syncType == "UNSYNCED" || lyrics.syncType == null) {
            canvasSubtitleLineIndex = -1
            return@LaunchedEffect
        }
        val lines = lyrics.lines ?: return@LaunchedEffect
        val translatedLines =
            screenDataState.lyricsData
                ?.translatedLyrics
                ?.first
                ?.lines
        if (timelineState.current > 0L) {
            lines.indices.forEach { i ->
                val startTimeMs = lines[i].startTimeMs.toLongOrNull() ?: 0L
                val endTimeMs =
                    if (i < lines.size - 1) {
                        lines[i + 1].startTimeMs.toLongOrNull() ?: 0L
                    } else {
                        startTimeMs + 60000
                    }
                if (timelineState.current in startTimeMs..endTimeMs) {
                    canvasSubtitleLineIndex = i
                }
            }
            if (lines.isNotEmpty() &&
                timelineState.current in 0..(lines.getOrNull(0)?.startTimeMs?.toLongOrNull() ?: 0L)
            ) {
                canvasSubtitleLineIndex = -1
            }
        } else {
            canvasSubtitleLineIndex = -1
        }
    }

    if (showSheet) {
        NowPlayingBottomSheet(
            onDismiss = {
                showSheet = false
            },
            navController = navController,
            onNavigateToOtherScreen = {
                onDismiss()
            },
            song = null, // Auto set now playing
            setSleepTimerEnable = true,
            changeMainLyricsProviderEnable = true,
        )
    }

    if (showFullscreenLyrics) {
        FullscreenLyricsSheet(
            sharedViewModel = sharedViewModel,
            navController = navController,
            color = startColor.value,
            shouldHaze = sharedViewModel.blurFullscreenLyrics(),
        ) {
            showFullscreenLyrics = false
        }
    }

    if (showQueueBottomSheet) {
        QueueBottomSheet(
            onDismiss = {
                showQueueBottomSheet = false
            },
        )
    }

    if (showInfoBottomSheet) {
        InfoPlayerBottomSheet(
            onDismiss = {
                showInfoBottomSheet = false
            },
        )
    }

    // NEW: Add to Playlist Bottom Sheet
    if (showAddToPlaylistDirectly) {
        val viewModel: NowPlayingBottomSheetViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            viewModel.resetPlaylists()
            viewModel.setSongEntity(null) // Uses current playing song
        }

        AddToPlaylistModalBottomSheet(
            isBottomSheetVisible = true,
            listLocalPlaylist = uiState.listLocalPlaylist,
            listYouTubePlaylist = uiState.listYouTubePlaylist,
            onDismiss = { showAddToPlaylistDirectly = false },
            onClick = { playlist ->
                viewModel.onUIEvent(NowPlayingBottomSheetUIEvent.AddToPlaylist(playlist.id))
                showAddToPlaylistDirectly = false
            },
            onYTPlaylistClick = { playlist ->
                viewModel.onUIEvent(NowPlayingBottomSheetUIEvent.AddToYouTubePlaylist(playlist.browseId))
                showAddToPlaylistDirectly = false
            },
            videoId = uiState.songUIState.videoId,
        )
    }

    // Vote Dialog
    if (showVoteDialog) {
        val canVoteLyrics =
            screenDataState.lyricsData?.lyricsProvider == LyricsProvider.XEVRAE &&
                screenDataState.lyricsData
                    ?.lyrics
                    ?.simpMusicLyrics != null
        val canVoteTranslatedLyrics =
            screenDataState.lyricsData?.translatedLyrics?.second == LyricsProvider.XEVRAE &&
                screenDataState.lyricsData
                    ?.translatedLyrics
                    ?.first
                    ?.simpMusicLyrics != null

        VoteLyricsDialog(
            canVoteLyrics = canVoteLyrics,
            canVoteTranslatedLyrics = canVoteTranslatedLyrics,
            lyricsVoteState = lyricsVoteState,
            translatedLyricsVoteState = translatedVoteState,
            onVoteLyrics = { upvote ->
                sharedViewModel.voteLyrics(upvote)
            },
            onVoteTranslatedLyrics = { upvote ->
                sharedViewModel.voteTranslatedLyrics(upvote)
            },
            onDismiss = {
                showVoteDialog = false
            },
        )
    }

    val hazeState = remember { HazeState() }

    if (screenDataState.lyricsData != null && controllerState.isPlaying) {
        KeepScreenOn()
    }
    Box {
        if (blurBg && screenDataState.canvasData == null) {
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalPlatformContext.current)
                        .data(screenDataState.thumbnailURL)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .diskCacheKey(screenDataState.thumbnailURL + "BIGGER")
                        .crossfade(550)
                        .build(),
                contentDescription = "",
                contentScale = ContentScale.FillHeight,
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .fillMaxSize()
                        .haze(hazeState),
            )
        }
        Column(
            Modifier
                .verticalScroll(
                    mainScrollState,
                    enabled = isExpanded,
                )
                // Horizontal swipe is handled by the unified ArtworkPager below.
                // Spacers in this Column have no pointer input and don't block hits.
                .then(
                    if (showHideMiddleLayout) {
                        if (blurBg && screenDataState.canvasData == null) {
                            Modifier
                                .background(Color.Transparent)
                                .hazeChild(hazeState, style = CupertinoMaterials.thin()) {
                                }
                        } else {
                            Modifier
                                .background(
                                    Brush.linearGradient(
                                        colors =
                                            listOf(
                                                startColor.value,
                                                endColor.value,
                                            ),
                                        start = gradientOffset.start,
                                        end = gradientOffset.end,
                                    ),
                                )
                        }
                    } else {
                        Modifier.background(md_theme_dark_background)
                    },
                ),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // === Unified ArtworkPager (Spotify-style swipe) ===
                // ONE HorizontalPager wraps both the fullscreen canvas backdrop AND the
                // centered square thumbnail. Both layers slide together as a single page.
                HorizontalPager(
                    state = artworkPagerState,
                    modifier =
                        Modifier
                            .height(screenInfo.hDP.dp)
                            .fillMaxWidth(),
                    beyondViewportPageCount = 1,
                    userScrollEnabled = !isRepeatOne && artworkQueue.isNotEmpty(),
                    key = { idx ->
                        val vid = artworkQueue.getOrNull(idx)?.id.orEmpty()
                        "artwork_${vid}_$idx"
                    },
                ) { page ->
                    val pageTrack = artworkQueue.getOrNull(page)
                    val isCurrentArtworkPage = page == currentOrderIndex
                    val pageHasCanvas = isCurrentArtworkPage && screenDataState.canvasData != null

                    val pagePaletteState = rememberPaletteState()
                    val pageStartColor =
                        remember(pageTrack?.id) {
                            Animatable(md_theme_dark_background)
                        }
                    val palettePageScope = rememberCoroutineScope()
                    LaunchedEffect(pagePaletteState, pageTrack?.id) {
                        snapshotFlow { pagePaletteState.palette }
                            .distinctUntilChanged()
                            .collectLatest { palette ->
                                pageStartColor.animateTo(
                                    palette.getColorFromPalette(),
                                )
                            }
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .clipToBounds()
                                .clickable(
                                    enabled = pageHasCanvas,
                                    onClick = {
                                        if (mainScrollState.value == 0) {
                                            showHideJob = true
                                            showHideControlLayout = !showHideControlLayout
                                        }
                                    },
                                    indication = null,
                                    interactionSource =
                                        remember {
                                            MutableInteractionSource()
                                        },
                                ),
                    ) {
                        // ── Layer 0: per-page backdrop (adjacent pages only) ──
                        if (!isCurrentArtworkPage && pageTrack != null) {
                            if (blurBg) {
                                val backdropUrl =
                                    pageTrack.thumbnail
                                AsyncImage(
                                    model =
                                        ImageRequest
                                            .Builder(LocalPlatformContext.current)
                                            .data(backdropUrl)
                                            .diskCachePolicy(CachePolicy.ENABLED)
                                            .diskCacheKey(backdropUrl)
                                            .crossfade(300)
                                            .build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    placeholder = ColorPainter(androidx.compose.ui.graphics.Color(0xFF2A2A2A)),
                                    error = ColorPainter(androidx.compose.ui.graphics.Color(0xFF2A2A2A)),
                                    modifier =
                                        Modifier
                                            .fillMaxSize()
                                            .alpha(0.35f),
                                )
                            } else {
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.linearGradient(
                                                    colors =
                                                        listOf(
                                                            pageStartColor.value,
                                                            md_theme_dark_background,
                                                        ),
                                                    start = gradientOffset.start,
                                                    end = gradientOffset.end,
                                                ),
                                            ),
                                )
                            }
                        }

                        // ── Layer 1: fullscreen canvas backdrop (current track + canvas data) ──
                        if (pageHasCanvas) {
                            Crossfade(targetState = screenDataState.canvasData?.isVideo) { isVideo ->
                                if (isVideo == true) {
                                    screenDataState.canvasData?.url?.let { url ->
                                        MediaPlayerView(
                                            url = url,
                                            screenSize = screenInfo,
                                            modifier =
                                                Modifier
                                                    .fillMaxHeight()
                                                    .then(
                                                        if (false) {
                                                            Modifier
                                                        } else {
                                                            Modifier
                                                                .wrapContentWidth(unbounded = true, align = Alignment.CenterHorizontally)
                                                                .align(Alignment.Center)
                                                        },
                                                    ),
                                        )
                                    }
                                } else if (isVideo == false) {
                                    AsyncImage(
                                        model =
                                            ImageRequest
                                                .Builder(LocalPlatformContext.current)
                                                .data(screenDataState.canvasData?.url)
                                                .diskCachePolicy(CachePolicy.ENABLED)
                                                .diskCacheKey(screenDataState.canvasData?.url)
                                                .crossfade(550)
                                                .build(),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                            }
                            // Bottom gradient overlay
                            Crossfade(
                                targetState = showHideControlLayout,
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .align(Alignment.BottomCenter),
                            ) { focused ->
                                if (focused) {
                                    Box(
                                        modifier =
                                            Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        colorStops =
                                                            arrayOf(
                                                                0.2f to overlay,
                                                                1f to Color(0xFF121212),
                                                            ),
                                                    ),
                                                ),
                                    )
                                } else {
                                    Box(
                                        modifier =
                                            Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        colorStops =
                                                            arrayOf(
                                                                0f to Color.Black.copy(alpha = 0.5f),
                                                                0.1f to Color.Transparent,
                                                                0.75f to Color.Transparent,
                                                                0.95f to Color(0xFF121212).copy(alpha = 0.7f),
                                                                1f to Color(0xFF121212),
                                                            ),
                                                    ),
                                                ),
                                    )
                                }
                            }
                        }

                        // ── Layer 2: centered square thumbnail ──
                        Column(modifier = Modifier.fillMaxSize()) {
                            Spacer(modifier = Modifier.height(topAppBarHeightDp.dp))
                            Spacer(
                                modifier =
                                    Modifier
                                        .animateContentSize()
                                        .height(middleLayoutPaddingDp.dp)
                                        .fillMaxWidth(),
                            )
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp)
                                        .alpha(if (pageHasCanvas) 0f else 1f)
                                        .aspectRatio(1f),
                            ) {
                                if (isCurrentArtworkPage) {
                                    // Live artwork (drives palette extraction via setBitmap).
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier =
                                            Modifier
                                                .align(Alignment.Center)
                                                .background(Color.Transparent)
                                                .shadow(
                                                    elevation = 3.dp,
                                                    shape = RoundedCornerShape(15.dp),
                                                    spotColor =
                                                        spotShadowColor.copy(
                                                            alpha = 0.6f,
                                                        ),
                                                    ambientColor = Color.Transparent,
                                                ),
                                    ) {
                                        AsyncImage(
                                            model =
                                                ImageRequest
                                                    .Builder(LocalPlatformContext.current)
                                                    .data(screenDataState.thumbnailURL)
                                                    .diskCachePolicy(CachePolicy.ENABLED)
                                                    .diskCacheKey(screenDataState.thumbnailURL + "BIGGER")
                                                    .crossfade(550)
                                                    .build(),
                                            contentDescription = "",
                                            onSuccess = {
                                                sharedViewModel.setBitmap(
                                                    it.result.image.toImageBitmap(),
                                                )
                                            },
                                            contentScale = ContentScale.Crop,
                                            placeholder = ColorPainter(androidx.compose.ui.graphics.Color(0xFF2A2A2A)),
                                            modifier =
                                                Modifier
                                                    .align(Alignment.Center)
                                                    .padding(3.dp)
                                                    .fillMaxWidth()
                                                    .background(Color.Transparent)
                                                    .aspectRatio(
                                                        if (!screenDataState.isVideo) 1f else 16f / 9,
                                                    ).clip(
                                                        RoundedCornerShape(15.dp),
                                                    ).alpha(
                                                        if (!screenDataState.isVideo || !shouldShowVideo) 1f else 0f,
                                                    ),
                                        )
                                    }

                                    // IS VIDEO => Show Video
                                    androidx.compose.animation.AnimatedVisibility(
                                        visible = screenDataState.isVideo && shouldShowVideo,
                                        modifier = Modifier.align(Alignment.Center),
                                    ) {
                                        var internalShowSubtitle by rememberSaveable {
                                            mutableStateOf(true)
                                        }
                                        Box(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .aspectRatio(16f / 9)
                                                    .clip(
                                                        RoundedCornerShape(15.dp),
                                                    ).background(
                                                        md_theme_dark_background,
                                                    ),
                                        ) {
                                            // Player
                                            Box(Modifier.fillMaxSize()) {
                                                MediaPlayerViewWithSubtitle(
                                                    player = sharedViewModel.playerConnection.player,
                                                    modifier = Modifier.align(Alignment.Center),
                                                    shouldShowSubtitle = internalShowSubtitle,
                                                    shouldPip = false,
                                                    shouldScaleDownSubtitle = true,
                                                    timelineState = timelineState,
                                                    lyricsData = screenDataState.lyricsData?.lyrics,
                                                    translatedLyricsData = screenDataState.lyricsData?.translatedLyrics?.first,
                                                    isInPipMode = isInPipMode,
                                                    mainTextStyle = typo().bodyLarge,
                                                    translatedTextStyle = typo().bodyMedium,
                                                )
                                            }
                                            Box(
                                                modifier =
                                                    Modifier
                                                        .fillMaxSize()
                                                        .clickable(
                                                            onClick = { showHideFullscreenOverlay = !showHideFullscreenOverlay },
                                                            indication = null,
                                                            interactionSource =
                                                                remember {
                                                                    MutableInteractionSource()
                                                                },
                                                        ),
                                            ) {
                                                Crossfade(
                                                    targetState = showHideFullscreenOverlay,
                                                ) {
                                                    if (it) {
                                                        Box(
                                                            modifier =
                                                                Modifier
                                                                    .fillMaxSize()
                                                                    .background(
                                                                        Brush.verticalGradient(
                                                                            colorStops =
                                                                                arrayOf(
                                                                                    0.03f to blackMoreOverlay,
                                                                                    0.15f to overlay,
                                                                                    0.8f to Color.Transparent,
                                                                                ),
                                                                        ),
                                                                    ),
                                                        ) {
                                                            DimIconButton(onClick = {
                                                                onDismiss()
                                                                navController.navigate(
                                                                    FullscreenDestination,
                                                                )
                                                            }, Modifier.align(Alignment.TopEnd)) {
                                                                Icon(
                                                                    painter = painterResource(com.metrolist.music.R.drawable.baseline_fullscreen_24),
                                                                    contentDescription = "",
                                                                    tint = Color.White,
                                                                )
                                                            }
                                                            Row(
                                                                Modifier
                                                                    .align(Alignment.Center)
                                                                    .fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceEvenly,
                                                            ) {
                                                                FilledTonalIconButton(
                                                                    colors =
                                                                        IconButtonDefaults.iconButtonColors().copy(
                                                                            containerColor = Color.Transparent,
                                                                        ),
                                                                    modifier =
                                                                        Modifier
                                                                            .size(48.dp)
                                                                            .aspectRatio(1f)
                                                                            .clip(
                                                                                CircleShape,
                                                                            ),
                                                                    onClick = {
                                                                        sharedViewModel.onUIEvent(UIEvent.Backward)
                                                                    },
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Rounded.Replay5,
                                                                        tint = Color.White,
                                                                        contentDescription = "",
                                                                        modifier =
                                                                            Modifier
                                                                                .size(36.dp)
                                                                                .alpha(0.8f),
                                                                    )
                                                                }
                                                                FilledTonalIconButton(
                                                                    colors =
                                                                        IconButtonDefaults.iconButtonColors().copy(
                                                                            containerColor = Color.Transparent,
                                                                        ),
                                                                    modifier =
                                                                        Modifier
                                                                            .size(48.dp)
                                                                            .aspectRatio(1f)
                                                                            .clip(
                                                                                CircleShape,
                                                                            ),
                                                                    onClick = {
                                                                        sharedViewModel.onUIEvent(UIEvent.Forward)
                                                                    },
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Rounded.Forward5,
                                                                        tint = Color.White,
                                                                        contentDescription = "",
                                                                        modifier =
                                                                            Modifier
                                                                                .size(36.dp)
                                                                                .alpha(0.8f),
                                                                    )
                                                                }
                                                            }
                                                            if (screenDataState.lyricsData != null) {
                                                                DimIconButton(onClick = {
                                                                    internalShowSubtitle = !internalShowSubtitle
                                                                }, Modifier.align(Alignment.BottomEnd)) {
                                                                    Icon(
                                                                        imageVector =
                                                                            if (internalShowSubtitle) {
                                                                                Icons.Filled.SubtitlesOff
                                                                            } else {
                                                                                Icons.Filled.Subtitles
                                                                            },
                                                                        contentDescription = "",
                                                                        tint = Color.White,
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else if (pageTrack != null) {
                                    // Adjacent page thumbnail
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier =
                                            Modifier
                                                .align(Alignment.Center)
                                                .background(Color.Transparent)
                                                .shadow(
                                                    elevation = 3.dp,
                                                    shape = RoundedCornerShape(15.dp),
                                                    spotColor = Color.Black.copy(alpha = 0.3f),
                                                    ambientColor = Color.Transparent,
                                                ),
                                    ) {
                                        val adjacentThumbUrl =
                                            pageTrack.thumbnail
                                        AsyncImage(
                                            model =
                                                ImageRequest
                                                    .Builder(LocalPlatformContext.current)
                                                    .data(adjacentThumbUrl)
                                                    .diskCachePolicy(CachePolicy.ENABLED)
                                                    .diskCacheKey(adjacentThumbUrl)
                                                    .crossfade(300)
                                                    .build(),
                                            contentDescription = pageTrack.title,
                                            contentScale = ContentScale.Crop,
                                            placeholder = ColorPainter(androidx.compose.ui.graphics.Color(0xFF2A2A2A)),
                                            error = ColorPainter(androidx.compose.ui.graphics.Color(0xFF2A2A2A)),
                                            onSuccess = { state ->
                                                palettePageScope.launch {
                                                    pagePaletteState.generate(
                                                        state.result.image.toImageBitmap(),
                                                    )
                                                }
                                            },
                                            modifier =
                                                Modifier
                                                    .align(Alignment.Center)
                                                    .padding(3.dp)
                                                    .fillMaxWidth()
                                                    .aspectRatio(1f)
                                                    .clip(RoundedCornerShape(15.dp)),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                CenterAlignedTopAppBar(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .onGloballyPositioned {
                                topAppBarHeightDp =
                                    with(localDensity) {
                                        it.size.height
                                            .toDp()
                                            .value
                                            .toInt()
                                    }
                            },
                    colors =
                        TopAppBarDefaults.topAppBarColors().copy(
                            containerColor = Color.Transparent,
                        ),
                    windowInsets =
                        TopAppBarDefaults.windowInsets.only(
                            WindowInsetsSides.Top,
                        ),
                    title = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = stringResource(com.metrolist.music.R.string.now_playing_upper),
                                style = typo().bodyMedium,
                                color = Color.White,
                            )
                            Text(
                                text = screenDataState.playlistName,
                                style = typo().labelMedium,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight(align = Alignment.CenterVertically)
                                        .basicMarquee(
                                            iterations = Int.MAX_VALUE,
                                            animationMode = MarqueeAnimationMode.Immediately,
                                        ).focusable(),
                            )
                        }
                    },
                    navigationIcon = {
                        DimIconButton(
                            modifier = Modifier.padding(start = 20.dp),
                            onClick = { onDismiss() },
                        ) {
                            Icon(
                                imageVector = dismissIcon,
                                contentDescription = "",
                                tint = Color.White,
                            )
                        }
                    },
                    actions = {
                        // Desktop mini player button (JVM only)
                        if (false) {
                            DimIconButton(onClick = { /* toggleMiniPlayer() */ }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                    contentDescription = "Mini Player",
                                    tint = Color.White,
                                )
                            }
                        }
                        DimIconButton(
                            modifier = Modifier.padding(end = 20.dp),
                            onClick = { showSheet = true },
                        ) {
                            Icon(
                                painter = painterResource(com.metrolist.music.R.drawable.baseline_more_vert_24),
                                contentDescription = "",
                                tint = Color.White,
                            )
                        }
                    },
                )
                Column {
                    Spacer(
                        modifier =
                            Modifier.height(
                                topAppBarHeightDp.dp,
                            ),
                    )
                    Box {
                        Column(
                            Modifier
                                .fillMaxWidth(),
                        ) {
                            Spacer(
                                modifier =
                                    Modifier
                                        .animateContentSize()
                                        .height(
                                            middleLayoutPaddingDp.dp,
                                        ).fillMaxWidth(),
                            )

                            // Reserve vertical space for the ArtworkPager's thumbnail layer above.
                            // Spacer has no pointer input so it does not block the pager swipe gesture.
                            Spacer(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp)
                                        .onGloballyPositioned { coords ->
                                            middleLayoutHeightDp =
                                                with(localDensity) {
                                                    coords.size.height
                                                        .toDp()
                                                        .value
                                                        .toInt()
                                                }
                                        }.aspectRatio(1f),
                            )

                            Spacer(
                                modifier =
                                    Modifier
                                        .animateContentSize()
                                        .height(
                                            middleLayoutPaddingDp.dp,
                                        ).fillMaxWidth(),
                            )

                            // Info Layout
                            Box {
                                Column(
                                    Modifier
                                        .padding(horizontal = 28.dp)
                                        .alpha(controlLayoutAlpha)
                                        .onGloballyPositioned {
                                            infoLayoutHeightDp =
                                                with(localDensity) {
                                                    it.size.height
                                                        .toDp()
                                                        .value
                                                        .toInt()
                                                }
                                        },
                                ) {
                                    Row(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        AnimatedVisibility(screenDataState.canvasData != null) {
                                            AsyncImage(
                                                model =
                                                    ImageRequest
                                                        .Builder(LocalPlatformContext.current)
                                                        .data(screenDataState.thumbnailURL)
                                                        .diskCachePolicy(CachePolicy.ENABLED)
                                                        .diskCacheKey(screenDataState.thumbnailURL + "BIGGER")
                                                        .crossfade(true)
                                                        .build(),
                                                placeholder = ColorPainter(androidx.compose.ui.graphics.Color(0xFF2A2A2A)),
                                                error = ColorPainter(androidx.compose.ui.graphics.Color(0xFF2A2A2A)),
                                                contentDescription = null,
                                                contentScale = ContentScale.FillWidth,
                                                modifier =
                                                    Modifier
                                                        .heightIn(0.dp, 55.dp)
                                                        .width(55.dp)
                                                        .padding(end = 10.dp)
                                                        .clip(
                                                            RoundedCornerShape(4.dp),
                                                        ).align(Alignment.CenterVertically),
                                            )
                                        }

                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                text = screenDataState.nowPlayingTitle,
                                                style = typo().headlineMedium,
                                                maxLines = 1,
                                                color = Color.White,
                                                modifier =
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .wrapContentHeight(align = Alignment.CenterVertically)
                                                        .basicMarquee(
                                                            iterations = Int.MAX_VALUE,
                                                            animationMode = MarqueeAnimationMode.Immediately,
                                                        ).focusable(),
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            LazyRow(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                item(screenDataState.isExplicit) {
                                                    AnimatedVisibility(visible = screenDataState.isExplicit) {
                                                        ExplicitBadge(
                                                            modifier =
                                                                Modifier
                                                                    .size(20.dp)
                                                                    .padding(end = 4.dp)
                                                                    .weight(1f),
                                                        )
                                                    }
                                                }
                                                item(screenDataState.artistName) {
                                                    Text(
                                                        text = screenDataState.artistName,
                                                        style = typo().bodyMedium,
                                                        maxLines = 1,
                                                        modifier =
                                                            Modifier
                                                                .fillMaxWidth()
                                                                .wrapContentHeight(align = Alignment.CenterVertically)
                                                                .basicMarquee(
                                                                    iterations = Int.MAX_VALUE,
                                                                    animationMode = MarqueeAnimationMode.Immediately,
                                                                ).focusable()
                                                                .clickable(
                                                                    indication = null,
                                                                    interactionSource = remember { MutableInteractionSource() },
                                                                ) {
                                                                    val song = sharedViewModel.nowPlayingState.value?.songEntity
                                                                    (screenDataState.songInfoData?.authorId)?.let { channelId ->
                                                                        onDismiss()
                                                                        navController.navigate(
                                                                            ArtistDestination(
                                                                                channelId = channelId,
                                                                            ),
                                                                        )
                                                                    }
                                                                },
                                                    )
                                                }
                                            }
                                        }
                                        if (sharedViewModel.isUserLoggedIn()) {
                                            Spacer(modifier = Modifier.size(16.dp))
                                            Crossfade(
                                                targetState = likeStatus,
                                            ) {
                                                if (it) {
                                                    DimIconButton(
                                                        modifier =
                                                            Modifier
                                                                .size(24.dp)
                                                                .aspectRatio(1f),
                                                        onClick = {
                                                            sharedViewModel.addToYouTubeLiked()
                                                        },
                                                    ) {
                                                        Icon(imageVector = Icons.Rounded.CheckCircle, tint = Color.White, contentDescription = "")
                                                    }
                                                } else {
                                                    DimIconButton(
                                                        modifier =
                                                            Modifier
                                                                .size(24.dp)
                                                                .aspectRatio(1f),
                                                        onClick = {
                                                            sharedViewModel.addToYouTubeLiked()
                                                        },
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Rounded.AddCircleOutline,
                                                            tint = Color.White,
                                                            contentDescription = "",
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.size(12.dp))
                                        HeartCheckBox(checked = controllerState.isLiked, size = 32) {
                                            sharedViewModel.onUIEvent(UIEvent.ToggleLike)
                                        }
                                    }
                                    if (true) {
                                        // Real Slider
                                        Box(
                                            Modifier
                                                .padding(
                                                    top = 15.dp,
                                                )
                                                .onGloballyPositioned { coords ->
                                                    val visible = coords.isAttached
                                                    shouldShowToolbar = !visible && isExpanded && mainScrollState.value > 0
                                                },
                                        ) {
                                            Box(
                                                modifier =
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .height(24.dp),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Crossfade(timelineState.loading) {
                                                    if (it) {
                                                        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                                                            LinearProgressIndicator(
                                                                modifier =
                                                                    Modifier
                                                                        .fillMaxWidth()
                                                                        .height(2.dp)
                                                                        .padding(
                                                                            horizontal = 3.dp,
                                                                        ).clip(
                                                                            RoundedCornerShape(15.dp),
                                                                        ),
                                                                color = Color.Gray,
                                                                trackColor = Color.DarkGray,
                                                                strokeCap = StrokeCap.Round,
                                                            )
                                                        }
                                                    } else {
                                                        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                                                            LinearProgressIndicator(
                                                                progress = { timelineState.bufferedPercent.toFloat() / 100 },
                                                                modifier =
                                                                    Modifier
                                                                        .fillMaxWidth()
                                                                        .height(2.dp)
                                                                        .padding(
                                                                            horizontal = 3.dp,
                                                                        ).clip(
                                                                            RoundedCornerShape(15.dp),
                                                                        ),
                                                                color = Color.Gray,
                                                                trackColor =
                                                                    Color.Gray.copy(
                                                                        alpha = 0.6f,
                                                                    ),
                                                                strokeCap = StrokeCap.Round,
                                                                drawStopIndicator = {},
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                                                Slider(
                                                    value = sliderValue,
                                                    onValueChangeFinished = {
                                                        isSliding = false
                                                        sharedViewModel.onUIEvent(
                                                            UIEvent.UpdateProgress(sliderValue),
                                                        )
                                                    },
                                                    onValueChange = {
                                                        isSliding = true
                                                        sliderValue = it
                                                    },
                                                    valueRange = 0f..100f,
                                                    modifier =
                                                        Modifier
                                                            .fillMaxWidth()
                                                            .padding(top = 3.dp)
                                                            .align(
                                                                Alignment.TopCenter,
                                                            ),
                                                    track = { sliderState ->
                                                        SliderDefaults.Track(
                                                            modifier =
                                                                Modifier
                                                                    .height(3.dp),
                                                            enabled = true,
                                                            sliderState = sliderState,
                                                            colors =
                                                                SliderDefaults.colors().copy(
                                                                    thumbColor = sliderTrackColor,
                                                                    activeTrackColor = sliderTrackColor,
                                                                    inactiveTrackColor = Color.Transparent,
                                                                ),
                                                            thumbTrackGapSize = 0.dp,
                                                            drawTick = { _, _ -> },
                                                            drawStopIndicator = null,
                                                        )
                                                    },
                                                    thumb = {
                                                        SliderDefaults.Thumb(
                                                            modifier =
                                                                Modifier
                                                                    .height(18.dp)
                                                                    .width(8.dp)
                                                                    .padding(
                                                                        vertical = 4.dp,
                                                                    ),
                                                            thumbSize = DpSize(8.dp, 8.dp),
                                                            interactionSource =
                                                                remember {
                                                                    MutableInteractionSource()
                                                                },
                                                            colors =
                                                                SliderDefaults.colors().copy(
                                                                    thumbColor = sliderTrackColor,
                                                                    activeTrackColor = sliderTrackColor,
                                                                    inactiveTrackColor = Color.Transparent,
                                                                ),
                                                            enabled = true,
                                                        )
                                                    },
                                                )
                                            }
                                        }
                                        // Time Layout
                                        Row(
                                            Modifier
                                                .fillMaxWidth(),
                                        ) {
                                            Text(
                                                text = formatDuration((timelineState.total * (sliderValue / 100f)).roundToLong()),
                                                style = typo().bodyMedium,
                                                modifier = Modifier.weight(1f),
                                                textAlign = TextAlign.Left,
                                            )
                                            AnimatedVisibility(
                                                enter = fadeIn(),
                                                exit = fadeOut(),
                                                visible = timelineState.isCrossfading,
                                            ) {
                                                Text(
                                                    text = stringResource(com.metrolist.music.R.string.crossfading),
                                                    style = typo().bodyMedium,
                                                    modifier = Modifier.weight(1f),
                                                    textAlign = TextAlign.Center,
                                                )
                                            }
                                            Text(
                                                text = formatDuration(timelineState.total),
                                                style = typo().bodyMedium,
                                                modifier = Modifier.weight(1f),
                                                textAlign = TextAlign.Right,
                                            )
                                        }

                                        Spacer(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .height(5.dp),
                                        )
                                        // Control Button Layout
                                        PlayerControlLayout(
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            // control events handled by sharedViewModel
                                        }
                                    } else {
                                        Spacer(Modifier.height(16.dp))
                                    }
                                    // Bottom Buttons Row
                                    Row(
                                        modifier =
                                            Modifier
                                                .height(32.dp)
                                                .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        // Info Button (Left)
                                        DimIconButton(
                                            modifier =
                                                Modifier
                                                    .size(24.dp)
                                                    .aspectRatio(1f),
                                            onClick = {
                                                showInfoBottomSheet = true
                                            },
                                        ) {
                                            Icon(imageVector = Icons.Outlined.Info, tint = Color.White, contentDescription = "")
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            // Add to Playlist Button
                                            DimIconButton(
                                                modifier = Modifier.size(36.dp),
                                                onClick = {
                                                    showAddToPlaylistDirectly = true
                                                },
                                            ) {
                                                Icon(
                                                    painter = painterResource(com.metrolist.music.R.drawable.addtoqueue),
                                                    tint = Color.White,
                                                    contentDescription = "Add to Playlist",
                                                    modifier = Modifier.size(16.dp),
                                                )
                                            }

                                            // Queue Button
                                            DimIconButton(
                                                modifier = Modifier.size(36.dp),
                                                onClick = {
                                                    showQueueBottomSheet = true
                                                },
                                            ) {
                                                Icon(
                                                    painter = painterResource(com.metrolist.music.R.drawable.queue),
                                                    tint = Color.White,
                                                    contentDescription = "",
                                                    modifier = Modifier.size(16.dp),
                                                )
                                            }
                                        }
                                    }
                                }
                                this@Column.AnimatedVisibility(
                                    visible = !showHideControlLayout,
                                    enter = fadeIn(),
                                    exit = fadeOut(),
                                ) {
                                    Box(
                                        modifier =
                                            Modifier
                                                .height(
                                                    infoLayoutHeightDp.dp,
                                                ).fillMaxWidth()
                                                .padding(
                                                    horizontal = 20.dp,
                                                ).padding(
                                                    bottom = 20.dp,
                                                ).clickable(
                                                    onClick = {
                                                        if (mainScrollState.value == 0) {
                                                            showHideJob = true
                                                            showHideControlLayout = !showHideControlLayout
                                                        }
                                                    },
                                                    indication = null,
                                                    interactionSource =
                                                        remember {
                                                            MutableInteractionSource()
                                                        },
                                                ),
                                        contentAlignment = Alignment.BottomStart,
                                    ) {
                                        Column {
                                            this@Column.AnimatedVisibility(canvasSubtitleLineIndex > -1) {
                                                // Canvas subtitle - above artist
                                                val lineText =
                                                    screenDataState.lyricsData
                                                        ?.lyrics
                                                        ?.lines
                                                        ?.getOrNull(canvasSubtitleLineIndex)
                                                        ?.words
                                                        ?.replace(RICH_SYNC_TIMESTAMP_REGEX, "")
                                                        ?.trim()
                                                if (!lineText.isNullOrBlank()) {
                                                    Text(
                                                        modifier =
                                                            Modifier
                                                                .padding(bottom = 8.dp)
                                                                .basicMarquee(
                                                                    iterations = Int.MAX_VALUE,
                                                                    animationMode = MarqueeAnimationMode.Immediately,
                                                                ).focusable(),
                                                        text = lineText,
                                                        style = typo().bodyMedium,
                                                        color = Color.White,
                                                        maxLines = 1,
                                                    )
                                                }
                                            }
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                val thumb = screenDataState.songInfoData?.authorThumbnail
                                                AsyncImage(
                                                    model =
                                                        ImageRequest
                                                            .Builder(LocalPlatformContext.current)
                                                            .data(thumb)
                                                            .diskCachePolicy(CachePolicy.ENABLED)
                                                            .diskCacheKey(thumb)
                                                            .crossfade(550)
                                                            .build(),
                                                    placeholder = ColorPainter(androidx.compose.ui.graphics.Color(0xFF2A2A2A)),
                                                    error = ColorPainter(androidx.compose.ui.graphics.Color(0xFF2A2A2A)),
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier =
                                                        Modifier
                                                            .size(42.dp)
                                                            .clip(
                                                                CircleShape,
                                                            ),
                                                )
                                                Spacer(modifier = Modifier.size(12.dp))
                                                Text(
                                                    text = screenDataState.songInfoData?.author ?: "",
                                                    style = typo().labelMedium,
                                                    color = Color.White,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        // Touch Area (canvas mode tap to toggle controls)
                        this@Column.AnimatedVisibility(
                            visible = screenDataState.canvasData != null,
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(
                                            (middleLayoutPaddingDp * 2 + middleLayoutHeightDp).dp,
                                        ).clickable(
                                            onClick = {
                                                if (mainScrollState.value == 0) {
                                                    showHideJob = true
                                                    showHideControlLayout = !showHideControlLayout
                                                }
                                            },
                                            indication = null,
                                            interactionSource =
                                                remember {
                                                    MutableInteractionSource()
                                                },
                                        ),
                            )
                        }
                    }
                    Column(Modifier.padding(horizontal = 20.dp)) {
                        // Lyrics Layout
                        AnimatedVisibility(
                            visible = screenDataState.lyricsData != null,
                            modifier = Modifier.padding(top = 10.dp),
                        ) {
                            ElevatedCard(
                                shape = RoundedCornerShape(15.dp),
                                colors =
                                    CardDefaults.elevatedCardColors().copy(
                                        containerColor = startColor.value,
                                    ),
                            ) {
                                Column(modifier = Modifier.padding(15.dp)) {
                                    Spacer(modifier = Modifier.height(5.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Lyrics Preview",
                                            style = typo().labelMedium,
                                            color = Color.White,
                                        )
                                        if (screenDataState.lyricsData?.translatedLyrics?.second == LyricsProvider.AI) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            AIBadge()
                                        }
                                        Spacer(modifier = Modifier.weight(1f))
                                        val canVoteLyrics =
                                            screenDataState.lyricsData?.lyricsProvider == LyricsProvider.XEVRAE &&
                                                screenDataState.lyricsData
                                                    ?.lyrics
                                                    ?.simpMusicLyrics != null
                                        val canVoteTranslatedLyrics =
                                            screenDataState.lyricsData?.translatedLyrics?.second == LyricsProvider.XEVRAE &&
                                                screenDataState.lyricsData
                                                    ?.translatedLyrics
                                                    ?.first
                                                    ?.simpMusicLyrics != null
                                        if (canVoteLyrics || canVoteTranslatedLyrics) {
                                            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                                                DimIconButton(
                                                    onClick = {
                                                        showVoteDialog = true
                                                    },
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.ThumbsUpDown,
                                                        contentDescription = stringResource(com.metrolist.music.R.string.rate_lyrics),
                                                        tint = Color.White,
                                                        modifier = Modifier.size(16.dp),
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }
                                        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                                            TextButton(
                                                onClick = {
                                                    showFullscreenLyrics = true
                                                },
                                                contentPadding = PaddingValues(0.dp),
                                                modifier =
                                                    Modifier
                                                        .height(20.dp)
                                                        .wrapContentWidth(),
                                            ) {
                                                Text(text = stringResource(com.metrolist.music.R.string.show))
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(18.dp))
                                    Box(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .height(300.dp),
                                    ) {
                                        screenDataState.lyricsData?.let {
                                            LyricsView(
                                                lyricsData = it,
                                                timeLine = sharedViewModel.timeline,
                                                userScrollEnabled = false,
                                                onLineClick = { f ->
                                                    sharedViewModel.onUIEvent(UIEvent.UpdateProgress(f))
                                                },
                                            )
                                        }
                                    }
                                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                                        Text(
                                            text =
                                                when (screenDataState.lyricsData?.lyrics?.syncType) {
                                                    "LINE_SYNCED" -> stringResource(com.metrolist.music.R.string.line_synced)
                                                    "RICH_SYNCED" -> stringResource(com.metrolist.music.R.string.rich_synced)
                                                    else -> stringResource(com.metrolist.music.R.string.unsynced)
                                                },
                                            style = typo().bodySmall,
                                            textAlign = TextAlign.End,
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 10.dp),
                                        )
                                        Text(
                                            text =
                                                when (screenDataState.lyricsData?.lyricsProvider) {
                                                    LyricsProvider.XEVRAE -> {
                                                        stringResource(com.metrolist.music.R.string.lyrics_provider_xevrae)
                                                    }
                                                    LyricsProvider.LRCLIB -> {
                                                        stringResource(com.metrolist.music.R.string.lyrics_provider_lrc)
                                                    }
                                                    LyricsProvider.YOUTUBE -> {
                                                        stringResource(com.metrolist.music.R.string.lyrics_provider_youtube)
                                                    }
                                                    LyricsProvider.SPOTIFY -> {
                                                        stringResource(com.metrolist.music.R.string.spotify_lyrics_provider)
                                                    }
                                                    LyricsProvider.OFFLINE -> {
                                                        stringResource(com.metrolist.music.R.string.offline_mode)
                                                    }
                                                    LyricsProvider.BETTER_LYRICS -> {
                                                        stringResource(com.metrolist.music.R.string.lyrics_provider_betterlyrics)
                                                    }
                                                    else -> {
                                                        ""
                                                    }
                                                },
                                            style = typo().bodySmall,
                                            textAlign = TextAlign.End,
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth(),
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        AnimatedVisibility(visible = screenDataState.songInfoData != null) {
                            ElevatedCard(
                                shape = RoundedCornerShape(15.dp),
                                colors =
                                    CardDefaults.elevatedCardColors().copy(
                                        containerColor = startColor.value,
                                    ),
                                modifier = Modifier.clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                ) {
                                    (screenDataState.songInfoData?.authorId)?.let { channelId ->
                                        onDismiss()
                                        navController.navigate(ArtistDestination(channelId = channelId))
                                    }
                                },
                            ) {
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .height(250.dp),
                                ) {
                                    val thumb = screenDataState.songInfoData?.authorThumbnail
                                    AsyncImage(
                                        model =
                                            ImageRequest
                                                .Builder(LocalPlatformContext.current)
                                                .data(thumb)
                                                .diskCachePolicy(CachePolicy.ENABLED)
                                                .diskCacheKey(thumb)
                                                .crossfade(550)
                                                .build(),
                                        placeholder = ColorPainter(androidx.compose.ui.graphics.Color(0xFF2A2A2A)),
                                        error = ColorPainter(androidx.compose.ui.graphics.Color(0xFF2A2A2A)),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier =
                                            Modifier
                                                .fillMaxSize()
                                                .alpha(0.8f)
                                                .clip(
                                                    RoundedCornerShape(15.dp),
                                                ),
                                    )
                                    Box(
                                        modifier =
                                            Modifier
                                                .padding(15.dp)
                                                .fillMaxSize(),
                                    ) {
                                        Column(Modifier.align(Alignment.TopStart)) {
                                            Spacer(modifier = Modifier.height(5.dp))
                                            Text(
                                                text = stringResource(com.metrolist.music.R.string.artists),
                                                style = typo().labelMedium,
                                                color = Color.White,
                                            )
                                        }
                                        Column(Modifier.align(Alignment.BottomStart)) {
                                            Text(
                                                text = screenDataState.songInfoData?.author ?: "",
                                                style = typo().labelMedium,
                                                color = Color.White,
                                            )
                                            Spacer(modifier = Modifier.height(5.dp))
                                            Text(
                                                text = screenDataState.songInfoData?.subscribers ?: "",
                                                style = typo().bodySmall,
                                                textAlign = TextAlign.End,
                                            )
                                            Spacer(modifier = Modifier.height(5.dp))
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        AnimatedVisibility(visible = screenDataState.songInfoData != null) {
                            ElevatedCard(
                                shape = RoundedCornerShape(15.dp),
                                colors =
                                    CardDefaults.elevatedCardColors().copy(
                                        containerColor = startColor.value,
                                    ),
                            ) {
                                Column(
                                    Modifier
                                        .padding(15.dp)
                                        .fillMaxWidth(),
                                ) {
                                    Spacer(modifier = Modifier.height(5.dp))
                                    Text(
                                        text = stringResource(com.metrolist.music.R.string.published_at, screenDataState.songInfoData?.uploadDate ?: ""),
                                        style = typo().labelSmall,
                                        color = Color.White,
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text =
                                            stringResource(
                                                com.metrolist.music.R.string.view_count,
                                                "%,d".format(screenDataState.songInfoData?.viewCount),
                                            ),
                                        style = typo().labelMedium,
                                        color = Color.White,
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text =
                                            stringResource(
                                                com.metrolist.music.R.string.like_and_dislike,
                                                screenDataState.songInfoData?.like ?: 0,
                                                screenDataState.songInfoData?.dislike ?: 0,
                                            ),
                                        style = typo().bodyMedium,
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = stringResource(com.metrolist.music.R.string.description),
                                        style = typo().labelSmall,
                                        color = Color.White,
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    DescriptionView(
                                        text = screenDataState.songInfoData?.description ?: "",
                                        onTimeClicked = { raw ->
                                            val timestamp = parseTimestampToMilliseconds(raw)
                                            if (timestamp != 0L && timestamp < timelineState.total) {
                                                sharedViewModel.onUIEvent(
                                                    UIEvent.UpdateProgress(
                                                        ((timestamp * 100) / timelineState.total).toFloat(),
                                                    ),
                                                )
                                            }
                                        },
                                        onURLClicked = { url ->
                                            uriHandler.openUri(url)
                                        },
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Spacer(
                        modifier =
                            Modifier.height(
                                with(localDensity) { WindowInsets.systemBars.getBottom(localDensity).toDp() },
                            ),
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = shouldShowToolbar && isExpanded,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
        ) {
            ElevatedCard(
                elevation = CardDefaults.elevatedCardElevation(10.dp),
                shape = RectangleShape,
                colors =
                    CardDefaults.elevatedCardColors(
                        containerColor =
                            startColor.value
                                .copy(
                                    red = startColor.value.red - 0.05f,
                                    green = startColor.value.green - 0.05f,
                                    blue = startColor.value.blue - 0.05f,
                                ),
                    ),
                modifier =
                    Modifier
                        .clipToBounds()
                        .wrapContentHeight()
                        .fillMaxWidth(),
            ) {
                Box(
                    modifier =
                        Modifier.padding(
                            top = with(localDensity) { WindowInsets.statusBars.getTop(localDensity).toDp() },
                        ),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .padding(
                                    vertical = 8.dp,
                                    horizontal = 15.dp,
                                ).fillMaxWidth(),
                    ) {
                        Spacer(modifier = Modifier.size(8.dp))
                        Box(modifier = Modifier.weight(1F)) {
                            Column(
                                Modifier
                                    .wrapContentHeight(),
                            ) {
                                Text(
                                    text = screenDataState.nowPlayingTitle,
                                    style = typo().bodyMedium,
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
                                LazyRow(verticalAlignment = Alignment.CenterVertically) {
                                    item {
                                        AnimatedVisibility(visible = screenDataState.isExplicit) {
                                            ExplicitBadge(
                                                modifier =
                                                    Modifier
                                                        .size(20.dp)
                                                        .padding(end = 4.dp)
                                                        .weight(1f),
                                            )
                                        }
                                    }
                                    item(key = screenDataState.artistName) {
                                        Text(
                                            text = screenDataState.artistName,
                                            style = typo().bodySmall,
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
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(15.dp))
                        HeartCheckBox(checked = controllerState.isLiked, size = 30) {
                            sharedViewModel.onUIEvent(UIEvent.ToggleLike)
                        }
                        Spacer(modifier = Modifier.width(15.dp))
                        Crossfade(targetState = timelineState.loading, label = "") {
                            if (it) {
                                Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = Color.LightGray,
                                        strokeWidth = 3.dp,
                                    )
                                }
                            } else {
                                PlayPauseButton(isPlaying = controllerState.isPlaying, modifier = Modifier.size(48.dp)) {
                                    sharedViewModel.onUIEvent(UIEvent.PlayPause)
                                }
                            }
                        }
                    }
                    Box(
                        modifier =
                            Modifier
                                .wrapContentSize(Alignment.Center)
                                .align(Alignment.BottomCenter),
                    ) {
                        LinearProgressIndicator(
                            progress = { timelineState.current.toFloat() / timelineState.total },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(
                                        color = Color.Transparent,
                                        shape = RoundedCornerShape(4.dp),
                                    ),
                            color = Color.White,
                            trackColor = Color.Gray.copy(alpha = 0.4f),
                            strokeCap = StrokeCap.Round,
                            drawStopIndicator = {},
                        )
                    }
                }
            }
        }
    }
}
