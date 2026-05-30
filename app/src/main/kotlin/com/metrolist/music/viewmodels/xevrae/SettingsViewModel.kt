package com.metrolist.music.viewmodels.xevrae

import android.app.usage.StorageStatsManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.storage.StorageManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewModelScope
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.imageLoader
import com.eygraber.uri.Uri
import com.eygraber.uri.toAndroidUri
import com.metrolist.music.common.Config
import com.metrolist.music.common.DB_NAME
import com.metrolist.music.common.DOWNLOAD_EXOPLAYER_FOLDER
import com.metrolist.music.common.EXOPLAYER_DB_NAME
import com.metrolist.music.common.QUALITY
import com.metrolist.music.common.SELECTED_LANGUAGE
import com.metrolist.music.common.SETTINGS_FILENAME
import com.metrolist.music.common.VIDEO_QUALITY
import com.metrolist.music.models.xevrae.DownloadState
import com.metrolist.music.models.xevrae.GoogleAccountEntity
import com.metrolist.music.extensions.toNetScapeString
import com.metrolist.music.domain.manager.DataStoreManager
import com.metrolist.music.db.MusicDatabase
import com.metrolist.music.utils.LocalResource
import com.metrolist.music.extensions.bytesToMB
import com.metrolist.music.extensions.getSizeOfFile
import com.metrolist.music.extensions.zipInputStream
import com.metrolist.music.extensions.zipOutputStream
import com.metrolist.music.utils.Logger
import com.metrolist.music.utils.LogLevel
import com.metrolist.music.Platform
import com.metrolist.music.getPlatform
import com.metrolist.music.viewmodels.xevrae.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import android.widget.Toast
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

