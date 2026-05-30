package com.metrolist.music.expect.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_SURFACE_VIEW
import androidx.media3.ui.compose.state.rememberPresentationState
import com.metrolist.music.models.xevrae.ScreenSizeInfo
import com.metrolist.music.utils.Logger
import com.metrolist.music.extensions.KeepScreenOn
import kotlin.math.roundToInt

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun MediaPlayerView(
    modifier: Modifier = Modifier,
    url: String,
    screenSize: ScreenSizeInfo,
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    var widthPx by rememberSaveable {
        mutableIntStateOf(screenSize.wPX)
    }

    var keepScreenOn by rememberSaveable {
        mutableStateOf(false)
    }

    val playerListener =
        remember {
            object : Player.Listener {
                override fun onVideoSizeChanged(videoSize: VideoSize) {
                    super.onVideoSizeChanged(videoSize)
                    Logger.w("MediaPlayerView", "Video size changed: ${videoSize.width} / ${videoSize.height}")
                    if (videoSize.width != 0 && videoSize.height != 0) {
                        val h = (videoSize.width.toFloat() / videoSize.height) * screenSize.hPX
                        Logger.w("MediaPlayerView", "Calculated width: $h")
                        widthPx = h.roundToInt()
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)
                    keepScreenOn = isPlaying
                }
            }
        }

    // Initialize ExoPlayer (Simplified without cache for now to avoid Koin/Hilt complexity in Composable)
    val exoPlayer =
        remember {
            ExoPlayer
                .Builder(context)
                .build()
                .apply {
                    addListener(playerListener)
                    videoScalingMode = C.VIDEO_SCALING_MODE_DEFAULT
                }
        }

    // Set MediaSource to ExoPlayer
    LaunchedEffect(url) {
        exoPlayer.setMediaItem(MediaItem.fromUri(url))
        exoPlayer.prepare()
        exoPlayer.play()
        exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
    }

    // Manage lifecycle events
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.removeListener(playerListener)
            exoPlayer.release()
        }
    }

    if (keepScreenOn) {
        KeepScreenOn()
    }

    val presentationState = rememberPresentationState(exoPlayer)
    Box(modifier = modifier.graphicsLayer { clip = true }) {
        PlayerSurface(
            player = exoPlayer,
            surfaceType = SURFACE_TYPE_SURFACE_VIEW,
            modifier =
                Modifier
                    .fillMaxHeight()
                    .width(with(density) { widthPx.toDp() })
                    .align(Alignment.Center),
        )

        if (presentationState.coverSurface) {
            // Cover the surface that is being prepared with a shutter
            Box(Modifier.background(Color.Black))
        }
    }
}
