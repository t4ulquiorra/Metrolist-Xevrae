package com.metrolist.music.viewmodels.xevrae

import androidx.compose.ui.graphics.Color
import com.metrolist.music.db.entities.LocalPlaylistEntity
import com.metrolist.music.models.xevrae.DownloadState
import com.metrolist.music.domain.utils.FilterState
import com.metrolist.music.ui.theme.md_theme_dark_background
import java.time.LocalDateTime
import com.metrolist.innertube.models.SongItem

data class LocalPlaylistState(
    val id: Long,
    val title: String,
    val thumbnail: String? = null,
    val colors: List<Color> =
        listOf(
            Color(0xFF121212),
            md_theme_dark_background,
        ),
    val inLibrary: LocalDateTime? = null,
    val downloadState: Int = 0, // DownloadState.STATE_NOT_DOWNLOADED
    val syncState: Int = 0, // LocalPlaylistEntity.YouTubeSyncState.NotSynced
    val ytPlaylistId: String? = null,
    val trackCount: Int = 0,
    val page: Int = 0,
    val isLoadedFull: Boolean = false,
    val loadState: PlaylistLoadState = PlaylistLoadState.Loading,
    val filterState: FilterState = FilterState.OlderFirst,
    val suggestions: SuggestionSongs? = null,
) {
    sealed class SuggestionState {
        data object Loading : SuggestionState()
        data object Error : SuggestionState()
        data class Success(
            val suggestSongs: SuggestionSongs,
        ) : SuggestionState()
    }

    sealed class PlaylistLoadState {
        data object Loading : PlaylistLoadState()
        data object Error : PlaylistLoadState()
        data object Success : PlaylistLoadState()
    }

    data class SuggestionSongs(
        val reloadParams: String,
        val songs: List<SongItem>,
    )

    companion object {
        fun initial(): LocalPlaylistState =
            LocalPlaylistState(
                id = 0,
                title = "",
                thumbnail = null,
                inLibrary = null,
                downloadState = 0,
                syncState = 0,
                trackCount = 0,
            )
    }
}
