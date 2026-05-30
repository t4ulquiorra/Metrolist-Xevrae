package com.metrolist.music.models.xevrae

import com.metrolist.music.db.entities.SongEntity
import com.metrolist.innertube.models.SongItem
import androidx.media3.common.MediaItem

/**
 * Xevrae Compatibility Models
 */

data class NowPlayingTrackState(
    val mediaItem: MediaItem,
    val track: SongItem?,
    val songEntity: SongEntity?,
) {
    fun isNotEmpty(): Boolean = this != initial()

    companion object {
        fun initial(): NowPlayingTrackState =
            NowPlayingTrackState(
                mediaItem = MediaItem.EMPTY,
                track = null,
                songEntity = null,
            )
    }
}

data class Lyrics(
    val error: Boolean = false,
    val lines: List<Line>?,
    val syncType: String?,
    val simpMusicLyrics: XevraeLyrics? = null,
)

data class Line(
    val endTimeMs: String,
    val startTimeMs: String,
    val syllables: List<String>? = null,
    val words: String,
)

data class XevraeLyrics(
    val id: String,
    val vote: Int,
)

data class CanvasResult(
    val videoId: String,
    val isVideo: Boolean,
    val canvasUrl: String,
    val canvasThumbUrl: String? = null,
)

data class TimeLine(
    val current: Long,
    val total: Long,
    val bufferedPercent: Int,
    val loading: Boolean,
    val isCrossfading: Boolean = false,
)

data class ControlState(
    val isPlaying: Boolean,
    val isShuffle: Boolean,
    val repeatState: RepeatState,
    val isLiked: Boolean,
    val isNextAvailable: Boolean,
    val isPreviousAvailable: Boolean,
    val isCrossfading: Boolean,
    val volume: Float,
)

enum class RepeatState {
    None,
    One,
    All,
}

sealed class SimpleMediaState {
    data object Initial : SimpleMediaState()
    data object Ended : SimpleMediaState()
    data class Buffering(val progress: Long) : SimpleMediaState()
    data class Ready(val duration: Long) : SimpleMediaState()
    data class Progress(val progress: Long) : SimpleMediaState()
    data class Loading(val duration: Long, val bufferedPercentage: Int) : SimpleMediaState()
}

data class NewFormatEntity(
    val videoId: String,
    val itag: Int,
    val mimeType: String,
    val bitrate: Int,
    val contentLength: Long,
    val lastModified: Long,
)

data class GenericIntent(
    val action: String,
    val data: String?,
)

data class UpdateData(
    val version: String,
    val description: String,
    val url: String,
)

data class SongInfoEntity(
    val videoId: String,
    val title: String,
    val artist: String,
    val thumbnail: String?,
    val duration: Long,
)

object VersionManager {
    fun getVersionName(): String = "1.0.0"
}

data class DownloadProgress(
    val audioDownloadProgress: Float = 0f, // 0.0 - 1.0
    val videoDownloadProgress: Float = 0f, // 0.0 - 1.0
    val downloadSpeed: Int = 0, // kb/s
    val errorMessage: String = "",
    val isMerging: Boolean = false,
    val isError: Boolean = false,
    val isDone: Boolean = false,
) {
    companion object {
        fun failed(message: String) = DownloadProgress(0f, 0f, 0, message, isMerging = false, isError = true, isDone = false)

        val AUDIO_DONE = DownloadProgress(1f, 0f, 0, "", isMerging = false, isError = false, isDone = true)
        val VIDEO_DONE = DownloadProgress(1f, 1f, 0, "", isMerging = false, isError = false, isDone = true)
        val MERGING = DownloadProgress(1f, 1f, 0, "", isMerging = true, isError = false, isDone = false)
        val INIT = DownloadProgress(0f, 0f, 0, "", isMerging = false, isError = false, isDone = false)
    }
}
