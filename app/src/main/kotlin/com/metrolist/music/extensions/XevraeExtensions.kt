package com.metrolist.music.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import com.kmpalette.palette.graphics.Palette
import com.metrolist.music.R
import com.metrolist.music.common.SponsorBlockType
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

@Composable
fun SponsorBlockType.displayString(): String =
    when (this) {
        SponsorBlockType.FILLER -> stringResource(R.string.filler)
        SponsorBlockType.INTERACTION -> stringResource(R.string.interaction)
        SponsorBlockType.INTRO -> stringResource(R.string.intro)
        SponsorBlockType.MUSIC_OFF_TOPIC -> stringResource(R.string.music_off_topic)
        SponsorBlockType.OUTRO -> stringResource(R.string.outro)
        SponsorBlockType.POI_HIGHLIGHT -> stringResource(R.string.poi_highlight)
        SponsorBlockType.PREVIEW -> stringResource(R.string.preview)
        SponsorBlockType.SELF_PROMOTION -> stringResource(R.string.self_promotion)
        SponsorBlockType.SPONSOR -> stringResource(R.string.sponsor)
    }

fun Long?.bytesToMB(): Long {
    val mbInBytes = 1024 * 1024
    return this?.div(mbInBytes) ?: 0L
}

fun getSizeOfFile(dir: File): Long {
    var dirSize: Long = 0
    if (!dir.listFiles().isNullOrEmpty()) {
        for (f in dir.listFiles()!!) {
            dirSize += f.length()
            if (f.isDirectory) {
                dirSize += getSizeOfFile(f)
            }
        }
    }
    return dirSize
}

fun formatDuration(duration: Long): String {
    if (duration < 0L) return "00:00"
    val minutes: Long = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
    val seconds: Long = (
        TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) -
            minutes * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES)
    )
    return String.format(Locale.ENGLISH, "%02d:%02d", minutes, seconds)
}

fun Palette?.getColorFromPalette(): Color {
    val p = this ?: return Color(0xFF121212)
    val defaultColor = 0x000000
    var startColor = p.getDarkVibrantColor(defaultColor)
    if (startColor == defaultColor) {
        startColor = p.getDarkMutedColor(defaultColor)
        if (startColor == defaultColor) {
            startColor = p.getVibrantColor(defaultColor)
            if (startColor == defaultColor) {
                startColor =
                    p.getMutedColor(defaultColor)
                if (startColor == defaultColor) {
                    startColor =
                        p.getLightVibrantColor(
                            defaultColor,
                        )
                    if (startColor == defaultColor) {
                        startColor =
                            p.getLightMutedColor(
                                defaultColor,
                            )
                    }
                }
            }
        }
    }
    return Color(startColor)
}

fun ImageBitmap.toResizedBitmap(
    width: Int,
    height: Int,
): ImageBitmap {
    val resized = ImageBitmap(width, height)
    val canvas = Canvas(resized)
    canvas.drawImageRect(
        image = this,
        dstSize = IntSize(width, height),
        paint = Paint(),
    )
    return resized
}

fun isValidProxyHost(host: String): Boolean {
    val proxyHostRegex =
        Regex(
            pattern = "^(?!-)[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(?<!-)\$",
            options = setOf(RegexOption.IGNORE_CASE),
        )
    return proxyHostRegex.matches(host) || isIPAddress(host)
}

private fun isIPAddress(host: String): Boolean {
    val ipv4Regex =
        Regex(
            pattern = "^([0-9]{1,3}\\.){3}[0-9]{1,3}\$",
        )
    if (ipv4Regex.matches(host)) {
        return host.split('.').all { it.toInt() in 0..255 }
    }
    val ipv6Regex =
        Regex(
            pattern = "^[0-9a-fA-F:]+$",
        )
    return ipv6Regex.matches(host)
}

fun String.isTwoLetterCode(): Boolean {
    val regex = "^[A-Za-z]{2}$".toRegex()
    return regex.matches(this)
}

fun String.toNetScapeString(): String = this
