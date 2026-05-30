package com.metrolist.music.common

object Config {
    const val PLAYER_CACHE = "playerCache"
    const val DOWNLOAD_CACHE = "downloadCache"
    const val CANVAS_CACHE = "canvasCache"
    const val YOUTUBE_MUSIC_MAIN_URL = "https://music.youtube.com/"
}

object QUALITY {
    val items: Array<CharSequence> = arrayOf("Low - 66kps", "Medium - 129kps")
}

object VIDEO_QUALITY {
    val items: Array<CharSequence> = arrayOf("1080p", "720p", "360p")
}

const val SETTINGS_FILENAME = "settings"
const val DOWNLOAD_EXOPLAYER_FOLDER = "download"
const val DB_NAME = "Music Database"
const val EXOPLAYER_DB_NAME = "exoplayer_internal.db"
const val SELECTED_LANGUAGE = "selected_language"
const val QUALITY_KEY = "quality"
const val VIDEO_QUALITY_KEY = "video_quality"
