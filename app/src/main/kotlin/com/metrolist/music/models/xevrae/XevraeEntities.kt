package com.metrolist.music.models.xevrae

import com.metrolist.music.db.entities.SongEntity

/**
 * Xevrae specific entities copied from Xevrae core/domain for compatibility.
 * Room annotations removed as they are currently used as plain data models.
 */

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

data class LocalPlaylistEntity(
    val id: Long = 0,
    val title: String,
    val thumbnail: String? = null,
    val inLibrary: String? = null, // Simplified for compatibility
    val downloadedAt: String? = null,
    val downloadState: Int = DownloadState.STATE_NOT_DOWNLOADED,
    val youtubePlaylistId: String? = null,
    val syncState: Int = YouTubeSyncState.NotSynced,
    val tracks: List<String>? = null,
) {
    object YouTubeSyncState {
        const val NotSynced = 0
        const val Syncing = 1
        const val Synced = 2
    }
}

data class AlbumEntity(
    val browseId: String = "",
    val artistId: List<String?>? = null,
    val artistName: List<String>? = null,
    val audioPlaylistId: String? = null,
    val description: String? = null,
    val duration: String? = null,
    val durationSeconds: Int = 0,
    val thumbnails: String? = null,
    val title: String,
    val trackCount: Int = 0,
    val tracks: List<String>? = null,
    val type: String? = null,
    val year: String? = null,
    val liked: Boolean = false,
)

data class PlaylistEntity(
    val id: String = "",
    val title: String,
    val author: String? = null,
    val thumbnail: String? = null,
    val trackCount: Int = 0,
    val isEditable: Boolean = false,
    val browseId: String? = null,
)
