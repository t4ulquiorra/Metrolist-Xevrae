package com.metrolist.music.domain.manager

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.metrolist.music.utils.dataStore
import com.metrolist.music.utils.get
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val localPlaylistFilter: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[LOCAL_PLAYLIST_FILTER_KEY] ?: LOCAL_PLAYLIST_FILTER_OLDER_FIRST
    }

    suspend fun setLocalPlaylistFilter(filter: Int) {
        context.dataStore.edit { preferences ->
            preferences[LOCAL_PLAYLIST_FILTER_KEY] = filter
        }
    }

    // Xevrae Compatibility Stubs
    val translationLanguage: Flow<String?> = flowOf(null)
    val helpBuildLyricsDatabase: Flow<String> = flowOf("false")
    val lyricsProvider: Flow<String> = flowOf("Unknown")
    val watchVideoInsteadOfPlayingAudio: Flow<String> = flowOf("false")
    val youtubeSubtitleLanguage: Flow<String> = flowOf("en")
    val aiApiKey: Flow<String> = flowOf("")
    val enableTranslateLyric: Flow<String> = flowOf("false")
    val appVersion: Flow<String> = flowOf("1.0.0")
    val spotifyCanvas: Flow<String> = flowOf("false")
    val endlessQueue: Flow<String> = flowOf("false")
    val updateChannel: Flow<String> = flowOf("stable")
    val spotifyLyrics: Flow<String> = flowOf("false")
    val blurPlayerBackground: Flow<Boolean> = flowOf(false)
    val useAITranslation: Flow<String> = flowOf("false")
    val quality: Flow<String> = flowOf("high")
    val location: Flow<String> = flowOf("US")
    val cookie: Flow<String> = flowOf("")
    val autoCheckForUpdates: Flow<String> = flowOf("true")
    val killServiceOnExit: Flow<String> = flowOf("false")
    val combineLocalAndYouTubeLiked: Flow<String> = flowOf("false")
    val openAppTime: Flow<Int> = flowOf(0)
    val blurFullscreenLyrics: Flow<String> = flowOf("false")
    val enableLiquidGlass: Flow<Boolean> = flowOf(false)
    val translucentBottomBar: Flow<Boolean> = flowOf(false)

    fun getString(key: String): Flow<String?> = flowOf(null)
    suspend fun putString(key: String, value: String) {}
    suspend fun setTranslationLanguage(language: String) {}
    suspend fun setHelpBuildLyricsDatabase(help: Boolean) {}
    suspend fun setLyricsProvider(provider: String) {}
    suspend fun setSpotifyLyrics(loggedIn: Boolean) {}
    suspend fun setBlurFullscreenLyrics(blurFullscreenLyrics: Boolean) {}
    suspend fun setPlayerVolume(volume: Float) {}
    fun doneOpenAppTime() {}
    fun openApp() {}
    fun resetOpenAppTime() {}
    suspend fun setAppVersion(version: String) {}
    suspend fun setContributorLyricsDatabase(contributor: Pair<String, String>) {}

    companion object {
        private val LOCAL_PLAYLIST_FILTER_KEY = intPreferencesKey("local_playlist_filter")
        
        const val LOCAL_PLAYLIST_FILTER_OLDER_FIRST = 0
        const val LOCAL_PLAYLIST_FILTER_NEWER_FIRST = 1
        const val LOCAL_PLAYLIST_FILTER_TITLE = 2
        const val LOCAL_PLAYLIST_FILTER_CUSTOM_ORDER = 3

        const val TRUE = "true"
        const val FALSE = "false"
    }
}
