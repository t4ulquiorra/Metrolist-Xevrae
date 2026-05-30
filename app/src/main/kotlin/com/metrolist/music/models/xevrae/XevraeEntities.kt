package com.metrolist.music.models.xevrae

import com.metrolist.music.db.entities.SongEntity

/**
 * Xevrae specific entities copied from Xevrae core/domain for compatibility.
 * Room annotations removed as they are currently used as plain data models.
 */

object DownloadState {
    const val STATE_NOT_DOWNLOADED = 0
    const val STATE_PREPARING = 1
    const val STATE_DOWNLOADING = 2
    const val STATE_DOWNLOADED = 3
}

data class GoogleAccountEntity(
    val email: String = "",
    val name: String = "",
    val thumbnailUrl: String = "",
    val pageId: String? = null,
    val cache: String? = null,
    val isUsed: Boolean = false,
    val netscapeCookie: String? = null,
)

data class NotificationEntity(
    val id: Long = 0L,
    val channelId: String,
    val thumbnail: String? = null,
    val name: String,
    val single: List<Map<String, String>> = listOf(),
    val album: List<Map<String, String>> = listOf(),
    val time: String = "", // Simplified from LocalDateTime for now
)

data class PairSongLocalPlaylist(
    val playlist: LocalPlaylistEntity,
    val songs: List<SongEntity>
)
