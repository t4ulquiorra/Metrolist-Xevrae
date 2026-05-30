package com.metrolist.music.viewmodels.xevrae

import com.metrolist.music.db.MusicDatabase
import com.metrolist.music.db.entities.SongEntity
import com.metrolist.music.db.entities.PlaylistEntity
import com.metrolist.music.db.entities.FormatEntity
import com.metrolist.music.db.entities.LyricsEntity
import com.metrolist.music.models.xevrae.*
import com.metrolist.music.utils.Resource
import com.metrolist.innertube.YouTube
import com.metrolist.innertube.models.WatchEndpoint
import com.metrolist.innertube.models.YouTubeClient
import com.metrolist.innertube.models.SongItem
import com.metrolist.music.constants.SongSortType
import com.metrolist.music.constants.PlaylistSortType
import com.metrolist.music.domain.manager.DataStoreManager
import java.time.LocalDateTime
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongRepository @Inject constructor(
    private val database: MusicDatabase
) {
    fun getDownloadedSongs(): Flow<List<SongEntity>?> =
        database.downloadedSongs(SongSortType.CREATE_DATE, true)
            .map { songs -> songs.map { it.song } }

    fun getDownloadingSongs(): Flow<List<SongEntity>?> = flow {
        // Metrolist doesn't track downloading state in DB like Xevrae did.
        // Returning empty list for now as downloading is handled by ExoDownloadService.
        emit(emptyList<SongEntity>())
    }

    fun getPreparingSongs(): Flow<List<SongEntity>> = flow { emit(emptyList()) }

    fun getSongById(id: String): Flow<SongEntity?> =
        database.song(id).map { it?.song }

    fun insertSong(songEntity: SongEntity): Flow<Long> = flow {
        emit(database.insert(songEntity))
    }

    suspend fun updateDownloadState(videoId: String, state: Int) {
        val downloaded = state == DownloadState.STATE_DOWNLOADED
        database.updateDownloadedInfo(
            videoId,
            downloaded,
            if (downloaded) LocalDateTime.now() else null
        )
    }

    suspend fun updateDurationSeconds(duration: Int, videoId: String) {
        database.song(videoId).firstOrNull()?.let { song ->
            database.update(song.song.copy(duration = duration))
        }
    }

    fun getSongInfo(videoId: String): Flow<SongInfoEntity?> =
        database.song(videoId).map { song ->
            song?.let {
                SongInfoEntity(
                    videoId = it.song.id,
                    title = it.song.title,
                    artist = it.artists.joinToString { artist -> artist.name },
                    thumbnail = it.song.thumbnailUrl,
                    duration = it.song.duration.toLong()
                )
            }
        }

    fun addToYouTubeLiked(mediaId: String?): Flow<Int> = flow {
        if (mediaId != null) {
            YouTube.likeVideo(mediaId, true).onSuccess {
                emit(1)
            }.onFailure {
                emit(0)
            }
        } else {
            emit(0)
        }
    }

    fun removeFromYouTubeLiked(mediaId: String?): Flow<Int> = flow {
        if (mediaId != null) {
            YouTube.likeVideo(mediaId, false).onSuccess {
                emit(1)
            }.onFailure {
                emit(0)
            }
        } else {
            emit(0)
        }
    }

    fun getLikeStatus(videoId: String): Flow<Int> =
        database.song(videoId).map { if (it?.song?.liked == true) 1 else 0 }
}

@Singleton
class PlaylistRepository @Inject constructor(
    private val database: MusicDatabase
) {
    fun getAllDownloadedPlaylist(): Flow<List<PlaylistEntity>> =
        database.playlists(PlaylistSortType.CREATE_DATE, true)
            .map { playlists -> playlists.map { it.playlist } }

    fun getAllDownloadingPlaylist(): Flow<List<PlaylistEntity>> = flow {
        emit(emptyList())
    }

    suspend fun updatePlaylistDownloadState(id: String, state: Int) {
        // Not implemented in Metrolist entities
    }
}