@dagger.hilt.android.lifecycle.HiltViewModel
class SettingsViewModel @javax.inject.Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
    private val dataStoreManager: DataStoreManager,
    private val database: MusicDatabase,
) : BaseViewModel(context) {
    private val commonRepository = CommonRepository(database, context)
    private val songRepository = SongRepository(database)
    private val accountRepository = AccountRepository(database)
    private val cacheRepository = CacheRepository()
    private val downloadUtils = DownloadHandler()

    private val databasePath: String? = commonRepository.getDatabasePath()

    private var _location: MutableStateFlow<String?> = MutableStateFlow(null)
    val location: StateFlow<String?> = _location
    private var _language: MutableStateFlow<String?> = MutableStateFlow(null)
    val language: StateFlow<String?> = _language
    private var _loggedIn: MutableStateFlow<String?> = MutableStateFlow(null)
    val loggedIn: StateFlow<String?> = _loggedIn
    private var _normalizeVolume: MutableStateFlow<String?> = MutableStateFlow(null)
    val normalizeVolume: StateFlow<String?> = _normalizeVolume
    private var _skipSilent: MutableStateFlow<String?> = MutableStateFlow(null)
    val skipSilent: StateFlow<String?> = _skipSilent
    private var _savedPlaybackState: MutableStateFlow<String?> = MutableStateFlow(null)
    val savedPlaybackState: StateFlow<String?> = _savedPlaybackState
    private var _saveRecentSongAndQueue: MutableStateFlow<String?> = MutableStateFlow(null)
    val saveRecentSongAndQueue: StateFlow<String?> = _saveRecentSongAndQueue
    private var _lastCheckForUpdate: MutableStateFlow<String?> = MutableStateFlow(null)
    val lastCheckForUpdate: StateFlow<String?> = _lastCheckForUpdate
    private var _sponsorBlockEnabled: MutableStateFlow<String?> = MutableStateFlow(null)
    val sponsorBlockEnabled: StateFlow<String?> = _sponsorBlockEnabled
    private var _sponsorBlockCategories: MutableStateFlow<ArrayList<String>?> =
        MutableStateFlow(null)
    val sponsorBlockCategories: StateFlow<ArrayList<String>?> = _sponsorBlockCategories
    private var _sendBackToGoogle: MutableStateFlow<String?> = MutableStateFlow(null)
    val sendBackToGoogle: StateFlow<String?> = _sendBackToGoogle
    private var _mainLyricsProvider: MutableStateFlow<String?> = MutableStateFlow(null)
    val mainLyricsProvider: StateFlow<String?> = _mainLyricsProvider

    private var _translationLanguage: MutableStateFlow<String?> = MutableStateFlow(null)
    val translationLanguage: StateFlow<String?> = _translationLanguage
    private var _useTranslation: MutableStateFlow<String?> = MutableStateFlow(null)
    val useTranslation: StateFlow<String?> = _useTranslation
    private var _playerCacheLimit: MutableStateFlow<Int?> = MutableStateFlow(null)
    val playerCacheLimit: StateFlow<Int?> = _playerCacheLimit
    private var _playVideoInsteadOfAudio: MutableStateFlow<String?> = MutableStateFlow(null)
    val playVideoInsteadOfAudio: StateFlow<String?> = _playVideoInsteadOfAudio
    private var _videoQuality: MutableStateFlow<String?> = MutableStateFlow(null)
    val videoQuality: StateFlow<String?> = _videoQuality
    private var _thumbCacheSize = MutableStateFlow<Long?>(null)
    val thumbCacheSize: StateFlow<Long?> = _thumbCacheSize
    private var _canvasCacheSize: MutableStateFlow<Long?> = MutableStateFlow(null)
    val canvasCacheSize: StateFlow<Long?> = _canvasCacheSize
    private var _translucentBottomBar: MutableStateFlow<String?> = MutableStateFlow(null)
    val translucentBottomBar: StateFlow<String?> = _translucentBottomBar
    private var _usingProxy = MutableStateFlow(false)
    val usingProxy: StateFlow<Boolean> = _usingProxy
    private var _proxyType = MutableStateFlow(DataStoreManager.ProxyType.PROXY_TYPE_HTTP)
    val proxyType: StateFlow<DataStoreManager.ProxyType> = _proxyType
    private var _proxyHost = MutableStateFlow("")
    val proxyHost: StateFlow<String> = _proxyHost
    private var _proxyPort = MutableStateFlow(8000)
    val proxyPort: StateFlow<Int> = _proxyPort
    private var _proxyUsername = MutableStateFlow("")
    val proxyUsername: StateFlow<String> = _proxyUsername
    private var _proxyPassword = MutableStateFlow("")
    val proxyPassword: StateFlow<String> = _proxyPassword
    private var _autoCheckUpdate = MutableStateFlow(false)
    val autoCheckUpdate: StateFlow<Boolean> = _autoCheckUpdate
    private var _updateChannel: MutableStateFlow<String> = MutableStateFlow(DataStoreManager.GITHUB)
    val updateChannel: StateFlow<String> = _updateChannel
    private var _blurFullscreenLyrics = MutableStateFlow(false)
    val blurFullscreenLyrics: StateFlow<Boolean> = _blurFullscreenLyrics
    private var _blurPlayerBackground = MutableStateFlow(false)
    val blurPlayerBackground: StateFlow<Boolean> = _blurPlayerBackground
    private val _aiProvider = MutableStateFlow<String>(DataStoreManager.AI_PROVIDER_OPENAI)
    val aiProvider: StateFlow<String> = _aiProvider
    private val _isHasApiKey = MutableStateFlow<Boolean>(false)
    val isHasApiKey: StateFlow<Boolean> = _isHasApiKey
    private val _useAITranslation = MutableStateFlow<Boolean>(false)
    val useAITranslation: StateFlow<Boolean> = _useAITranslation
    private val _customModelId = MutableStateFlow<String>("")
    val customModelId: StateFlow<String> = _customModelId
    private val _customOpenAIBaseUrl = MutableStateFlow<String>("")
    val customOpenAIBaseUrl: StateFlow<String> = _customOpenAIBaseUrl
    private val _customOpenAIHeaders = MutableStateFlow<String>("")
    val customOpenAIHeaders: StateFlow<String> = _customOpenAIHeaders
    private val _crossfadeEnabled = MutableStateFlow<Boolean>(false)
    val crossfadeEnabled: StateFlow<Boolean> = _crossfadeEnabled
    private val _crossfadeDuration = MutableStateFlow<Int>(5000)
    val crossfadeDuration: StateFlow<Int> = _crossfadeDuration
    private val _crossfadeDjMode = MutableStateFlow<Boolean>(true)
    val crossfadeDjMode: StateFlow<Boolean> = _crossfadeDjMode
    private val _prefer320kbpsStream = MutableStateFlow<Boolean>(false)
    val prefer320kbpsStream: StateFlow<Boolean> = _prefer320kbpsStream
    private val _your320kbpsUrl: MutableStateFlow<String> = MutableStateFlow("")
    val your320kbpsUrl: StateFlow<String> = _your320kbpsUrl
    private val _youtubeSubtitleLanguage = MutableStateFlow<String>("")
    val youtubeSubtitleLanguage: StateFlow<String> = _youtubeSubtitleLanguage

    private var _helpBuildLyricsDatabase: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val helpBuildLyricsDatabase: StateFlow<Boolean> = _helpBuildLyricsDatabase
    private var _contributor: MutableStateFlow<Pair<String, String>> = MutableStateFlow(Pair("", ""))
    val contributor: StateFlow<Pair<String, String>> = _contributor

    private var _backupDownloaded: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val backupDownloaded: StateFlow<Boolean> = _backupDownloaded

    private var _enableLiquidGlass: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val enableLiquidGlass: StateFlow<Boolean> = _enableLiquidGlass

    private val _explicitContentEnabled = MutableStateFlow(false)
    val explicitContentEnabled: StateFlow<Boolean> = _explicitContentEnabled

    private val _discordLoggedIn = MutableStateFlow(false)
    val discordLoggedIn: StateFlow<Boolean> = _discordLoggedIn

    private val _richPresenceEnabled = MutableStateFlow(false)
    val richPresenceEnabled: StateFlow<Boolean> = _richPresenceEnabled

    private val _keepServiceAlive = MutableStateFlow<Boolean>(false)
    val keepServiceAlive: StateFlow<Boolean> = _keepServiceAlive

    private val _keepYouTubePlaylistOffline = MutableStateFlow<Boolean>(false)
    val keepYouTubePlaylistOffline: StateFlow<Boolean> = _keepYouTubePlaylistOffline

    private val _combineLocalAndYouTubeLiked = MutableStateFlow<Boolean>(false)
    val combineLocalAndYouTubeLiked: StateFlow<Boolean> = _combineLocalAndYouTubeLiked

    private val _downloadQuality = MutableStateFlow<String?>(null)
    val downloadQuality: StateFlow<String?> = _downloadQuality

    private val _videoDownloadQuality = MutableStateFlow<String?>(null)
    val videoDownloadQuality: StateFlow<String?> = _videoDownloadQuality

    private val _localTrackingEnabled = MutableStateFlow<Boolean>(false)
    val localTrackingEnabled: StateFlow<Boolean> = _localTrackingEnabled

    // Auto Backup
    private val _autoBackupEnabled = MutableStateFlow<Boolean>(false)
    val autoBackupEnabled: StateFlow<Boolean> = _autoBackupEnabled

    private val _autoBackupFrequency = MutableStateFlow<String>(DataStoreManager.AUTO_BACKUP_FREQUENCY_DAILY)
    val autoBackupFrequency: StateFlow<String> = _autoBackupFrequency

    private val _autoBackupMaxFiles = MutableStateFlow<Int>(5)
    val autoBackupMaxFiles: StateFlow<Int> = _autoBackupMaxFiles

    private val _autoBackupLastTime = MutableStateFlow<Long>(0L)
    val autoBackupLastTime: StateFlow<Long> = _autoBackupLastTime

    private var _alertData: MutableStateFlow<SettingAlertState?> = MutableStateFlow(null)
    val alertData: StateFlow<SettingAlertState?> = _alertData

    private var _basicAlertData: MutableStateFlow<SettingBasicAlertState?> = MutableStateFlow(null)
    val basicAlertData: StateFlow<SettingBasicAlertState?> = _basicAlertData

    // Fraction of storage
    private var _fraction: MutableStateFlow<SettingsStorageSectionFraction> =
        MutableStateFlow(
            SettingsStorageSectionFraction(),
        )
    val fraction: StateFlow<SettingsStorageSectionFraction> = _fraction

    // Biến để lưu trữ và hiển thị trạng thái killServiceOnExit
    private var _killServiceOnExit: MutableStateFlow<String?> = MutableStateFlow(null)
    val killServiceOnExit: StateFlow<String?> = _killServiceOnExit

    init {
        getYoutubeSubtitleLanguage()
        getHelpBuildLyricsDatabase()
        viewModelScope.launch {
            enableLiquidGlass.collect {
                if (getPlatform() != Platform.Android && it) {
                    setEnableLiquidGlass(false)
                }
            }
        }
    }

    fun getAudioSessionId() = mediaPlayerHandler.player.audioSessionId

    fun getData() {
        getLocation()
        getLanguage()
        getQuality()
        getPlayerCacheSize()
        getDownloadedCacheSize()
        getPlayerCacheLimit()
        getLoggedIn()
        getNormalizeVolume()
        getSkipSilent()
        getSavedPlaybackState()
        getSendBackToGoogle()
        getSaveRecentSongAndQueue()
        getLastCheckForUpdate()
        getSponsorBlockEnabled()
        getSponsorBlockCategories()
        getTranslationLanguage()
        getYoutubeSubtitleLanguage()
        getLyricsProvider()
        getUseTranslation()
        getPlayVideoInsteadOfAudio()
        getVideoQuality()
        getSpotifyLogIn()
        getSpotifyLyrics()
        getSpotifyCanvas()
        getUsingProxy()
        getCanvasCache()
        getTranslucentBottomBar()
        getAutoCheckUpdate()
        getBlurFullscreenLyrics()
        getBlurPlayerBackground()
        getAIProvider()
        getAIApiKey()
        getAITranslation()
        getCustomModelId()
        getCustomOpenAIBaseUrl()
        getCustomOpenAIHeaders()
        getKillServiceOnExit()
        getCrossfadeEnabled()
        getCrossfadeDuration()
        getCrossfadeDjMode()
        getPrefer320kbpsStream()
        getYour320kbpsUrl()
        getContributorNameAndEmail()
        getBackupDownloaded()
        getUpdateChannel()
        getEnableLiquidGlass()
        getExplicitContentEnabled()
        getDiscordLoggedIn()
        getDiscordRichPresenceEnabled()
        getKeepServiceAlive()
        getKeepYouTubePlaylistOffline()
        getCombineLocalAndYouTubeLiked()
        getDownloadQuality()
        getVideoDownloadQuality()
        getLocalTrackingEnabled()
        getAutoBackupEnabled()
        getAutoBackupFrequency()
        getAutoBackupMaxFiles()
        getAutoBackupLastTime()
        viewModelScope.launch {
            calculateDataFraction(
                cacheRepository,
            )?.let {
                _fraction.value = it
            }
        }
    }

    private fun getLocalTrackingEnabled() {
        viewModelScope.launch {
            dataStoreManager.localTrackingEnabled.collect { enabled ->
                _localTrackingEnabled.value = enabled == true
            }
        }
    }

    fun setLocalTrackingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setLocalTrackingEnabled(enabled)
            getLocalTrackingEnabled()
        }
    }

    private fun getDownloadQuality() {
        viewModelScope.launch {
            dataStoreManager.downloadQuality.collect { quality ->
                when (quality) {
                    QUALITY.items[0].toString() -> _downloadQuality.emit(QUALITY.items[0].toString())
                    QUALITY.items[1].toString() -> _downloadQuality.emit(QUALITY.items[1].toString())
                    else -> _downloadQuality.emit(QUALITY.items[0].toString())
                }
            }
        }
    }

    fun setDownloadQuality(quality: String) {
        viewModelScope.launch {
            dataStoreManager.setDownloadQuality(quality)
            getDownloadQuality()
        }
    }

    private fun getVideoDownloadQuality() {
        viewModelScope.launch {
            dataStoreManager.videoDownloadQuality.collect { videoQuality ->
                when (videoQuality) {
                    VIDEO_QUALITY.items[0].toString() -> _videoDownloadQuality.emit(VIDEO_QUALITY.items[0].toString())
                    VIDEO_QUALITY.items[1].toString() -> _videoDownloadQuality.emit(VIDEO_QUALITY.items[1].toString())
                    VIDEO_QUALITY.items[2].toString() -> _videoDownloadQuality.emit(VIDEO_QUALITY.items[2].toString())
                }
            }
        }
    }

    fun setVideoDownloadQuality(quality: String) {
        viewModelScope.launch {
            if (VIDEO_QUALITY.items.contains(quality)) {
                dataStoreManager.setVideoDownloadQuality(quality)
            }
            getVideoDownloadQuality()
        }
    }

    private fun getKeepYouTubePlaylistOffline() {
        viewModelScope.launch {
            dataStoreManager.keepYouTubePlaylistOffline.collect { keep ->
                _keepYouTubePlaylistOffline.value = keep == true
            }
        }
    }

    fun setKeepYouTubePlaylistOffline(keep: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setKeepYouTubePlaylistOffline(keep)
            getKeepYouTubePlaylistOffline()
        }
    }

    private fun getCombineLocalAndYouTubeLiked() {
        viewModelScope.launch {
            dataStoreManager.combineLocalAndYouTubeLiked.collect { combine ->
                _combineLocalAndYouTubeLiked.value = combine == true
            }
        }
    }

    fun setCombineLocalAndYouTubeLiked(combine: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setCombineLocalAndYouTubeLiked(combine)
            getCombineLocalAndYouTubeLiked()
        }
    }

    private fun getKeepServiceAlive() {
        viewModelScope.launch {
            dataStoreManager.keepServiceAlive.collect { keepServiceAlive ->
                _keepServiceAlive.value = keepServiceAlive == true
            }
        }
    }

    fun setKeepServiceAlive(keepServiceAlive: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setKeepServiceAlive(keepServiceAlive)
            getKeepServiceAlive()
        }
    }

    private fun getCrossfadeEnabled() {
        viewModelScope.launch {
            dataStoreManager.crossfadeEnabled.collect { enabled ->
                _crossfadeEnabled.value = enabled == true
            }
        }
    }

    fun setCrossfadeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setCrossfadeEnabled(enabled)
            getCrossfadeEnabled()
        }
    }

    private fun getCrossfadeDuration() {
        viewModelScope.launch {
            dataStoreManager.crossfadeDuration.collect { duration ->
                _crossfadeDuration.value = duration
            }
        }
    }

    fun setCrossfadeDuration(duration: Int) {
        viewModelScope.launch {
            dataStoreManager.setCrossfadeDuration(duration)
            getCrossfadeDuration()
        }
    }

    private fun getCrossfadeDjMode() {
        viewModelScope.launch {
            dataStoreManager.crossfadeDjMode.collect { enabled ->
                _crossfadeDjMode.value = enabled == true
            }
        }
    }

    fun setCrossfadeDjMode(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setCrossfadeDjMode(enabled)
            getCrossfadeDjMode()
        }
    }

    private fun getPrefer320kbpsStream() {
        viewModelScope.launch {
            dataStoreManager.prefer320kbpsStream.collect { enabled ->
                _prefer320kbpsStream.value = enabled == true
            }
        }
    }

    fun setPrefer320kbpsStream(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setPrefer320kbpsStream(enabled)
            if (!enabled) {
                dataStoreManager.setCrossfadeDjMode(false)
                getCrossfadeDjMode()
            }
            getPrefer320kbpsStream()
        }
    }

    private fun getYour320kbpsUrl() {
        viewModelScope.launch {
            dataStoreManager.your320kbpsUrl.collect { url ->
                _your320kbpsUrl.value = url
            }
        }
    }

    fun setYour320kbpsUrl(url: String) {
        viewModelScope.launch {
            dataStoreManager.setYour320kbpsUrl(url.removeSuffix("/"))
            getYour320kbpsUrl()
        }
    }

    private fun getDiscordLoggedIn() {
        viewModelScope.launch {
            dataStoreManager.discordToken.collect { loggedIn ->
                _discordLoggedIn.value = loggedIn.isNotEmpty()
            }
        }
    }

    fun logOutDiscord() {
        viewModelScope.launch {
            dataStoreManager.setDiscordToken("")
            delay(100)
            getDiscordLoggedIn()
        }
    }

    private fun getDiscordRichPresenceEnabled() {
        viewModelScope.launch {
            dataStoreManager.richPresenceEnabled.collect { enabled ->
                _richPresenceEnabled.value = enabled == true
            }
        }
    }

    fun setDiscordRichPresenceEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setRichPresenceEnabled(enabled)
            delay(100)
            getDiscordRichPresenceEnabled()
        }
    }

    private fun getExplicitContentEnabled() {
        viewModelScope.launch {
            dataStoreManager.explicitContentEnabled.collect { enabled ->
                _explicitContentEnabled.value = enabled == true
            }
        }
    }

    fun setExplicitContentEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setExplicitContentEnabled(enabled)
            getExplicitContentEnabled()
        }
    }

    private fun getEnableLiquidGlass() {
        viewModelScope.launch {
            dataStoreManager.enableLiquidGlass.collect { enableLiquidGlass ->
                _enableLiquidGlass.value = enableLiquidGlass == true
            }
        }
    }

    fun setEnableLiquidGlass(enableLiquidGlass: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setEnableLiquidGlass(enableLiquidGlass)
            getEnableLiquidGlass()
        }
    }

    private fun getUpdateChannel() {
        viewModelScope.launch {
            dataStoreManager.updateChannel.collect { channel ->
                _updateChannel.value = channel
            }
        }
    }

    fun setUpdateChannel(channel: String) {
        viewModelScope.launch {
            dataStoreManager.setUpdateChannel(channel)
            getUpdateChannel()
        }
    }

    private fun getBackupDownloaded() {
        viewModelScope.launch {
            dataStoreManager.backupDownloaded.collect { backupDownloaded ->
                _backupDownloaded.value = backupDownloaded == true
            }
        }
    }

    fun setBackupDownloaded(backupDownloaded: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setBackupDownloaded(backupDownloaded)
            getBackupDownloaded()
        }
    }

    // Auto Backup functions
    private fun getAutoBackupEnabled() {
        viewModelScope.launch {
            dataStoreManager.autoBackupEnabled.collect { enabled ->
                _autoBackupEnabled.value = enabled == true
            }
        }
    }

    fun setAutoBackupEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setAutoBackupEnabled(enabled)
            getAutoBackupEnabled()
        }
    }

    private fun getAutoBackupFrequency() {
        viewModelScope.launch {
            dataStoreManager.autoBackupFrequency.collect { frequency ->
                _autoBackupFrequency.value = frequency
            }
        }
    }

    fun setAutoBackupFrequency(frequency: String) {
        viewModelScope.launch {
            dataStoreManager.setAutoBackupFrequency(frequency)
            getAutoBackupFrequency()
        }
    }

    private fun getAutoBackupMaxFiles() {
        viewModelScope.launch {
            dataStoreManager.autoBackupMaxFiles.collect { max ->
                _autoBackupMaxFiles.value = max
            }
        }
    }

    fun setAutoBackupMaxFiles(max: Int) {
        viewModelScope.launch {
            dataStoreManager.setAutoBackupMaxFiles(max)
            getAutoBackupMaxFiles()
        }
    }

    private fun getAutoBackupLastTime() {
        viewModelScope.launch {
            dataStoreManager.autoBackupLastTime.collect { time ->
                _autoBackupLastTime.value = time
            }
        }
    }

    private fun getContributorNameAndEmail() {
        viewModelScope.launch {
            combine(dataStoreManager.contributorName, dataStoreManager.contributorEmail) { name, email ->
                name to email
            }.collect { contributor ->
                _contributor.value = contributor
            }
        }
    }

    fun setContributorName(name: String) {
        viewModelScope.launch {
            dataStoreManager.setContributorLyricsDatabase(name to contributor.value.second)
            getContributorNameAndEmail()
        }
    }

    fun setContributorEmail(email: String) {
        viewModelScope.launch {
            dataStoreManager.setContributorLyricsDatabase(contributor.value.first to email)
            getContributorNameAndEmail()
        }
    }

    private fun getCustomModelId() {
        viewModelScope.launch {
            dataStoreManager.customModelId.collect { customModelId ->
                _customModelId.value = customModelId
            }
        }
    }

    fun setCustomModelId(modelId: String) {
        viewModelScope.launch {
            dataStoreManager.setCustomModelId(modelId)
            getCustomModelId()
        }
    }

    private fun getCustomOpenAIBaseUrl() {
        viewModelScope.launch {
            dataStoreManager.customOpenAIBaseUrl.collect { baseUrl ->
                _customOpenAIBaseUrl.value = baseUrl
            }
        }
    }

    fun setCustomOpenAIBaseUrl(baseUrl: String) {
        viewModelScope.launch {
            dataStoreManager.setCustomOpenAIBaseUrl(baseUrl)
            getCustomOpenAIBaseUrl()
        }
    }

    private fun getCustomOpenAIHeaders() {
        viewModelScope.launch {
            dataStoreManager.customOpenAIHeaders.collect { headers ->
                _customOpenAIHeaders.value = headers
            }
        }
    }

    fun setCustomOpenAIHeaders(headers: String) {
        viewModelScope.launch {
            dataStoreManager.setCustomOpenAIHeaders(headers)
            getCustomOpenAIHeaders()
        }
    }

    private fun getAIProvider() {
        viewModelScope.launch {
            dataStoreManager.aiProvider.collect { aiProvider ->
                _aiProvider.value = aiProvider
            }
        }
    }

    fun setAIProvider(provider: String) {
        viewModelScope.launch {
            dataStoreManager.setAIProvider(provider)
            getAIProvider()
        }
    }

    private fun getAITranslation() {
        viewModelScope.launch {
            dataStoreManager.useAITranslation.collect { useAITranslation ->
                _useAITranslation.value = useAITranslation == true
            }
        }
    }

    fun setAITranslation(useAITranslation: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setUseAITranslation(useAITranslation)
            getAITranslation()
        }
    }

    private fun getAIApiKey() {
        viewModelScope.launch {
            dataStoreManager.aiApiKey.collect { aiApiKey ->
                if (aiApiKey.isNotEmpty()) {
                    _isHasApiKey.value = true
                    log("getAIApiKey: $aiApiKey")
                } else {
                    _isHasApiKey.value = false
                }
            }
        }
    }

    fun setAIApiKey(apiKey: String) {
        viewModelScope.launch {
            dataStoreManager.setAIApiKey(apiKey)
            getAIApiKey()
        }
    }

    private fun getBlurFullscreenLyrics() {
        viewModelScope.launch {
            dataStoreManager.blurFullscreenLyrics.collect { blurFullscreenLyrics ->
                _blurFullscreenLyrics.value = blurFullscreenLyrics == true
            }
        }
    }

    fun setBlurFullscreenLyrics(blurFullscreenLyrics: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setBlurFullscreenLyrics(blurFullscreenLyrics)
            getBlurFullscreenLyrics()
        }
    }

    private fun getBlurPlayerBackground() {
        viewModelScope.launch {
            dataStoreManager.blurPlayerBackground.collect { blurPlayerBackground ->
                _blurPlayerBackground.value = blurPlayerBackground == true
            }
        }
    }

    fun setBlurPlayerBackground(blurPlayerBackground: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setBlurPlayerBackground(blurPlayerBackground)
            getBlurPlayerBackground()
        }
    }

    private fun getAutoCheckUpdate() {
        viewModelScope.launch {
            dataStoreManager.autoCheckForUpdates.collect { autoCheckUpdate ->
                _autoCheckUpdate.value = autoCheckUpdate == true
            }
        }
    }

    fun setAutoCheckUpdate(autoCheckUpdate: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setAutoCheckForUpdates(autoCheckUpdate)
            getAutoCheckUpdate()
        }
    }

    private fun getCanvasCache() {
        viewModelScope.launch {
            _canvasCacheSize.value = cacheRepository.getCacheSize(Config.CANVAS_CACHE)
        }
    }

    fun setAlertData(alertData: SettingAlertState?) {
        _alertData.value = alertData
    }

    fun setBasicAlertData(alertData: SettingBasicAlertState?) {
        _basicAlertData.value = alertData
    }

    private fun getUsingProxy() {
        viewModelScope.launch {
            dataStoreManager.usingProxy.collectLatest { usingProxy ->
                if (usingProxy == true) {
                    getProxy()
                }
                _usingProxy.value = usingProxy == true
            }
        }
    }

    fun setUsingProxy(usingProxy: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setUsingProxy(usingProxy)
            getUsingProxy()
            getProxy()
        }
    }

    private fun getProxy() {
        viewModelScope.launch {
            val host =
                launch {
                    dataStoreManager.proxyHost.collect {
                        _proxyHost.value = it
                    }
                }
            val port =
                launch {
                    dataStoreManager.proxyPort.collect {
                        _proxyPort.value = it
                    }
                }
            val type =
                launch {
                    dataStoreManager.proxyType.collect {
                        _proxyType.value = it
                        log("getProxy: $it")
                    }
                }
            val username =
                launch {
                    dataStoreManager.proxyUsername.collect {
                        _proxyUsername.value = it
                    }
                }
            val password =
                launch {
                    dataStoreManager.proxyPassword.collect {
                        _proxyPassword.value = it
                    }
                }
            host.join()
            port.join()
            type.join()
            username.join()
            password.join()
        }
    }

    fun setProxy(
        proxyType: DataStoreManager.ProxyType,
        host: String,
        port: Int,
    ) {
        log("setProxy: $proxyType, $host, $port")
        viewModelScope.launch {
            dataStoreManager.setProxyType(proxyType)
            dataStoreManager.setProxyHost(host)
            dataStoreManager.setProxyPort(port)
        }
    }

    fun setProxyCredentials(
        username: String,
        password: String,
    ) {
        log("setProxyCredentials: username=$username")
        viewModelScope.launch {
            dataStoreManager.setProxyUsername(username)
            dataStoreManager.setProxyPassword(password)
        }
    }

    fun getTranslucentBottomBar() {
        viewModelScope.launch {
            dataStoreManager.translucentBottomBar.collect { translucentBottomBar ->
                _translucentBottomBar.emit(translucentBottomBar)
            }
        }
    }

    fun setTranslucentBottomBar(translucentBottomBar: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setTranslucentBottomBar(translucentBottomBar)
            getTranslucentBottomBar()
        }
    }

    fun getThumbCacheSize(context: PlatformContext) {
        viewModelScope.launch {
            val diskCache = SingletonImageLoader.get(context).diskCache
            _thumbCacheSize.emit(diskCache?.size)
        }
    }

    fun getVideoQuality() {
        viewModelScope.launch {
            dataStoreManager.videoQuality.collect { videoQuality ->
                when (videoQuality) {
                    VIDEO_QUALITY.items[0].toString() -> _videoQuality.emit(VIDEO_QUALITY.items[0].toString())
                    VIDEO_QUALITY.items[1].toString() -> _videoQuality.emit(VIDEO_QUALITY.items[1].toString())
                    VIDEO_QUALITY.items[2].toString() -> _videoQuality.emit(VIDEO_QUALITY.items[2].toString())
                }
            }
        }
    }

    fun getTranslationLanguage() {
        viewModelScope.launch {
            dataStoreManager.translationLanguage.collect { translationLanguage ->
                _translationLanguage.emit(translationLanguage)
            }
        }
    }

    fun setTranslationLanguage(language: String) {
        viewModelScope.launch {
            dataStoreManager.setTranslationLanguage(language)
            getTranslationLanguage()
        }
    }

    fun getUseTranslation() {
        viewModelScope.launch {
            dataStoreManager.enableTranslateLyric.collect { useTranslation ->
                _useTranslation.emit(useTranslation)
            }
        }
    }

    fun setUseTranslation(useTranslation: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setEnableTranslateLyric(useTranslation)
            getUseTranslation()
        }
    }

    fun getLyricsProvider() {
        viewModelScope.launch {
            dataStoreManager.lyricsProvider.collect { mainLyricsProvider ->
                _mainLyricsProvider.emit(mainLyricsProvider)
            }
        }
    }

    fun setLyricsProvider(provider: String) {
        viewModelScope.launch {
            dataStoreManager.setLyricsProvider(provider)
            getLyricsProvider()
        }
    }

    fun getLocation() {
        viewModelScope.launch {
            dataStoreManager.location.collect { location ->
                _location.emit(location)
            }
        }
    }

    fun getLoggedIn() {
        viewModelScope.launch {
            dataStoreManager.loggedIn.collect { loggedIn ->
                _loggedIn.emit(loggedIn)
            }
        }
    }

    fun changeLocation(location: String) {
        viewModelScope.launch {
            dataStoreManager.setLocation(location)
            getLocation()
        }
    }

    fun getSaveRecentSongAndQueue() {
        viewModelScope.launch {
            dataStoreManager.saveRecentSongAndQueue.collect { saved ->
                _saveRecentSongAndQueue.emit(saved)
            }
        }
    }

    fun getLastCheckForUpdate() {
        viewModelScope.launch {
            dataStoreManager.getString("CheckForUpdateAt").first().let { lastCheckForUpdate ->
                _lastCheckForUpdate.emit(lastCheckForUpdate)
            }
        }
    }

    fun getSponsorBlockEnabled() {
        viewModelScope.launch {
            dataStoreManager.sponsorBlockEnabled.first().let { enabled ->
                _sponsorBlockEnabled.emit(enabled)
            }
        }
    }

    fun setSponsorBlockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setSponsorBlockEnabled(enabled)
            getSponsorBlockEnabled()
        }
    }

    fun getPlayVideoInsteadOfAudio() {
        viewModelScope.launch {
            dataStoreManager.watchVideoInsteadOfPlayingAudio.collect { playVideoInsteadOfAudio ->
                _playVideoInsteadOfAudio.emit(playVideoInsteadOfAudio)
            }
        }
    }

    fun setPlayVideoInsteadOfAudio(playVideoInsteadOfAudio: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setWatchVideoInsteadOfPlayingAudio(playVideoInsteadOfAudio)
            getPlayVideoInsteadOfAudio()
        }
    }

    fun getSponsorBlockCategories() {
        viewModelScope.launch {
            dataStoreManager.getSponsorBlockCategories().let {
                log("getSponsorBlockCategories: $it", LogLevel.WARN)
                _sponsorBlockCategories.emit(it)
            }
        }
    }

    fun setSponsorBlockCategories(list: ArrayList<String>) {
        log("setSponsorBlockCategories: $list", LogLevel.WARN)
        viewModelScope.launch {
            runBlocking(Dispatchers.IO) {
                dataStoreManager.setSponsorBlockCategories(list)
            }
            getSponsorBlockCategories()
        }
    }

    private var _quality: MutableStateFlow<String?> = MutableStateFlow(null)
    val quality: StateFlow<String?> = _quality

    fun getQuality() {
        viewModelScope.launch {
            dataStoreManager.quality.collect { quality ->
                when (quality) {
                    QUALITY.items[0].toString() -> _quality.emit(QUALITY.items[0].toString())
                    QUALITY.items[1].toString() -> _quality.emit(QUALITY.items[1].toString())
                    else -> _quality.emit(QUALITY.items[0].toString())
                }
            }
        }
    }

    fun changeVideoQuality(item: String) {
        viewModelScope.launch {
            if (VIDEO_QUALITY.items.contains(item)) {
                dataStoreManager.setVideoQuality(item)
            }
            getVideoQuality()
        }
    }

    fun changeQuality(qualityItem: String?) {
        viewModelScope.launch {
            log("changeQuality: $qualityItem")
            dataStoreManager.setQuality(qualityItem ?: QUALITY.items.first().toString())
            getQuality()
        }
    }

    private val _cacheSize: MutableStateFlow<Long?> = MutableStateFlow(null)
    var cacheSize: StateFlow<Long?> = _cacheSize

    fun getPlayerCacheSize() {
        viewModelScope.launch {
            _cacheSize.value = cacheRepository.getCacheSize(Config.PLAYER_CACHE)
        }
    }

    fun clearPlayerCache() {
        viewModelScope.launch {
            cacheRepository.clearCache(Config.PLAYER_CACHE)
            makeToast(getString(com.metrolist.music.R.string.clear_player_cache))
            getPlayerCacheSize()
        }
    }

    private val _downloadedCacheSize: MutableStateFlow<Long?> = MutableStateFlow(null)
    var downloadedCacheSize: StateFlow<Long?> = _downloadedCacheSize

    fun getDownloadedCacheSize() {
        viewModelScope.launch {
            _downloadedCacheSize.value = cacheRepository.getCacheSize(Config.DOWNLOAD_CACHE)
        }
    }

    fun clearDownloadedCache() {
        viewModelScope.launch {
            cacheRepository.clearCache(Config.DOWNLOAD_CACHE)
            songRepository.getDownloadedSongs().singleOrNull()?.let { songs ->
                songs.forEach { song ->
                    songRepository.updateDownloadState(song.videoId, DownloadState.STATE_NOT_DOWNLOADED)
                }
            }
            makeToast(getString(com.metrolist.music.R.string.clear_downloaded_cache))
            getDownloadedCacheSize()
            downloadUtils.removeAllDownloads()
        }
    }

    fun clearCanvasCache() {
        viewModelScope.launch {
            cacheRepository.clearCache(Config.CANVAS_CACHE)
            makeToast(getString(com.metrolist.music.R.string.clear_canvas_cache))
            getCanvasCache()
        }
    }

    fun backup(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                makeToast(getString(com.metrolist.music.R.string.backup_in_progress))
                withContext(Dispatchers.IO) {
                    backupNative(commonRepository, uri, backupDownloaded.value)
                }
            }.onSuccess {
                withContext(Dispatchers.Main) {
                    makeToast(getString(com.metrolist.music.R.string.backup_create_success))
                }
            }.onFailure {
                withContext(Dispatchers.Main) {
                    it.printStackTrace()
                    makeToast(getString(com.metrolist.music.R.string.backup_create_failed))
                }
            }
        }
    }

    fun restore(uri: Uri) {
        viewModelScope.launch {
            makeToast(getString(com.metrolist.music.R.string.restore_in_progress))
            withContext(Dispatchers.IO) {
                runCatching {
                    restoreNative(commonRepository, uri) {
                        getData()
                    }
                }.onFailure {
                    withContext(Dispatchers.Main) {
                        it.printStackTrace()
                        makeToast(getString(com.metrolist.music.R.string.restore_failed))
                    }
                }
            }
        }
    }

    fun getLanguage() {
        viewModelScope.launch {
            dataStoreManager.getString(SELECTED_LANGUAGE).collect { language ->
                _language.emit(language)
            }
        }
    }

    fun changeLanguage(code: String) {
        viewModelScope.launch {
            dataStoreManager.putString(SELECTED_LANGUAGE, code)
            Logger.w("SettingsViewModel", "changeLanguage: $code")
            getLanguage()
            changeLanguageNative(code)
        }
    }

    fun getNormalizeVolume() {
        viewModelScope.launch {
            dataStoreManager.normalizeVolume.collect { normalizeVolume ->
                _normalizeVolume.emit(normalizeVolume)
            }
        }
    }

    fun setNormalizeVolume(normalizeVolume: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setNormalizeVolume(normalizeVolume)
            getNormalizeVolume()
        }
    }

    fun getSendBackToGoogle() {
        viewModelScope.launch {
            dataStoreManager.sendBackToGoogle.collect { sendBackToGoogle ->
                _sendBackToGoogle.emit(sendBackToGoogle)
            }
        }
    }

    fun setSendBackToGoogle(sendBackToGoogle: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setSendBackToGoogle(sendBackToGoogle)
            getSendBackToGoogle()
        }
    }

    fun getSkipSilent() {
        viewModelScope.launch {
            dataStoreManager.skipSilent.collect { skipSilent ->
                _skipSilent.emit(skipSilent)
            }
        }
    }

    fun setSkipSilent(skip: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setSkipSilent(skip)
            getSkipSilent()
        }
    }

    fun getSavedPlaybackState() {
        viewModelScope.launch {
            dataStoreManager.saveStateOfPlayback.collect { savedPlaybackState ->
                _savedPlaybackState.emit(savedPlaybackState)
            }
        }
    }

    fun setSavedPlaybackState(savedPlaybackState: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setSaveStateOfPlayback(savedPlaybackState)
            getSavedPlaybackState()
        }
    }

    fun setSaveLastPlayed(b: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setSaveRecentSongAndQueue(b)
            getSaveRecentSongAndQueue()
        }
    }

    fun getPlayerCacheLimit() {
        viewModelScope.launch {
            dataStoreManager.maxSongCacheSize.collect {
                _playerCacheLimit.emit(it)
            }
        }
    }

    fun setPlayerCacheLimit(size: Int) {
        viewModelScope.launch {
            dataStoreManager.setMaxSongCacheSize(size)
            getPlayerCacheLimit()
        }
    }

    private var _googleAccounts: MutableStateFlow<LocalResource<List<GoogleAccountEntity>>> =
        MutableStateFlow(LocalResource.Loading())
    val googleAccounts: StateFlow<LocalResource<List<GoogleAccountEntity>>> = _googleAccounts

    fun getAllGoogleAccount() {
        Logger.w("getAllGoogleAccount", "getAllGoogleAccount: Go to function")
        viewModelScope.launch {
            _googleAccounts.emit(LocalResource.Loading())
            accountRepository.getGoogleAccounts().collectLatest { accounts ->
                Logger.w("getAllGoogleAccount", "getAllGoogleAccount: $accounts")
                if (!accounts.isNullOrEmpty()) {
                    _googleAccounts.emit(LocalResource.Success(accounts))
                } else {
                    if (loggedIn.value == true) {
                        accountRepository
                            .getAccountInfo(
                                dataStoreManager.cookie.first(),
                            ).collect {
                                Logger.w("getAllGoogleAccount", "getAllGoogleAccount: $it")
                                if (it.isNotEmpty()) {
                                    dataStoreManager.putString("AccountName", it.first().name)
                                    dataStoreManager.putString(
                                        "AccountThumbUrl",
                                        it
                                            .first()
                                            .thumbnails
                                            .lastOrNull()
                                            ?.url ?: "",
                                    )
                                    accountRepository
                                        .insertGoogleAccount(
                                            GoogleAccountEntity(
                                                email = it.first().email,
                                                name = it.first().name,
                                                thumbnailUrl =
                                                    it
                                                        .first()
                                                        .thumbnails
                                                        .lastOrNull()
                                                        ?.url ?: "",
                                                cache = accountRepository.getYouTubeCookie(),
                                                pageId = it.first().pageId,
                                                isUsed = true,
                                            ),
                                        ).singleOrNull()
                                        ?.let { account ->
                                            Logger.w("getAllGoogleAccount", "inserted: $account")
                                        }
                                    getAllGoogleAccount()
                                } else {
                                    _googleAccounts.emit(LocalResource.Success(emptyList()))
                                }
                            }
                    } else {
                        _googleAccounts.emit(LocalResource.Success(emptyList()))
                    }
                }
            }
        }
    }

    suspend fun addAccount(
        cookie: String,
        netscapeCookie: String? = null,
    ): Boolean {
        val currentCookie = dataStoreManager.cookie.first()
        val currentPageId = dataStoreManager.pageId.first()
        val currentLoggedIn = dataStoreManager.loggedIn.first() == true
        try {
            runBlocking {
                dataStoreManager.setCookie(cookie, "")
                dataStoreManager.setLoggedIn(true)
            }
            return accountRepository
                .getAccountInfo(
                    cookie,
                ).lastOrNull()
                ?.takeIf {
                    it.isNotEmpty()
                }?.let { accountInfoList ->
                    Logger.d("getAllGoogleAccount", "addAccount: $accountInfoList")
                    accountRepository.getGoogleAccounts().lastOrNull()?.forEach {
                        Logger.d("getAllGoogleAccount", "set used: $it start")
                        accountRepository
                            .updateGoogleAccountUsed(it.email, false)
                            .singleOrNull()
                            ?.let {
                                Logger.w("getAllGoogleAccount", "set used: $it")
                            }
                    }
                    dataStoreManager.putString("AccountName", accountInfoList.first().name)
                    dataStoreManager.putString(
                        "AccountThumbUrl",
                        accountInfoList
                            .first()
                            .thumbnails
                            .lastOrNull()
                            ?.url ?: "",
                    )
                    val cookieItem =
                        netscapeCookie ?: commonRepository
                            .getCookiesFromInternalDatabase(Config.YOUTUBE_MUSIC_MAIN_URL, getPackageName())
                            .toNetScapeString()
                    commonRepository.writeTextToFile(cookieItem, (getFileDir() + "/ytdlp-cookie.txt")).let {
                        Logger.d("getAllGoogleAccount", "addAccount: write cookie file: $it")
                    }
                    accountInfoList.forEachIndexed { index, account ->
                        accountRepository
                            .insertGoogleAccount(
                                GoogleAccountEntity(
                                    email = account.email,
                                    name = account.name,
                                    thumbnailUrl =
                                        account
                                            .thumbnails
                                            .lastOrNull()
                                            ?.url ?: "",
                                    cache = cookie,
                                    isUsed = index == 0,
                                    netscapeCookie = cookieItem,
                                    pageId = account.pageId,
                                ),
                            ).firstOrNull()
                            ?.let {
                                log("addAccount: $it", LogLevel.WARN)
                            }
                    }
                    dataStoreManager.setLoggedIn(true)
                    dataStoreManager.setCookie(cookie, accountInfoList.first().pageId)
                    getAllGoogleAccount()
                    getLoggedIn()
                    true
                } ?: run {
                Logger.w("getAllGoogleAccount", "addAccount: Account info is null")
                runBlocking {
                    dataStoreManager.setCookie(currentCookie, currentPageId)
                    dataStoreManager.setLoggedIn(currentLoggedIn)
                }
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("getAllGoogleAccount", "addAccount: ${e.message}")
            runBlocking {
                dataStoreManager.setCookie(currentCookie, currentPageId)
                dataStoreManager.setLoggedIn(currentLoggedIn)
            }
            return false
        }
    }

    fun setUsedAccount(acc: GoogleAccountEntity?) {
        viewModelScope.launch {
            if (acc != null) {
                googleAccounts.value.data?.forEach {
                    accountRepository
                        .updateGoogleAccountUsed(it.email, false)
                        .singleOrNull()
                        ?.let {
                            Logger.w("getAllGoogleAccount", "set used: $it")
                        }
                }
                dataStoreManager.putString("AccountName", acc.name)
                dataStoreManager.putString("AccountThumbUrl", acc.thumbnailUrl)
                accountRepository
                    .updateGoogleAccountUsed(acc.email, true)
                    .singleOrNull()
                    ?.let {
                        Logger.w("getAllGoogleAccount", "set used: $it")
                    }
                acc.netscapeCookie?.let { commonRepository.writeTextToFile(it, (getFileDir() + "/ytdlp-cookie.txt")) }.let {
                    Logger.d("getAllGoogleAccount", "addAccount: write cookie file: $it")
                }
                dataStoreManager.setCookie(acc.cache ?: "", acc.pageId)
                dataStoreManager.setLoggedIn(true)
                delay(500)
                getAllGoogleAccount()
                getLoggedIn()
            } else {
                googleAccounts.value.data?.forEach {
                    accountRepository
                        .updateGoogleAccountUsed(it.email, false)
                        .singleOrNull()
                        ?.let {
                            Logger.w("getAllGoogleAccount", "set used: $it")
                        }
                }
                dataStoreManager.putString("AccountName", "")
                dataStoreManager.putString("AccountThumbUrl", "")
                dataStoreManager.setLoggedIn(false)
                dataStoreManager.setCookie("", null)
                delay(500)
                getAllGoogleAccount()
                getLoggedIn()
            }
        }
    }

    fun logOutAllYouTube() {
        viewModelScope.launch {
            googleAccounts.value.data?.forEach { account ->
                accountRepository.deleteGoogleAccount(account.email)
            }
            dataStoreManager.putString("AccountName", "")
            dataStoreManager.putString("AccountThumbUrl", "")
            dataStoreManager.setLoggedIn(false)
            dataStoreManager.setCookie("", null)
            delay(500)
            getAllGoogleAccount()
            getLoggedIn()
        }
    }

    @ExperimentalCoilApi
    fun clearThumbnailCache(platformContext: PlatformContext) {
        viewModelScope.launch {
            SingletonImageLoader.get(platformContext).diskCache?.clear()
            makeToast(getString(com.metrolist.music.R.string.clear_thumbnail_cache))
            getThumbCacheSize(platformContext)
        }
    }

    private var _spotifyLogIn: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val spotifyLogIn: StateFlow<Boolean> = _spotifyLogIn

    fun getSpotifyLogIn() {
        viewModelScope.launch {
            dataStoreManager.spdc.collect { loggedIn ->
                if (loggedIn.isNotEmpty()) {
                    _spotifyLogIn.emit(true)
                } else {
                    _spotifyLogIn.emit(false)
                }
            }
        }
    }

    fun setSpotifyLogIn(loggedIn: Boolean) {
        viewModelScope.launch {
            _spotifyLogIn.emit(loggedIn)
            if (!loggedIn) {
                dataStoreManager.setSpdc("")
                delay(500)
            }
            getSpotifyLogIn()
        }
    }

    private var _spotifyLyrics: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val spotifyLyrics: StateFlow<Boolean> = _spotifyLyrics

    private var _spotifyCanvas: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val spotifyCanvas: StateFlow<Boolean> = _spotifyCanvas

    fun getSpotifyLyrics() {
        viewModelScope.launch {
            dataStoreManager.spotifyLyrics.collect {
                if (it == true) {
                    _spotifyLyrics.emit(true)
                } else {
                    _spotifyLyrics.emit(false)
                }
            }
        }
    }

    fun setSpotifyLyrics(loggedIn: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setSpotifyLyrics(loggedIn)
            getSpotifyLyrics()
        }
    }

    fun getSpotifyCanvas() {
        viewModelScope.launch {
            dataStoreManager.spotifyCanvas.collect {
                if (it == true) {
                    _spotifyCanvas.emit(true)
                } else {
                    _spotifyCanvas.emit(false)
                }
            }
        }
    }

    fun setSpotifyCanvas(loggedIn: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setSpotifyCanvas(loggedIn)
            getSpotifyCanvas()
        }
    }

    // Lấy giá trị của killServiceOnExit từ DataStore
    fun getKillServiceOnExit() {
        viewModelScope.launch {
            dataStoreManager.killServiceOnExit.collect { killServiceOnExit ->
                _killServiceOnExit.emit(killServiceOnExit)
            }
        }
    }

    // Lưu giá trị killServiceOnExit vào DataStore
    fun setKillServiceOnExit(kill: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setKillServiceOnExit(kill)
            getKillServiceOnExit()
        }
    }

    fun getYoutubeSubtitleLanguage() {
        viewModelScope.launch {
            dataStoreManager.youtubeSubtitleLanguage.collect { language ->
                _youtubeSubtitleLanguage.emit(language)
            }
        }
    }

    fun setYoutubeSubtitleLanguage(language: String) {
        viewModelScope.launch {
            dataStoreManager.setYoutubeSubtitleLanguage(language)
            getYoutubeSubtitleLanguage()
        }
    }

    fun getHelpBuildLyricsDatabase() {
        viewModelScope.launch {
            dataStoreManager.helpBuildLyricsDatabase.collect { helpBuildLyricsDatabase ->
                _helpBuildLyricsDatabase.emit(helpBuildLyricsDatabase == true)
            }
        }
    }

    fun setHelpBuildLyricsDatabase(help: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setHelpBuildLyricsDatabase(help)
            getHelpBuildLyricsDatabase()
        }
    }

    fun getPackageName(): String = context.packageName

    fun getFileDir(): String = context.filesDir.absolutePath

    private fun changeLanguageNative(code: String) {
        val localeList =
            LocaleListCompat.forLanguageTags(
                if (code == "id-ID") {
                    if (Build.VERSION.SDK_INT >= 35) {
                        "id-ID"
                    } else {
                        "in-ID"
                    }
                } else {
                    code
                },
            )
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    private suspend fun calculateDataFraction(cacheRepository: CacheRepository): SettingsStorageSectionFraction? {
        return withContext(Dispatchers.Default) {
            val playerCache = cacheRepository.getCacheSize(Config.PLAYER_CACHE)
            val downloadCache = cacheRepository.getCacheSize(Config.DOWNLOAD_CACHE)
            val canvasCache = cacheRepository.getCacheSize(Config.CANVAS_CACHE)
            val mStorageStatsManager =
                context.getSystemService(StorageStatsManager::class.java)
            if (mStorageStatsManager != null) {
                val totalByte =
                    mStorageStatsManager.getTotalBytes(StorageManager.UUID_DEFAULT).bytesToMB()
                val freeSpace =
                    mStorageStatsManager.getFreeBytes(StorageManager.UUID_DEFAULT).bytesToMB()
                val usedSpace = totalByte - freeSpace
                val simpMusicSize = getSizeOfFile(context.filesDir).bytesToMB()
                val thumbSize = (context.imageLoader.diskCache?.size ?: 0L).bytesToMB()
                val otherApp = simpMusicSize.let { usedSpace.minus(it) - thumbSize }
                val databaseSize =
                    simpMusicSize - playerCache.bytesToMB() - downloadCache.bytesToMB() - canvasCache.bytesToMB()

                SettingsStorageSectionFraction(
                    otherApp = otherApp.toFloat().div(totalByte.toFloat()),
                    downloadCache =
                        downloadCache
                            .bytesToMB()
                            .toFloat()
                            .div(totalByte.toFloat()),
                    playerCache =
                        playerCache
                            .bytesToMB()
                            .toFloat()
                            .div(totalByte.toFloat()),
                    canvasCache =
                        canvasCache
                            .bytesToMB()
                            .toFloat()
                            .div(totalByte.toFloat()),
                    thumbCache = thumbSize.toFloat().div(totalByte.toFloat()),
                    freeSpace = freeSpace.toFloat().div(totalByte.toFloat()),
                    appDatabase = databaseSize.toFloat().div(totalByte.toFloat()),
                )
            } else {
                null
            }
        }
    }

    private suspend fun restoreNative(
        commonRepository: CommonRepository,
        uri: Uri,
        getData: () -> Unit,
    ) {
        context.applicationContext.contentResolver.openInputStream(uri.toAndroidUri())?.use {
            it.zipInputStream().use { inputStream ->
                var entry =
                    try {
                        inputStream.nextEntry
                    } catch (e: Exception) {
                        null
                    }

                var downloadFolderCleared = false

                while (entry != null) {
                    Logger.d("BackupRestore", "Processing entry: ${entry.name}")
                    when {
                        entry.name == "$SETTINGS_FILENAME.preferences_pb" -> {
                            val datastoreFile = File(context.filesDir, "datastore/$SETTINGS_FILENAME.preferences_pb")
                            datastoreFile.parentFile?.mkdirs()
                            datastoreFile.outputStream().use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }

                        entry.name == DB_NAME -> {
                            runBlocking(Dispatchers.IO) {
                                commonRepository.databaseDaoCheckpoint()
                                commonRepository.closeDatabase()
                            }
                            val dbPath = commonRepository.getDatabasePath()
                            if (dbPath != null) {
                                FileOutputStream(dbPath).use { outputStream ->
                                    inputStream.copyTo(outputStream)
                                }
                            }
                        }

                        entry.name == EXOPLAYER_DB_NAME -> {
                            FileOutputStream(context.getDatabasePath(EXOPLAYER_DB_NAME)).use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }

                        entry.name.startsWith("$DOWNLOAD_EXOPLAYER_FOLDER/") -> {
                            if (!downloadFolderCleared) {
                                val downloadFolder = File(context.filesDir, DOWNLOAD_EXOPLAYER_FOLDER)
                                clearFolder(downloadFolder)
                                downloadFolderCleared = true
                            }
                            restoreFolder(context, entry.name, inputStream, "download")
                        }
                    }
                    entry = inputStream.nextEntry
                }
            }
        }

        withContext(Dispatchers.Main) {
            Toast.makeText(context, context.getString(com.metrolist.music.R.string.restore_success), Toast.LENGTH_SHORT).show()
            // Stop service and restart logic here if needed
            getData()
            val pm: PackageManager = context.packageManager
            val intent = pm.getLaunchIntentForPackage(context.packageName)
            val mainIntent = Intent.makeRestartActivityTask(intent?.component)
            context.startActivity(mainIntent)
            Runtime.getRuntime().exit(0)
        }
    }

    private fun clearFolder(folder: File) {
        if (folder.exists() && folder.isDirectory) {
            folder.listFiles()?.forEach { file ->
                if (file.isFile) {
                    file.delete()
                } else if (file.isDirectory) {
                    clearFolder(file)
                    file.delete()
                }
            }
        }
    }

    private fun restoreFolder(
        context: android.content.Context,
        entryName: String,
        zipInputStream: ZipInputStream,
        baseFolderName: String,
    ) {
        val relativePath = entryName.removePrefix("$baseFolderName/")
        val targetFile = File(context.filesDir, "$baseFolderName/$relativePath")
        targetFile.parentFile?.mkdirs()

        try {
            targetFile.outputStream().use { outputStream ->
                zipInputStream.copyTo(outputStream)
            }
        } catch (e: Exception) {
            Logger.e("BackupRestore", "Error restoring file: ${targetFile.name}")
        }
    }

    private suspend fun backupNative(
        commonRepository: CommonRepository,
        uri: Uri,
        backupDownloaded: Boolean,
    ) {
        context.applicationContext.contentResolver.openOutputStream(uri.toAndroidUri())?.use {
            it.buffered().zipOutputStream().use { outputStream ->
                val settingsFile = File(context.filesDir, "datastore/$SETTINGS_FILENAME.preferences_pb")
                if (settingsFile.exists()) {
                    settingsFile.inputStream().buffered().use { inputStream ->
                        outputStream.putNextEntry(ZipEntry("$SETTINGS_FILENAME.preferences_pb"))
                        inputStream.copyTo(outputStream)
                    }
                }

                runBlocking(Dispatchers.IO) {
                    commonRepository.databaseDaoCheckpoint()
                }
                val dbPath = commonRepository.getDatabasePath()
                if (dbPath != null) {
                    FileInputStream(dbPath).use { inputStream ->
                        outputStream.putNextEntry(ZipEntry(DB_NAME))
                        inputStream.copyTo(outputStream)
                    }
                }

                if (backupDownloaded) {
                    val exoDb = context.getDatabasePath(EXOPLAYER_DB_NAME)
                    if (exoDb.exists()) {
                        exoDb.inputStream().buffered().use { inputStream ->
                            outputStream.putNextEntry(ZipEntry(EXOPLAYER_DB_NAME))
                            inputStream.copyTo(outputStream)
                        }
                    }
                    val downloadFolder = File(context.filesDir, DOWNLOAD_EXOPLAYER_FOLDER)
                    backupFolder(downloadFolder, DOWNLOAD_EXOPLAYER_FOLDER, outputStream)
                }
            }
        }
    }

    private fun backupFolder(
        folder: File,
        baseName: String,
        zipOutputStream: ZipOutputStream,
    ) {
        if (!folder.exists() || !folder.isDirectory) return

        folder.listFiles()?.forEach { file ->
            if (file.isFile) {
                val entryName = "$baseName/${file.name}"
                zipOutputStream.putNextEntry(ZipEntry(entryName))
                file.inputStream().buffered().use { inputStream ->
                    inputStream.copyTo(zipOutputStream)
                }
                zipOutputStream.closeEntry()
            } else if (file.isDirectory) {
                backupFolder(file, "$baseName/${file.name}", zipOutputStream)
            }
        }
    }
}

