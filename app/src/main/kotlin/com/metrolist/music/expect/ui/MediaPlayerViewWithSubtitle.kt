package com.metrolist.music.expect.ui

import android.app.PictureInPictureParams
import android.content.Context
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_SURFACE_VIEW
import androidx.media3.ui.compose.state.rememberPresentationState
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.metrolist.music.models.xevrae.Lyrics
import com.metrolist.music.models.xevrae.TimeLine
import com.metrolist.music.utils.Logger
import com.metrolist.music.extensions.KeepScreenOn
import kotlin.math.roundToInt

private val RICH_SYNC_TIMESTAMP_REGEX = Regex("""<\d{2}:\d{2}\.\d{2,3}>\s*""")

@Composable
@androidx.annotation.OptIn(UnstableApi::class)
fun MediaPlayerViewWithSubtitle(
    modifier: Modifier = Modifier,
    player: Player,
    shouldPip: Boolean = false,
    shouldShowSubtitle: Boolean,
    shouldScaleDownSubtitle: Boolean = false,
    isInPipMode: Boolean,
    timelineState: TimeLine,
    lyricsData: Lyrics? = null,
    translatedLyricsData: Lyrics? = null,
    mainTextStyle: TextStyle,
    translatedTextStyle: TextStyle,
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    var shouldEnterPipMode by rememberSaveable {
        mutableStateOf(false)
    }

    var videoRatio by rememberSaveable {
        mutableFloatStateOf(16f / 9)
    }

    var showArtwork by rememberSaveable {
        mutableStateOf(false)
    }

    var artworkUri by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var currentLineIndex by rememberSaveable {
        mutableIntStateOf(-1)
    }
    var currentTranslatedLineIndex by rememberSaveable {
        mutableIntStateOf(-1)
    }

    LaunchedEffect(key1 = timelineState) {
        val lines = lyricsData?.lines ?: return@LaunchedEffect
        val translatedLines = translatedLyricsData?.lines
        if (timelineState.current > 0L) {
            lines.indices.forEach { i ->
                val sentence = lines[i]
                val startTimeMs = sentence.startTimeMs.toLong()

                // estimate the end time of the current sentence based on the start time of the next sentence
                val endTimeMs =
                    if (i < lines.size - 1) {
                        lines[i + 1].startTimeMs.toLong()
                    } else {
                        // if this is the last sentence, set the end time to be some default value (e.g., 1 minute after the start time)
                        startTimeMs + 60000
                    }
                if (timelineState.current in startTimeMs..endTimeMs) {
                    currentLineIndex = i
                }
            }
            translatedLines?.indices?.forEach { i ->
                val sentence = translatedLines[i]
                val startTimeMs = sentence.startTimeMs.toLong()

                // estimate the end time of the current sentence based on the start time of the next sentence
                val endTimeMs =
                    if (i < translatedLines.size - 1) {
                        translatedLines[i + 1].startTimeMs.toLong()
                    } else {
                        // if this is the last sentence, set the end time to be some default value (e.g., 1 minute after the start time)
                        startTimeMs + 60000
                    }
                if (timelineState.current in startTimeMs..endTimeMs) {
                    currentTranslatedLineIndex = i
                }
            }
            if (lines.isNotEmpty() &&
                (
                    timelineState.current in (
                        0..(
                            lines.getOrNull(0)?.startTimeMs
                                ?: "0"
                        ).toLong()
                    )
                )
            ) {
                currentLineIndex = -1
                currentTranslatedLineIndex = -1
            }
        } else {
            currentLineIndex = -1
            currentTranslatedLineIndex = -1
        }
    }

    val playerListener =
        remember {
            object : Player.Listener {
                override fun onMediaItemTransition(
                    mediaItem: MediaItem?,
                    reason: Int,
                ) {
                    super.onMediaItemTransition(mediaItem, reason)
                    artworkUri = mediaItem?.mediaMetadata?.artworkUri?.toString()
                }

                override fun onTracksChanged(tracks: Tracks) {
                    super.onTracksChanged(tracks)
                    if (!tracks.groups.isEmpty()) {
                        for (arrayIndex in 0 until tracks.groups.size) {
                            var done = false
                            for (groupIndex in 0 until tracks.groups[arrayIndex].length) {
                                val sampleMimeType = tracks.groups[arrayIndex].getTrackFormat(groupIndex).sampleMimeType
                                if (sampleMimeType != null && sampleMimeType.contains("video")) {
                                    showArtwork = false
                                    done = true
                                    break
                                } else {
                                    showArtwork = true
                                }
                            }
                            if (done) {
                                break
                            }
                        }
                    }
                }

                override fun onVideoSizeChanged(videoSize: VideoSize) {
                    super.onVideoSizeChanged(videoSize)
                    videoRatio =
                        if (videoSize.width != 0 && videoSize.height != 0) {
                            videoSize.width.toFloat() / videoSize.height
                        } else {
                            16f / 9 // Default ratio if video size is not available
                        }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)
                    shouldEnterPipMode = isPlaying && shouldPip
                }
            }
        }

    DisposableEffect(Unit) {
        shouldEnterPipMode = shouldPip
        onDispose {
            shouldEnterPipMode = false
            player.removeListener(playerListener)
            Logger.w("MediaPlayerView", "Disposing ExoPlayer")
            if (shouldPip && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && activity != null) {
                val builder = PictureInPictureParams.Builder()

                // Add autoEnterEnabled for versions S and up
                builder.setAutoEnterEnabled(false)
                activity.setPictureInPictureParams(builder.build())
            }
        }
    }
    LaunchedEffect(player) {
        player.addListener(playerListener)
        (player as? ExoPlayer)?.videoScalingMode = C.VIDEO_SCALING_MODE_DEFAULT
    }

    val presentationState = rememberPresentationState(player)

    LaunchedEffect(shouldEnterPipMode) {
        Logger.w("MediaPlayerView", "shouldEnterPipMode: $shouldEnterPipMode")
    }

    if (shouldPip && Build.VERSION.SDK_INT < Build.VERSION_CODES.S && activity != null) {
        val currentShouldEnterPipMode by rememberUpdatedState(newValue = shouldEnterPipMode)
        DisposableEffect(context) {
            val onUserLeaveBehavior =
                Runnable {
                    if (currentShouldEnterPipMode) {
                        activity.enterPictureInPictureMode(PictureInPictureParams.Builder().build())
                    }
                }
            activity.addOnUserLeaveHintListener(
                onUserLeaveBehavior,
            )
            onDispose {
                activity.removeOnUserLeaveHintListener(
                    onUserLeaveBehavior,
                )
            }
        }
    }

    Box(
        modifier =
            modifier
                .then(
                    if (shouldPip && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && activity != null) {
                        Modifier.onGloballyPositioned { layoutCoordinates ->
                            val builder = PictureInPictureParams.Builder()

                            // Add autoEnterEnabled for versions S and up
                            builder.setAutoEnterEnabled(shouldEnterPipMode)
                            activity.setPictureInPictureParams(builder.build())
                        }
                    } else {
                        Modifier
                    },
                ),
        contentAlignment = Alignment.Center,
    ) {
        KeepScreenOn()
        Crossfade(showArtwork, label = "artwork_crossfade") {
            if (it) {
                AsyncImage(
                    model =
                        ImageRequest
                            .Builder(LocalContext.current)
                            .data(
                                artworkUri,
                            ).diskCachePolicy(CachePolicy.ENABLED)
                            .diskCacheKey(
                                artworkUri,
                            ).crossfade(550)
                            .build(),
                    contentDescription = null,
                    contentScale = ContentScale.FillHeight,
                    modifier =
                        Modifier
                            .fillMaxHeight()
                            .align(Alignment.Center),
                )
            } else {
                PlayerSurface(
                    player = player,
                    surfaceType = SURFACE_TYPE_SURFACE_VIEW,
                    modifier =
                        Modifier
                            .wrapContentSize()
                            .aspectRatio(if (videoRatio > 0f) videoRatio else 16f / 9)
                            .align(Alignment.Center),
                )

                if (presentationState.coverSurface) {
                    // Cover the surface that is being prepared with a shutter
                    Box(Modifier.background(Color.Black))
                }
            }
        }
        if (lyricsData != null && shouldShowSubtitle) {
            Crossfade(
                currentLineIndex != -1,
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxSize(),
                label = "subtitle_crossfade"
            ) {
                val lines = lyricsData.lines ?: return@Crossfade
                if (it) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .padding(bottom = if (isInPipMode || shouldScaleDownSubtitle) 10.dp else 40.dp)
                            .align(Alignment.BottomCenter),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Box(Modifier.fillMaxWidth(0.7f)) {
                            Column(
                                Modifier.align(Alignment.BottomCenter),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text =
                                        lines
                                            .getOrNull(currentLineIndex)
                                            ?.words
                                            ?.replace(RICH_SYNC_TIMESTAMP_REGEX, "")
                                            ?.trim()
                                            ?: return@Crossfade,
                                    style =
                                        mainTextStyle
                                            .let { style ->
                                                if (isInPipMode || shouldScaleDownSubtitle) {
                                                    style.copy(fontSize = style.fontSize * 0.8f)
                                                } else {
                                                    style
                                                }
                                            },
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    modifier =
                                        Modifier
                                            .padding(4.dp)
                                            .background(Color.Black.copy(alpha = 0.5f))
                                            .wrapContentWidth(),
                                )
                                Crossfade(translatedLyricsData?.lines != null, label = "translated_subtitle_crossfade") { translate ->
                                    val translateLines = translatedLyricsData?.lines ?: return@Crossfade
                                    if (translate) {
                                        Text(
                                            text =
                                                translateLines
                                                    .getOrNull(
                                                        currentTranslatedLineIndex,
                                                    )?.words
                                                    ?.replace(RICH_SYNC_TIMESTAMP_REGEX, "")
                                                    ?.trim()
                                                    ?: return@Crossfade,
                                            style =
                                                translatedTextStyle.let { style ->
                                                    if (isInPipMode || shouldScaleDownSubtitle) {
                                                        style.copy(fontSize = style.fontSize * 0.8f)
                                                    } else {
                                                        style
                                                    }
                                                },
                                            color = Color.Yellow,
                                            textAlign = TextAlign.Center,
                                            modifier =
                                                Modifier
                                                    .background(Color.Black.copy(alpha = 0.5f))
                                                    .wrapContentWidth(),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
