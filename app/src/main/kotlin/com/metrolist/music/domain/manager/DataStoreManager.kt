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
    val helpBuildLyricsDatabase: Flow<Boolean> = flowOf(false)
    val lyricsProvider: Flow<String> = flowOf("Unknown")
    val watchVideoInsteadOfPlayingAudio: Flow<Boolean> = flowOf(false)
    val youtubeSubtitleLanguage: Flow<String> = flowOf("en")
    val aiApiKey: Flow<String> = flowOf("")
    val enableTranslateLyric: Flow<Boolean> = flowOf(false)
    val appVersion: Flow<String> = flowOf("1.0.0")
    val spotifyCanvas: Flow<Boolean> = flowOf(false)
    val endlessQueue: Flow<Boolean> = flowOf(false)
    val updateChannel: Flow<String> = flowOf("stable")
    val spotifyLyrics: Flow<Boolean> = flowOf(false)
    val blurPlayerBackground: Flow<Boolean> = flowOf(false)
    val useAITranslation: Flow<Boolean> = flowOf(false)
    val quality: Flow<String> = flowOf("high")
    val location: Flow<String> = flowOf("US")
    val cookie: Flow<String> = flowOf("")
    val autoCheckForUpdates: Flow<Boolean> = flowOf(true)
    val killServiceOnExit: Flow<Boolean> = flowOf(false)
    val combineLocalAndYouTubeLiked: Flow<Boolean> = flowOf(false)
    val openAppTime: Flow<Int> = flowOf(0)
    val blurFullscreenLyrics: Flow<Boolean> = flowOf(false)
    val enableLiquidGlass: Flow<Boolean> = flowOf(false)
    val translucentBottomBar: Flow<Boolean> = flowOf(false)
    val loggedIn: Flow<Boolean> = flowOf(false)
    val pageId: Flow<String?> = flowOf(null)
    val contributorName: Flow<String> = flowOf("")
    val contributorEmail: Flow<String> = flowOf("")
    val discordToken: Flow<String> = flowOf("")
    val richPresenceEnabled: Flow<Boolean> = flowOf(false)
    val explicitContentEnabled: Flow<Boolean> = flowOf(false)
    val localTrackingEnabled: Flow<Boolean> = flowOf(false)
    val downloadQuality: Flow<String> = flowOf("high")
    val videoDownloadQuality: Flow<String> = flowOf("high")
    val autoBackupEnabled: Flow<Boolean> = flowOf(false)
    val autoBackupFrequency: Flow<String> = flowOf("daily")
    val backupDownloaded: Flow<Boolean> = flowOf(false)
    val autoBackupMaxFiles: Flow<Int> = flowOf(5)
    val autoBackupLastTime: Flow<Long> = flowOf(0L)
    val saveStateOfPlayback: Flow<Boolean> = flowOf(false)
    val saveRecentSongAndQueue: Flow<Boolean> = flowOf(false)
    val maxSongCacheSize: Flow<Int> = flowOf(1024)
    val videoQuality: Flow<String> = flowOf("high")
    val sponsorBlockEnabled: Flow<Boolean> = flowOf(false)
    val crossfadeEnabled: Flow<Boolean> = flowOf(false)
    val crossfadeDuration: Flow<Int> = flowOf(5000)
    val crossfadeDjMode: Flow<Boolean> = flowOf(true)
    val prefer320kbpsStream: Flow<Boolean> = flowOf(false)
    val your320kbpsUrl: Flow<String> = flowOf("")
    val usingProxy: Flow<Boolean> = flowOf(false)
    val proxyHost: Flow<String> = flowOf("")
    val proxyPort: Flow<Int> = flowOf(8000)
    val proxyUsername: Flow<String> = flowOf("")
    val proxyPassword: Flow<String> = flowOf("")
    val proxyType: Flow<ProxyType> = flowOf(ProxyType.PROXY_TYPE_HTTP)
    val spdc: Flow<String> = flowOf("")

    fun getString(key: String): Flow<String?> = flowOf(null)
    suspend fun putString(key: String, value: String) {}
    suspend fun setTranslationLanguage(language: String) {}
    suspend fun setHelpBuildLyricsDatabase(help: Boolean) {}
    suspend fun setLyricsProvider(provider: String) {}
    suspend fun setSpotifyLyrics(loggedIn: Boolean) {}
    suspend fun setBlurFullscreenLyrics(blurFullscreenLyrics: Boolean) {}
    suspend fun setBlurPlayerBackground(blurPlayerBackground: Boolean) {}
    suspend fun setPlayerVolume(volume: Float) {}
    fun doneOpenAppTime() {}
    fun openApp() {}
    fun resetOpenAppTime() {}
    suspend fun setAppVersion(version: String) {}
    suspend fun setContributorLyricsDatabase(contributor: Pair<String, String>) {}
    suspend fun setCookie(cookie: String, pageId: String?) {}
    suspend fun setLoggedIn(loggedIn: Boolean) {}
    suspend fun setLocalTrackingEnabled(enabled: Boolean) {}
    suspend fun setDownloadQuality(quality: String) {}
    suspend fun setVideoDownloadQuality(quality: String) {}
    suspend fun setKeepYouTubePlaylistOffline(keep: Boolean) {}
    suspend fun setCombineLocalAndYouTubeLiked(combine: Boolean) {}
    suspend fun setKeepServiceAlive(keep: Boolean) {}
    suspend fun setCrossfadeEnabled(enabled: Boolean) {}
    suspend fun setCrossfadeDuration(duration: Int) {}
    suspend fun setCrossfadeDjMode(enabled: Boolean) {}
    suspend fun setPrefer320kbpsStream(enabled: Boolean) {}
    suspend fun setYour320kbpsUrl(url: String) {}
    suspend fun setDiscordToken(token: String) {}
    suspend fun setRichPresenceEnabled(enabled: Boolean) {}
    suspend fun setExplicitContentEnabled(enabled: Boolean) {}
    suspend fun setUpdateChannel(channel: String) {}
    suspend fun setBackupDownloaded(backup: Boolean) {}
    suspend fun setAutoBackupEnabled(enabled: Boolean) {}
    suspend fun setAutoBackupFrequency(frequency: String) {}
    suspend fun setAutoBackupMaxFiles(max: Int) {}
    suspend fun setAutoBackupLastTime(time: Long) {}
    suspend fun setCustomModelId(modelId: String) {}
    suspend fun setCustomOpenAIBaseUrl(url: String) {}
    suspend fun setCustomOpenAIHeaders(headers: String) {}
    suspend fun setAIProvider(provider: String) {}
    suspend fun setAIApiKey(apiKey: String) {}
    suspend fun setUseAITranslation(use: Boolean) {}
    suspend fun setAutoCheckForUpdates(auto: Boolean) {}
    suspend fun setUsingProxy(using: Boolean) {}
    suspend fun setProxyType(type: ProxyType) {}
    suspend fun setProxyHost(host: String) {}
    suspend fun setProxyPort(port: Int) {}
    suspend fun setProxyUsername(username: String) {}
    suspend fun setProxyPassword(password: String) {}
    suspend fun setTranslucentBottomBar(translucent: Boolean) {}
    suspend fun setVideoQuality(quality: String) {}
    suspend fun setQuality(quality: String) {}
    suspend fun setLocation(location: String) {}
    suspend fun setSponsorBlockEnabled(enabled: Boolean) {}
    suspend fun setSponsorBlockCategories(categories: List<String>) {}
    fun getSponsorBlockCategories(): ArrayList<String> = arrayListOf()
    suspend fun setSaveRecentSongAndQueue(save: Boolean) {}
    suspend fun setNormalizeVolume(normalize: Boolean) {}
    suspend fun setSkipSilent(skip: Boolean) {}
    suspend fun setSaveStateOfPlayback(save: Boolean) {}
    suspend fun setMaxSongCacheSize(size: Int) {}
    suspend fun setKillServiceOnExit(kill: Boolean) {}
    suspend fun setSpdc(spdc: String) {}
    suspend fun setSpotifyCanvas(enabled: Boolean) {}

    enum class ProxyType {
        PROXY_TYPE_HTTP,
        PROXY_TYPE_SOCKS
    }

    companion object {
        private val LOCAL_PLAYLIST_FILTER_KEY = intPreferencesKey("local_playlist_filter")
        
        const val LOCAL_PLAYLIST_FILTER_OLDER_FIRST = 0
        const val LOCAL_PLAYLIST_FILTER_NEWER_FIRST = 1
        const val LOCAL_PLAYLIST_FILTER_TITLE = 2
        const val LOCAL_PLAYLIST_FILTER_CUSTOM_ORDER = 3

        const val XEVRAE = "Xevrae"
        const val LRCLIB = "LrcLib"
        const val YOUTUBE = "YouTube"
        const val BETTER_LYRICS = "BetterLyrics"

        const val AUTO_BACKUP_FREQUENCY_DAILY = "daily"
        const val AUTO_BACKUP_FREQUENCY_WEEKLY = "weekly"
        const val AUTO_BACKUP_FREQUENCY_MONTHLY = "monthly"
    }
}