data class SettingsStorageSectionFraction(
    val otherApp: Float = 0f,
    val downloadCache: Float = 0f,
    val playerCache: Float = 0f,
    val canvasCache: Float = 0f,
    val thumbCache: Float = 0f,
    val appDatabase: Float = 0f,
    val freeSpace: Float = 0f,
) {
    fun combine(): Float = otherApp + downloadCache + playerCache + canvasCache + thumbCache + appDatabase + freeSpace
}

data class SettingAlertState(
    val title: String,
    val message: String? = null,
    val textField: TextFieldData? = null,
    val selectOne: SelectData? = null,
    val multipleSelect: SelectData? = null,
    val confirm: Pair<String, (SettingAlertState) -> Unit>,
    val dismiss: String,
) {
    data class TextFieldData(
        val label: String,
        val value: String = "",
        // User typing string -> (true or false, If false, show error message)
        val verifyCodeBlock: ((String) -> Pair<Boolean, String?>)? = null,
    )

    data class SelectData(
        // Selected / Data
        val listSelect: List<Pair<Boolean, String>>,
    ) {
        fun getSelected(): String = listSelect.firstOrNull { it.first }?.second ?: ""

        fun getListSelected(): List<String> = listSelect.filter { it.first }.map { it.second }
    }
}

data class SettingBasicAlertState(
    val title: String,
    val message: String? = null,
    val confirm: Pair<String, () -> Unit>,
    val dismiss: String,
)