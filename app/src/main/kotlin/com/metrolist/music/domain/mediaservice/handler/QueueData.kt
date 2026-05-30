package com.metrolist.music.domain.mediaservice.handler

import com.metrolist.innertube.models.SongItem

data class QueueData(
    val queueState: StateSource = StateSource.STATE_CREATED,
    val data: Data = Data(),
) {
    data class Data(
        val listTracks: List<SongItem> = arrayListOf(),
        val firstPlayedTrack: SongItem? = null,
        val playlistId: String? = null,
        val playlistName: String? = null,
        val playlistType: PlaylistType? = null,
        val continuation: String? = null,
    )

    enum class StateSource {
        STATE_CREATED,
        STATE_INITIALIZING,
        STATE_INITIALIZED,
        STATE_ERROR,
    }
}

enum class PlaylistType {
    PLAYLIST,
    LOCAL_PLAYLIST,
    RADIO,
}