@Singleton
class AlbumRepository @Inject constructor(
    private val database: MusicDatabase
) {
    suspend fun updateAlbumDownloadState(id: String, state: Int) {
        // Not implemented in Metrolist entities
    }
}

@Singleton
class LocalPlaylistRepository @Inject constructor(
    private val database: MusicDatabase
) {
    fun getAllDownloadingLocalPlaylists(): Flow<List<LocalPlaylistEntity>> = flow {
        emit(emptyList())
    }

    suspend fun updateDownloadState(id: String, state: Int, successMessage: String): Flow<Int> = flow {
        emit(1)
    }

    suspend fun updateLocalPlaylistDownloadState(state: Int, id: String) {
        // Metrolist uses PlaylistEntity for local playlists too
    }
}

@Singleton
class StreamRepository @Inject constructor(
    private val database: MusicDatabase
) {
    fun getFullMetadata(videoId: String): Flow<Resource<SongItem>> = flow {
        emit(Resource.Loading())
        YouTube.next(WatchEndpoint(videoId = videoId)).onSuccess { result ->
            val songItem = result.items.find { it.id == videoId }
            if (songItem != null) {
                emit(Resource.Success(songItem))
            } else {
                emit(Resource.Error("Song not found"))
            }
        }.onFailure {
            emit(Resource.Error(it.message ?: "Unknown error"))
        }
    }

    fun getFormatFlow(mediaId: String): Flow<NewFormatEntity?> =
        database.format(mediaId).map { format ->
            format?.let {
                NewFormatEntity(
                    videoId = it.id,
                    itag = it.itag,
                    mimeType = it.mimeType,
                    bitrate = it.bitrate,
                    contentLength = it.contentLength,
                    lastModified = it.lastModified
                )
            }
        }
}

@Singleton
class LyricsCanvasRepository @Inject constructor(
    private val database: MusicDatabase
) {
    suspend fun insertLyrics(lyrics: LyricsEntity) {
        database.upsert(lyrics)
    }

    fun getSavedLyrics(videoId: String): Flow<LyricsEntity?> =
        database.lyrics(videoId)

    suspend fun removeTranslatedLyrics(videoId: String, lang: String) {
        // Metrolist lyrics storage is simpler, doesn't support separate translated lyrics yet
    }

    fun voteXevraeTranslatedLyrics(translatedLyricsId: String, vote: Boolean): Flow<Resource<Boolean>> =
        flow { emit(Resource.Success(true)) }

    fun getAITranslationLyrics(videoId: String, lyrics: Lyrics, lang: String, mode: String): Flow<Resource<Lyrics>> =
        flow { emit(Resource.Loading()) }

    fun getXevraeLyrics(videoId: String): Flow<Resource<Lyrics>> =
        flow { emit(Resource.Loading()) }

    fun getSpotifyCanvas(videoId: String, duration: Int): Flow<Resource<CanvasResult>> =
        flow { emit(Resource.Loading()) }

    fun getCanvas(dataStoreManager: DataStoreManager, videoId: String, duration: Int): Flow<Resource<CanvasResult>> =
        flow { emit(Resource.Loading()) }

    suspend fun updateCanvasUrl(videoId: String, canvasUrl: String) {}

    suspend fun updateCanvasThumbUrl(videoId: String, thumbUrl: String) {}

    fun getXevraeTranslatedLyrics(videoId: String, lang: String): Flow<Resource<TranslatedLyricsEntity>> =
        flow { emit(Resource.Loading()) }

    suspend fun insertTranslatedLyrics(translatedLyrics: TranslatedLyricsEntity) {}
}

@Singleton
class UpdateRepository @Inject constructor() {
    fun checkForGithubReleaseUpdate(): Flow<Resource<UpdateData>> = flow { emit(Resource.Loading()) }
    fun checkForFdroidUpdate(): Flow<Resource<UpdateData>> = flow { emit(Resource.Loading()) }
}

@Singleton
class CacheRepository @Inject constructor() {
    suspend fun getAllCacheKeys(cacheName: String): List<String> = emptyList()
}
