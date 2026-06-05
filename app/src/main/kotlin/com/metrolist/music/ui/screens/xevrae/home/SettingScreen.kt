package com.metrolist.music.ui.screens.xevrae.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.ComponentActivity
import com.metrolist.music.LocalActivity
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.eygraber.uri.toKmpUri
import com.metrolist.music.common.LIMIT_CACHE_SIZE
import com.metrolist.music.common.QUALITY
import com.metrolist.music.common.SUPPORTED_LANGUAGE
import com.metrolist.music.common.SUPPORTED_LOCATION
import com.metrolist.music.common.SponsorBlockType
import com.metrolist.music.common.VIDEO_QUALITY
import com.metrolist.music.extensions.now
import com.metrolist.music.domain.manager.DataStoreManager
import com.metrolist.music.utils.LocalResource
import com.metrolist.music.utils.Logger
import com.metrolist.music.ui.utils.fileSaverResult
import com.metrolist.music.ui.utils.openEqResult
import com.metrolist.music.extensions.bytesToMB
import com.metrolist.music.extensions.displayString
import com.metrolist.music.extensions.isTwoLetterCode
import com.metrolist.music.extensions.isValidProxyHost
import com.metrolist.music.ui.component.ActionButton
import com.metrolist.music.ui.component.CenterLoadingBox
import com.metrolist.music.ui.component.EndOfPage
import com.metrolist.music.ui.component.RippleIconButton
import com.metrolist.music.ui.component.SettingItem
import com.metrolist.music.ui.navigation.xevrae.destination.home.CreditDestination
import com.metrolist.music.ui.navigation.xevrae.destination.login.DiscordLoginDestination
import com.metrolist.music.ui.navigation.xevrae.destination.login.LoginDestination
import com.metrolist.music.ui.navigation.xevrae.destination.login.SpotifyLoginDestination
import com.metrolist.music.ui.theme.xevrae.DarkColors
import com.metrolist.music.ui.theme.xevrae.md_theme_dark_primary
import com.metrolist.music.ui.theme.xevrae.typo
import com.metrolist.music.ui.theme.xevrae.white
import com.metrolist.music.models.xevrae.VersionManager
import com.metrolist.music.viewmodels.xevrae.SettingAlertState
import com.metrolist.music.viewmodels.xevrae.SettingBasicAlertState
import com.metrolist.music.viewmodels.xevrae.SettingsViewModel
import com.metrolist.music.viewmodels.xevrae.SharedViewModel
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.ui.compose.ChipColors
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.m3.libraryColors
import com.mikepenz.aboutlibraries.ui.compose.produceLibraries
import com.mohamedrejeb.calf.core.ExperimentalCalfApi
import com.mohamedrejeb.calf.io.getPath
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalCoilApi::class,
    ExperimentalHazeMaterialsApi::class,
    FormatStringsInDatetimeFormats::class,
    ExperimentalCalfApi::class,
)
@Composable
fun SettingScreen(
    innerPadding: PaddingValues,
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel(),
    sharedViewModel: SharedViewModel = hiltViewModel(
        viewModelStoreOwner = LocalActivity.current as ComponentActivity
    ),
) {
    val cancelStr = stringResource(com.metrolist.music.R.string.cancel)
    val changeStr = stringResource(com.metrolist.music.R.string.change)
    val setStr = stringResource(com.metrolist.music.R.string.set)
    val invalidStr = stringResource(com.metrolist.music.R.string.error)
    val socksStr = stringResource(com.metrolist.music.R.string.socks)
    val httpStr = stringResource(com.metrolist.music.R.string.http)
    val saveStr = stringResource(com.metrolist.music.R.string.save)
    val noneStr = stringResource(com.metrolist.music.R.string.none)
    val dailyStr = stringResource(com.metrolist.music.R.string.daily)
    val weeklyStr = stringResource(com.metrolist.music.R.string.weekly)
    val monthlyStr = stringResource(com.metrolist.music.R.string.monthly)
    val categoriesSponsorBlockStr = stringResource(com.metrolist.music.R.string.categories_sponsor_block)
    val backupFrequencyStr = stringResource(com.metrolist.music.R.string.backup_frequency)
    val keepBackupsStr = stringResource(com.metrolist.music.R.string.keep_backups)

    val proxyTypeTitle = stringResource(com.metrolist.music.R.string.proxy_type)
    val platformContext = LocalPlatformContext.current
    val localDensity = LocalDensity.current
    val uriHandler = LocalUriHandler.current
    val coroutineScope = rememberCoroutineScope()

    var width by rememberSaveable { mutableIntStateOf(0) }

    // Backup and restore
    val formatter =
        LocalDateTime.Format {
            byUnicodePattern("yyyyMMddHHmmss")
        }
    val appName = stringResource(com.metrolist.music.R.string.app_name)

    val backupLauncher =
        fileSaverResult(
            "${appName}_${
                now().format(
                    formatter,
                )
            }.backup",
            "application/octet-stream",
        ) { uri ->
            uri?.let {
                viewModel.backup(it.toKmpUri())
            }
        }

    val restoreLauncher =
        rememberFilePickerLauncher(
            type =
                FilePickerFileType.All,
            selectionMode = FilePickerSelectionMode.Single,
        ) { file ->
            file.firstOrNull()?.getPath(platformContext)?.toKmpUri()?.let {
                viewModel.restore(it)
            }
        }

    // Open equalizer
    val resultLauncher = openEqResult(0)

    val enableTranslucentNavBar by viewModel.translucentBottomBar.collectAsStateWithLifecycle(initialValue = false)
    val language by viewModel.language.collectAsStateWithLifecycle()
    val location by viewModel.location.collectAsStateWithLifecycle()
    val quality by viewModel.quality.collectAsStateWithLifecycle()
    val prefer320kbpsStream by viewModel.prefer320kbpsStream.collectAsStateWithLifecycle()
    val your320kbpsUrl by viewModel.your320kbpsUrl.collectAsStateWithLifecycle()
    val downloadQuality by viewModel.downloadQuality.collectAsStateWithLifecycle()
    val videoDownloadQuality by viewModel.videoDownloadQuality.collectAsStateWithLifecycle()
    val keepYoutubePlaylistOffline by viewModel.keepYouTubePlaylistOffline.collectAsStateWithLifecycle()
    val localTrackingEnabled by viewModel.localTrackingEnabled.collectAsStateWithLifecycle(initialValue = false)
    val combineLocalAndYouTubeLiked by viewModel.combineLocalAndYouTubeLiked.collectAsStateWithLifecycle()
    val playVideo by viewModel.playVideoInsteadOfAudio.collectAsStateWithLifecycle(initialValue = false)
    val videoQuality by viewModel.videoQuality.collectAsStateWithLifecycle()
    val sendData by viewModel.sendBackToGoogle.collectAsStateWithLifecycle(initialValue = false)
    val normalizeVolume by viewModel.normalizeVolume.collectAsStateWithLifecycle(initialValue = false)
    val skipSilent by viewModel.skipSilent.collectAsStateWithLifecycle(initialValue = false)
    val savePlaybackState by viewModel.savedPlaybackState.collectAsStateWithLifecycle(initialValue = false)
    val saveLastPlayed by viewModel.saveRecentSongAndQueue.collectAsStateWithLifecycle(initialValue = false)
    val killServiceOnExit by viewModel.killServiceOnExit.collectAsStateWithLifecycle(initialValue = true)
    val mainLyricsProvider by viewModel.mainLyricsProvider.collectAsStateWithLifecycle()
    val youtubeSubtitleLanguage by viewModel.youtubeSubtitleLanguage.collectAsStateWithLifecycle()
    val spotifyLoggedIn by viewModel.spotifyLogIn.collectAsStateWithLifecycle()
    val spotifyLyrics by viewModel.spotifyLyrics.collectAsStateWithLifecycle()
    val spotifyCanvas by viewModel.spotifyCanvas.collectAsStateWithLifecycle()
    val blurFullscreenLyrics by viewModel.blurFullscreenLyrics.collectAsStateWithLifecycle(initialValue = false)
    val blurPlayerBackground by viewModel.blurPlayerBackground.collectAsStateWithLifecycle(initialValue = false)
    val enableLiquidGlass by viewModel.enableLiquidGlass.collectAsStateWithLifecycle(initialValue = false)
    val enableSponsorBlock by viewModel.sponsorBlockEnabled.collectAsStateWithLifecycle(initialValue = false)
    val skipSegments by viewModel.sponsorBlockCategories.collectAsStateWithLifecycle()
    val playerCache by viewModel.cacheSize.collectAsStateWithLifecycle()
    val downloadedCache by viewModel.downloadedCacheSize.collectAsStateWithLifecycle()
    val thumbnailCache by viewModel.thumbCacheSize.collectAsStateWithLifecycle()
    val canvasCache by viewModel.canvasCacheSize.collectAsStateWithLifecycle()
    val limitPlayerCache by viewModel.playerCacheLimit.collectAsStateWithLifecycle()
    val fraction by viewModel.fraction.collectAsStateWithLifecycle()
    val lastCheckUpdate by viewModel.lastCheckForUpdate.collectAsStateWithLifecycle()
    val explicitContentEnabled by viewModel.explicitContentEnabled.collectAsStateWithLifecycle()
    val usingProxy by viewModel.usingProxy.collectAsStateWithLifecycle()
    val proxyType by viewModel.proxyType.collectAsStateWithLifecycle()
    val proxyHost by viewModel.proxyHost.collectAsStateWithLifecycle()
    val proxyPort by viewModel.proxyPort.collectAsStateWithLifecycle()
    val proxyUsername by viewModel.proxyUsername.collectAsStateWithLifecycle()
    val proxyPassword by viewModel.proxyPassword.collectAsStateWithLifecycle()
    val autoCheckUpdate by viewModel.autoCheckUpdate.collectAsStateWithLifecycle()
    val blurFullscreenLyrics by viewModel.blurFullscreenLyrics.collectAsStateWithLifecycle()
    val blurPlayerBackground by viewModel.blurPlayerBackground.collectAsStateWithLifecycle()
    val aiProvider by viewModel.aiProvider.collectAsStateWithLifecycle()
    val isHasApiKey by viewModel.isHasApiKey.collectAsStateWithLifecycle()
    val useAITranslation by viewModel.useAITranslation.collectAsStateWithLifecycle()
    val translationLanguage by viewModel.translationLanguage.collectAsStateWithLifecycle()
    val customModelId by viewModel.customModelId.collectAsStateWithLifecycle()
    val customOpenAIBaseUrl by viewModel.customOpenAIBaseUrl.collectAsStateWithLifecycle()
    val customOpenAIHeaders by viewModel.customOpenAIHeaders.collectAsStateWithLifecycle()
    val helpBuildLyricsDatabase by viewModel.helpBuildLyricsDatabase.collectAsStateWithLifecycle()
    val contributor by viewModel.contributor.collectAsStateWithLifecycle()
    val backupDownloaded by viewModel.backupDownloaded.collectAsStateWithLifecycle()
    val autoBackupEnabled by viewModel.autoBackupEnabled.collectAsStateWithLifecycle()
    val autoBackupFrequency by viewModel.autoBackupFrequency.collectAsStateWithLifecycle()
    val autoBackupMaxFiles by viewModel.autoBackupMaxFiles.collectAsStateWithLifecycle()
    val autoBackupLastTime by viewModel.autoBackupLastTime.collectAsStateWithLifecycle()
    val updateChannel by viewModel.updateChannel.collectAsStateWithLifecycle()
    val enableLiquidGlass by viewModel.enableLiquidGlass.collectAsStateWithLifecycle()
    val discordLoggedIn by viewModel.discordLoggedIn.collectAsStateWithLifecycle()
    val richPresenceEnabled by viewModel.richPresenceEnabled.collectAsStateWithLifecycle()
    val keepServiceAlive by viewModel.keepServiceAlive.collectAsStateWithLifecycle()

    val crossfadeEnabled by viewModel.crossfadeEnabled.collectAsStateWithLifecycle()
    val crossfadeDuration by viewModel.crossfadeDuration.collectAsStateWithLifecycle()
    val crossfadeDjMode by viewModel.crossfadeDjMode.collectAsStateWithLifecycle()

    val isCheckingUpdate by sharedViewModel.isCheckingUpdate.collectAsStateWithLifecycle()

    val hazeState =
        remember { HazeState() }

    val checkingStr = stringResource(com.metrolist.music.R.string.checking)
    val lastCheckedStr = stringResource(com.metrolist.music.R.string.last_checked_at)

    val checkForUpdateSubtitle by remember(isCheckingUpdate, lastCheckUpdate) {
        derivedStateOf {
            if (isCheckingUpdate) {
                return@derivedStateOf checkingStr
            } else {
                val lastCheckLong = lastCheckUpdate?.toLong() ?: 0L
                return@derivedStateOf String.format(
                    lastCheckedStr,
                    DateTimeFormatter
                        .ofPattern("yyyy-MM-dd HH:mm:ss")
                        .withZone(ZoneId.systemDefault())
                        .format(Instant.ofEpochMilli(lastCheckLong)),
                )
            }
        }
    }
    var showYouTubeAccountDialog by rememberSaveable {
        mutableStateOf(false)
    }
    var showThirdPartyLibraries by rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(true) {
        viewModel.getAllGoogleAccount()
    }

    LaunchedEffect(true) {
        viewModel.getData()
        viewModel.getThumbCacheSize(platformContext)
    }

    LazyColumn(
        contentPadding = innerPadding,
        modifier =
            Modifier
                .padding(horizontal = 16.dp)
                .haze(hazeState),
    ) {
        item {
            Spacer(Modifier.height(64.dp))
        }
        item(key = "user_interface") {
            Column {
                Spacer(Modifier.height(16.dp))
                Text(text = stringResource(com.metrolist.music.R.string.user_interface), style = typo().labelMedium, color = white)
                SettingItem(
                    title = stringResource(com.metrolist.music.R.string.translucent_bottom_navigation_bar),
                    subtitle = stringResource(com.metrolist.music.R.string.you_can_see_the_content_below_the_bottom_bar),
                    smallSubtitle = true,
                    switch = (enableTranslucentNavBar to { viewModel.setTranslucentBottomBar(it) }),
                )
                SettingItem(
                    title = stringResource(com.metrolist.music.R.string.blur_fullscreen_lyrics),
                    subtitle = stringResource(com.metrolist.music.R.string.blur_fullscreen_lyrics_description),
                    smallSubtitle = true,
                    switch = (blurFullscreenLyrics to { viewModel.setBlurFullscreenLyrics(it) }),
                )
                SettingItem(
                    title = stringResource(com.metrolist.music.R.string.blur_player_background),
                    subtitle = stringResource(com.metrolist.music.R.string.blur_player_background_description),
                    smallSubtitle = true,
                    switch = (blurPlayerBackground to { viewModel.setBlurPlayerBackground(it) }),
                )
                if (true) {
                    SettingItem(
                        title = stringResource(com.metrolist.music.R.string.enable_liquid_glass_effect),
                        subtitle = stringResource(com.metrolist.music.R.string.enable_liquid_glass_effect_description),
                        smallSubtitle = true,
                        switch = (enableLiquidGlass to { viewModel.setEnableLiquidGlass(it) }),
                        isEnable = true,
                    )
                }
            }
        }
        item(key = "content") {
            Column {
                Text(
                    text = stringResource(com.metrolist.music.R.string.content),
                    style = typo().labelMedium,
                    color = white,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                SettingItem(
                    title = stringResource(com.metrolist.music.R.string.youtube_account),
                    subtitle = stringResource(com.metrolist.music.R.string.manage_your_youtube_accounts),
                    onClick = {
                        viewModel.getAllGoogleAccount()
                        showYouTubeAccountDialog = true
                    },
                )
                val languageTitle = stringResource(com.metrolist.music.R.string.language)
                val changeLanguageWarning = stringResource(com.metrolist.music.R.string.change_language_warning)

                SettingItem(
                    title = languageTitle,
                    subtitle = SUPPORTED_LANGUAGE.getLanguageFromCode(language ?: "en-US"),
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = languageTitle,
                                selectOne =
                                    SettingAlertState.SelectData(
                                        listSelect =
                                            SUPPORTED_LANGUAGE.items.map {
                                                (it.toString() == SUPPORTED_LANGUAGE.getLanguageFromCode(language ?: "en-US")) to it.toString()
                                            },
                                    ),
                                confirm =
                                    changeStr to { state ->
                                        val code = SUPPORTED_LANGUAGE.getCodeFromLanguage(state.selectOne?.getSelected() ?: "English")
                                        viewModel.setBasicAlertData(
                                            SettingBasicAlertState(
                                                title = "Warning",
                                                message = changeLanguageWarning,
                                                confirm =
                                                    changeStr to {
                                                        sharedViewModel.activityRecreate()
                                                        viewModel.setBasicAlertData(null)
                                                        viewModel.changeLanguage(code)
                                                    },
                                                dismiss = cancelStr,
                                            ),
                                        )
                                    },
                                dismiss = cancelStr,
                            ),
                        )
                    },
                )
                val contentCountryTitle = stringResource(com.metrolist.music.R.string.content_country)
                SettingItem(
                    title = contentCountryTitle,
                    subtitle = location ?: "",
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = contentCountryTitle,
                                selectOne =
                                    SettingAlertState.SelectData(
                                        listSelect =
                                            SUPPORTED_LOCATION.items.map { item ->
                                                (item.toString() == location) to item.toString()
                                            },
                                    ),
                                confirm =
                                    changeStr to { state ->
                                        viewModel.changeLocation(
                                            state.selectOne?.getSelected() ?: "US",
                                        )
                                    },
                                dismiss = cancelStr,
                            ),
                        )
                    },
                )
                val qualityTitle = stringResource(com.metrolist.music.R.string.quality)
                SettingItem(
                    title = qualityTitle,
                    subtitle = quality ?: "",
                    smallSubtitle = true,
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = qualityTitle,
                                selectOne =
                                    SettingAlertState.SelectData(
                                        listSelect =
                                            QUALITY.items.map { item ->
                                                (item.toString() == quality) to item.toString()
                                            },
                                    ),
                                confirm =
                                    changeStr to { state ->
                                        viewModel.changeQuality(state.selectOne?.getSelected())
                                    },
                                dismiss = cancelStr,
                            ),
                        )
                    },
                )
                SettingItem(
                    title = stringResource(com.metrolist.music.R.string.prefer_320kbps_stream),
                    subtitle = stringResource(com.metrolist.music.R.string.prefer_320kbps_stream_description),
                    smallSubtitle = true,
                    switch = (prefer320kbpsStream to { viewModel.setPrefer320kbpsStream(it) }),
                )
                AnimatedVisibility(visible = prefer320kbpsStream, enter = slideInVertically() + fadeIn(), exit = slideOutVertically() + fadeOut()) {
                    val your320kbpsUrlTitle = stringResource(com.metrolist.music.R.string.your_320kbps_url)
                    SettingItem(
                        title = your320kbpsUrlTitle,
                        subtitle = your320kbpsUrl,
                        isEnable = prefer320kbpsStream,
                        onClick = {
                            viewModel.setAlertData(
                                SettingAlertState(
                                    title = your320kbpsUrlTitle,
                                    textField =
                                        SettingAlertState.TextFieldData(
                                            label = your320kbpsUrlTitle,
                                            value = "",
                                            verifyCodeBlock = {
                                                (it.isNotEmpty()) to invalidStr
                                            },
                                        ),
                                    message = "",
                                    confirm =
                                        setStr to { state ->
                                            viewModel.setYour320kbpsUrl(state.textField?.value ?: "")
                                        },
                                    dismiss = cancelStr,
                                ),
                            )
                        },
                    )
                }
                val downloadQualityTitle = stringResource(com.metrolist.music.R.string.download_quality)
                SettingItem(
                    title = downloadQualityTitle,
                    subtitle = downloadQuality ?: "",
                    smallSubtitle = true,
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = downloadQualityTitle,
                                selectOne =
                                    SettingAlertState.SelectData(
                                        listSelect =
                                            QUALITY.items.map { item ->
                                                (item.toString() == downloadQuality) to item.toString()
                                            },
                                    ),
                                confirm =
                                    changeStr to { state ->
                                        state.selectOne?.getSelected()?.let { viewModel.setDownloadQuality(it) }
                                    },
                                dismiss = cancelStr,
                            ),
                        )
                    },
                )
                SettingItem(
                    title = stringResource(com.metrolist.music.R.string.play_video_for_video_track_instead_of_audio_only),
                    subtitle = stringResource(com.metrolist.music.R.string.such_as_music_video_lyrics_video_podcasts_and_more),
                    smallSubtitle = true,
                    switch = (playVideo to { viewModel.setPlayVideoInsteadOfAudio(it) }),
                )
                val videoQualityTitle = stringResource(com.metrolist.music.R.string.video_quality)
                SettingItem(
                    title = videoQualityTitle,
                    subtitle = videoQuality ?: "",
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = videoQualityTitle,
                                selectOne =
                                    SettingAlertState.SelectData(
                                        listSelect =
                                            VIDEO_QUALITY.items.map { item ->
                                                (item.toString() == videoQuality) to item.toString()
                                            },
                                    ),
                                confirm =
                                    changeStr to { state ->
                                        viewModel.changeVideoQuality(state.selectOne?.getSelected() ?: "")
                                    },
                                dismiss = cancelStr,
                            ),
                        )
                    },
                )
                val videoDownloadQualityTitle = stringResource(com.metrolist.music.R.string.video_download_quality)
                SettingItem(
                    title = videoDownloadQualityTitle,
                    subtitle = videoDownloadQuality ?: "",
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = videoDownloadQualityTitle,
                                selectOne =
                                    SettingAlertState.SelectData(
                                        listSelect =
                                            VIDEO_QUALITY.items.map { item ->
                                                (item.toString() == videoDownloadQuality) to item.toString()
                                            },
                                    ),
                                confirm =
                                    changeStr to { state ->
                                        viewModel.setVideoDownloadQuality(state.selectOne?.getSelected() ?: "")
                                    },
                                dismiss = cancelStr,
                            ),
                        )
                    },
                )
                SettingItem(
                    title = stringResource(com.metrolist.music.R.string.send_back_listening_data_to_google),
                    subtitle =
                        stringResource(
                            com.metrolist.music.R.string
                                .upload_your_listening_history_to_youtube_music_server_it_will_make_yt_music_recommendation_system_better_working_only_if_logged_in,
                        ),
                    smallSubtitle = true,
                    switch = (sendData to { viewModel.setSendBackToGoogle(it) }),
                )
                SettingItem(
                    title = stringResource(com.metrolist.music.R.string.play_explicit_content),
                    subtitle = stringResource(com.metrolist.music.R.string.play_explicit_content_description),
                    switch = (explicitContentEnabled to { viewModel.setExplicitContentEnabled(it) }),
                )
                SettingItem(
                    title = stringResource(com.metrolist.music.R.string.keep_your_youtube_playlist_offline),
                    subtitle = stringResource(com.metrolist.music.R.string.keep_your_youtube_playlist_offline_description),
                    switch = (keepYoutubePlaylistOffline to { viewModel.setKeepYouTubePlaylistOffline(it) }),
                )
                SettingItem(
                    title = stringResource(com.metrolist.music.R.string.local_tracking_title),
                    subtitle = stringResource(com.metrolist.music.R.string.local_tracking_description),
                    switch = (localTrackingEnabled to { viewModel.setLocalTrackingEnabled(it) }),
                )
                /*
                SettingItem(
                    title = stringResource(com.metrolist.music.R.string.combine_local_and_youtube_liked_songs),
                    subtitle = stringResource(com.metrolist.music.R.string.combine_local_and_youtube_liked_songs_description),
                    switch = (combineLocalAndYouTubeLiked to { viewModel.setCombineLocalAndYouTubeLiked(it) })
                )
                 */
                SettingItem(
                    title = stringResource(com.metrolist.music.R.string.proxy),
                    subtitle = stringResource(com.metrolist.music.R.string.proxy_description),
                    switch = (usingProxy to { viewModel.setUsingProxy(it) }),
                )
            }
        }
        item(key = "proxy") {
            Crossfade(usingProxy) { it ->
                if (it) {
                    Column {
                        SettingItem(
                            title = stringResource(com.metrolist.music.R.string.proxy_type),
                            subtitle =
                                when (proxyType) {
                                    DataStoreManager.ProxyType.PROXY_TYPE_HTTP -> stringResource(com.metrolist.music.R.string.http)
                                    DataStoreManager.ProxyType.PROXY_TYPE_SOCKS -> stringResource(com.metrolist.music.R.string.socks)
                                },
                            onClick = {
                                viewModel.setAlertData(
                                    SettingAlertState(
                                        title = proxyTypeTitle,
                                        selectOne =
                                            SettingAlertState.SelectData(
                                                listSelect =
                                                    listOf(
                                                        (proxyType == DataStoreManager.ProxyType.PROXY_TYPE_HTTP) to httpStr,
                                                        (proxyType == DataStoreManager.ProxyType.PROXY_TYPE_SOCKS) to socksStr,
                                                    ),
                                            ),
                                        confirm =
                                            changeStr to { state ->
                                                viewModel.setProxy(
                                                    if (state.selectOne?.getSelected() == socksStr) {
                                                        DataStoreManager.ProxyType.PROXY_TYPE_SOCKS
                                                    } else {
                                                        DataStoreManager.ProxyType.PROXY_TYPE_HTTP
                                                    },
                                                    proxyHost,
                                                    proxyPort,
                                                )
                                            },
                                        dismiss = cancelStr,
                                    ),
                                )
                            },
                        )
                        val proxyHostTitle = stringResource(com.metrolist.music.R.string.proxy_host)
                        val proxyHostMessage = stringResource(com.metrolist.music.R.string.proxy_host_message)
                        val invalidHostStr = stringResource(com.metrolist.music.R.string.invalid_host)
                        SettingItem(
                            title = proxyHostTitle,
                            subtitle = proxyHost,
                            onClick = {
                                viewModel.setAlertData(
                                    SettingAlertState(
                                        title = proxyHostTitle,
                                        message = proxyHostMessage,
                                        textField =
                                            SettingAlertState.TextFieldData(
                                                label = proxyHostTitle,
                                                value = proxyHost,
                                                verifyCodeBlock = {
                                                    isValidProxyHost(it) to invalidHostStr
                                                },
                                            ),
                                        confirm =
                                            changeStr to { state ->
                                                viewModel.setProxy(
                                                    proxyType,
                                                    state.textField?.value ?: "",
                                                    proxyPort,
                                                )
                                            },
                                        dismiss = cancelStr,
                                    ),
                                )
                            },
                        )
                        val proxyPortTitle = stringResource(com.metrolist.music.R.string.proxy_port)
                        val proxyPortMessage = stringResource(com.metrolist.music.R.string.proxy_port_message)
                        val invalidPortStr = stringResource(com.metrolist.music.R.string.invalid_port)
                        SettingItem(
                            title = proxyPortTitle,
                            subtitle = proxyPort.toString(),
                            onClick = {
                                viewModel.setAlertData(
                                    SettingAlertState(
                                        title = proxyPortTitle,
                                        message = proxyPortMessage,
                                        textField =
                                            SettingAlertState.TextFieldData(
                                                label = proxyPortTitle,
                                                value = proxyPort.toString(),
                                                verifyCodeBlock = {
                                                    (it.toIntOrNull() != null) to invalidPortStr
                                                },
                                            ),
                                        confirm =
                                            changeStr to { state ->
                                                viewModel.setProxy(
                                                    proxyType,
                                                    proxyHost,
                                                    state.textField?.value?.toIntOrNull() ?: 0,
                                                )
                                            },
                                        dismiss = cancelStr,
                                    ),
                                )
                            },
                        )
                        val proxyUsernameTitle = stringResource(com.metrolist.music.R.string.proxy_username)
                        val proxyUsernameMessage = stringResource(com.metrolist.music.R.string.proxy_username_message)
                        SettingItem(
                            title = proxyUsernameTitle,
                            subtitle = proxyUsername,
                            onClick = {
                                viewModel.setAlertData(
                                    SettingAlertState(
                                        title = proxyUsernameTitle,
                                        message = proxyUsernameMessage,
                                        textField =
                                            SettingAlertState.TextFieldData(
                                                label = proxyUsernameTitle,
                                                value = proxyUsername,
                                            ),
                                        confirm =
                                            changeStr to { state ->
                                                viewModel.setProxyCredentials(
                                                    state.textField?.value ?: "",
                                                    proxyPassword,
                                                )
                                            },
                                        dismiss = cancelStr,
                                    ),
                                )
                            },
                        )
                        val proxyPasswordTitle = stringResource(com.metrolist.music.R.string.proxy_password)
                        val proxyPasswordMessage = stringResource(com.metrolist.music.R.string.proxy_password_message)
                        SettingItem(
                            title = proxyPasswordTitle,
                            subtitle =
                                if (proxyPassword.isEmpty()) {
                                    ""
                                } else {
                                    "\u2022".repeat(proxyPassword.length)
                                },
                            onClick = {
                                viewModel.setAlertData(
                                    SettingAlertState(
                                        title = proxyPasswordTitle,
                                        message = proxyPasswordMessage,
                                        textField =
                                            SettingAlertState.TextFieldData(
                                                label = proxyPasswordTitle,
                                                value = proxyPassword,
                                            ),
                                        confirm =
                                            changeStr to { state ->
                                                viewModel.setProxyCredentials(
                                                    proxyUsername,
                                                    state.textField?.value ?: "",
                                                )
                                            },
                                        dismiss = cancelStr,
                                    ),
                                )
                            },
                        )
                    }
                }
            }
        }
        if (true) {
            item(key = "audio") {
                Column {
                    Text(
                        text = stringResource(com.metrolist.music.R.string.audio),
                        style = typo().labelMedium,
                        color = white,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                    SettingItem(
                        title = stringResource(com.metrolist.music.R.string.normalize_volume),
                        subtitle = stringResource(com.metrolist.music.R.string.balance_media_loudness),
                        switch = (normalizeVolume to { viewModel.setNormalizeVolume(it) }),
                    )
                    SettingItem(
                        title = stringResource(com.metrolist.music.R.string.skip_silent),
                        subtitle = stringResource(com.metrolist.music.R.string.skip_no_music_part),
                        switch = (skipSilent to { viewModel.setSkipSilent(it) }),
                    )
                    SettingItem(
                        title = stringResource(com.metrolist.music.R.string.open_system_equalizer),
                        subtitle = stringResource(com.metrolist.music.R.string.use_your_system_equalizer),
                        onClick = {
                            coroutineScope.launch {
                                resultLauncher.launch()
                            }
                        },
                    )
                }
            }
        }
        item(key = "playback") {
            Column {
                Text(
                    text = stringResource(com.metrolist.music.R.string.playback),
                    style = typo().labelMedium,
                    color = white,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                SettingItem(
                    title = stringResource(com.metrolist.music.R.string.save_playback_state),
                    subtitle = stringResource(com.metrolist.music.R.string.save_shuffle_and_repeat_mode),
                    switch = (savePlaybackState to { viewModel.setSavedPlaybackState(it) }),
                )
                SettingItem(
                    title = stringResource(com.metrolist.music.R.string.save_last_played),
                    subtitle = stringResource(com.metrolist.music.R.string.save_last_played_track_and_queue),
                    switch = (saveLastPlayed to { viewModel.setSaveLastPlayed(it) }),
                )
                if (true) {
                    SettingItem(
                        title = stringResource(com.metrolist.music.R.string.kill_service_on_exit),
                        subtitle = stringResource(com.metrolist.music.R.string.kill_service_on_exit_description),
                        switch = (killServiceOnExit to { viewModel.setKillServiceOnExit(it) }),
                    )
                    SettingItem(
                        title = stringResource(com.metrolist.music.R.string.keep_service_alive),
                        subtitle = stringResource(com.metrolist.music.R.string.keep_service_alive_description),
                        switch = (keepServiceAlive to { viewModel.setKeepServiceAlive(it) }),
                    )
                }
            }
        }
        // Crossfade Settings (all platforms)
        item(key = "crossfade_settings") {
            Column {
                SettingItem(
                    title = stringResource(com.metrolist.music.R.string.crossfade),
                    subtitle = stringResource(com.metrolist.music.R.string.crossfade_description),
                    smallSubtitle = true,
                    switch = (crossfadeEnabled to { viewModel.setCrossfadeEnabled(it) }),
                )
                val crossfadeDurationTitle = stringResource(com.metrolist.music.R.string.crossfade_duration)
                val crossfadeAutoStr = stringResource(com.metrolist.music.R.string.crossfade_auto)
                AnimatedVisibility(visible = crossfadeEnabled) {
                    Column {
                        SettingItem(
                            title = crossfadeDurationTitle,
                            subtitle =
                                if (crossfadeDuration == DataStoreManager.CROSSFADE_DURATION_AUTO) {
                                    crossfadeAutoStr
                                } else {
                                    "${crossfadeDuration / 1000}s"
                                },
                            onClick = {
                                viewModel.setAlertData(
                                    SettingAlertState(
                                        title = crossfadeDurationTitle,
                                        selectOne =
                                            SettingAlertState.SelectData(
                                                listSelect =
                                                    listOf(
                                                        (crossfadeDuration == DataStoreManager.CROSSFADE_DURATION_AUTO) to
                                                            crossfadeAutoStr,
                                                        (crossfadeDuration == 1000) to "1s",
                                                        (crossfadeDuration == 2000) to "2s",
                                                        (crossfadeDuration == 3000) to "3s",
                                                        (crossfadeDuration == 5000) to "5s",
                                                        (crossfadeDuration == 8000) to "8s",
                                                        (crossfadeDuration == 10000) to "10s",
                                                        (crossfadeDuration == 12000) to "12s",
                                                        (crossfadeDuration == 15000) to "15s",
                                                        (crossfadeDuration == 20000) to "20s",
                                                        (crossfadeDuration == 30000) to "30s",
                                                    ),
                                            ),
                                        confirm =
                                            changeStr to { state ->
                                                val duration =
                                                    when (state.selectOne?.getSelected()) {
                                                        crossfadeAutoStr -> DataStoreManager.CROSSFADE_DURATION_AUTO
                                                        "1s" -> 1000
                                                        "2s" -> 2000
                                                        "3s" -> 3000
                                                        "5s" -> 5000
                                                        "8s" -> 8000
                                                        "10s" -> 10000
                                                        "12s" -> 12000
                                                        "15s" -> 15000
                                                        "20s" -> 20000
                                                        "30s" -> 30000
                                                        else -> 5000
                                                    }
                                                viewModel.setCrossfadeDuration(duration)
                                            },
                                        dismiss = cancelStr,
                                    ),
                                )
                            },
                        )
                        if (true) {
                            SettingItem(
                                title = stringResource(com.metrolist.music.R.string.crossfade_dj_mode),
                                subtitle = stringResource(com.metrolist.music.R.string.crossfade_dj_mode_description),
                                smallSubtitle = true,
                                switch = ((crossfadeDjMode) to { viewModel.setCrossfadeDjMode(it) }),
                            )
                        }
                    }
                }
            }
        }
        item(key = "lyrics") {
            Column {
                Text(
                    text = stringResource(com.metrolist.music.R.string.lyrics),
                    style = typo().labelMedium,
                    color = white,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                val mainLyricsProviderTitle = stringResource(com.metrolist.music.R.string.main_lyrics_provider)
                val xevraeLyricsStr = stringResource(com.metrolist.music.R.string.xevrae_lyrics)
                val youtubeTranscriptStr = stringResource(com.metrolist.music.R.string.youtube_transcript)
                val lrclibStr = stringResource(com.metrolist.music.R.string.lrclib)
                val betterLyricsStr = stringResource(com.metrolist.music.R.string.better_lyrics)
                SettingItem(
                    title = mainLyricsProviderTitle,
                    subtitle =
                        when (mainLyricsProvider) {
                            DataStoreManager.XEVRAE -> xevraeLyricsStr
                            DataStoreManager.YOUTUBE -> youtubeTranscriptStr
                            DataStoreManager.LRCLIB -> lrclibStr
                            DataStoreManager.BETTER_LYRICS -> betterLyricsStr
                            else -> "Unknown"
                        },
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = mainLyricsProviderTitle,
                                selectOne =
                                    SettingAlertState.SelectData(
                                        listSelect =
                                            listOf(
                                                (mainLyricsProvider == DataStoreManager.XEVRAE) to xevraeLyricsStr,
                                                (mainLyricsProvider == DataStoreManager.YOUTUBE) to youtubeTranscriptStr,
                                                (mainLyricsProvider == DataStoreManager.LRCLIB) to lrclibStr,
                                                (mainLyricsProvider == DataStoreManager.BETTER_LYRICS) to betterLyricsStr,
                                            ),
                                    ),
                                confirm =
                                    changeStr to { state ->
                                        viewModel.setLyricsProvider(
                                            when (state.selectOne?.getSelected()) {
                                                xevraeLyricsStr -> DataStoreManager.XEVRAE
                                                youtubeTranscriptStr -> DataStoreManager.YOUTUBE
                                                lrclibStr -> DataStoreManager.LRCLIB
                                                betterLyricsStr -> DataStoreManager.BETTER_LYRICS
                                                else -> DataStoreManager.XEVRAE
                                            },
                                        )
                                    },
                                dismiss = cancelStr,
                            ),
                        )
                    },
                )

                val translationLanguageTitle = stringResource(com.metrolist.music.R.string.translation_language)
                val invalidLanguageCodeStr = stringResource(com.metrolist.music.R.string.invalid_language_code)
                val translationLanguageMessage = stringResource(com.metrolist.music.R.string.translation_language_message)
                SettingItem(
                    title = translationLanguageTitle,
                    subtitle = translationLanguage ?: "",
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = translationLanguageTitle,
                                textField =
                                    SettingAlertState.TextFieldData(
                                        label = translationLanguageTitle,
                                        value = translationLanguage ?: "",
                                        verifyCodeBlock = {
                                            (it.length == 2 && it.isTwoLetterCode()) to invalidLanguageCodeStr
                                        },
                                    ),
                                message = translationLanguageMessage,
                                confirm =
                                    changeStr to { state ->
                                        viewModel.setTranslationLanguage(state.textField?.value ?: "")
                                    },
                                dismiss = cancelStr,
                            ),
                        )
                    },
                    isEnable = true,
                )
                val youtubeSubtitleLanguageTitle = stringResource(com.metrolist.music.R.string.youtube_subtitle_language)
                val youtubeSubtitleLanguageMessage = stringResource(com.metrolist.music.R.string.youtube_subtitle_language_message)
                SettingItem(
                    title = youtubeSubtitleLanguageTitle,
                    subtitle = youtubeSubtitleLanguage,
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = youtubeSubtitleLanguageTitle,
                                textField =
                                    SettingAlertState.TextFieldData(
                                        label = youtubeSubtitleLanguageTitle,
                                        value = youtubeSubtitleLanguage,
                                        verifyCodeBlock = {
                                            (it.length == 2 && it.isTwoLetterCode()) to invalidLanguageCodeStr
                                        },
                                    ),
                                message = youtubeSubtitleLanguageMessage,
                                confirm =
                                    changeStr to { state ->
                                        viewModel.setYoutubeSubtitleLanguage(state.textField?.value ?: "")
                                    },
                                dismiss = cancelStr,
                            ),
                        )
                    },
                )
                val helpBuildLyricsDatabaseTitle = stringResource(com.metrolist.music.R.string.help_build_lyrics_database)
                val helpBuildLyricsDatabaseDescription = stringResource(com.metrolist.music.R.string.help_build_lyrics_database_description)
                SettingItem(
                    title = helpBuildLyricsDatabaseTitle,
                    subtitle = helpBuildLyricsDatabaseDescription,
                    switch = (helpBuildLyricsDatabase to { viewModel.setHelpBuildLyricsDatabase(it) }),
                )
                val contributorNameTitle = stringResource(com.metrolist.music.R.string.contributor_name)
                val anonymousStr = stringResource(com.metrolist.music.R.string.anonymous)
                SettingItem(
                    title = contributorNameTitle,
                    subtitle = contributor.first.ifEmpty { anonymousStr },
                    isEnable = helpBuildLyricsDatabase,
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = contributorNameTitle,
                                textField =
                                    SettingAlertState.TextFieldData(
                                        label = contributorNameTitle,
                                        value = "",
                                    ),
                                message = "",
                                confirm =
                                    setStr to { state ->
                                        viewModel.setContributorName(state.textField?.value ?: "")
                                    },
                                dismiss = cancelStr,
                            ),
                        )
                    },
                )
                val contributorEmailTitle = stringResource(com.metrolist.music.R.string.contributor_email)
                SettingItem(
                    title = contributorEmailTitle,
                    subtitle = contributor.second.ifEmpty { anonymousStr },
                    isEnable = helpBuildLyricsDatabase,
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = contributorEmailTitle,
                                textField =
                                    SettingAlertState.TextFieldData(
                                        label = contributorEmailTitle,
                                        value = "",
                                        verifyCodeBlock = {
                                            if (it.isNotEmpty()) {
                                                (it.contains("@")) to invalidStr
                                            } else {
                                                true to ""
                                            }
                                        },
                                    ),
                                message = "",
                                confirm =
                                    setStr to { state ->
                                        viewModel.setContributorEmail(state.textField?.value ?: "")
                                    },
                                dismiss = cancelStr,
                            ),
                        )
                    },
                )
            }
        }
        item(key = "AI") {
            Column {
                val aiTitle = stringResource(com.metrolist.music.R.string.ai)
                Text(text = aiTitle, style = typo().labelMedium, color = white, modifier = Modifier.padding(vertical = 8.dp))
                val aiProviderTitle = stringResource(com.metrolist.music.R.string.ai_provider)
                val openaiStr = stringResource(com.metrolist.music.R.string.openai)
                val geminiStr = stringResource(com.metrolist.music.R.string.gemini)
                val openaiCompatibleStr = stringResource(com.metrolist.music.R.string.openai_api_compatible)
                SettingItem(
                    title = aiProviderTitle,
                    subtitle =
                        when (aiProvider) {
                            DataStoreManager.AI_PROVIDER_OPENAI -> openaiStr
                            DataStoreManager.AI_PROVIDER_GEMINI -> geminiStr
                            DataStoreManager.AI_PROVIDER_CUSTOM_OPENAI -> openaiCompatibleStr
                            else -> "Unknown"
                        },
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = aiProviderTitle,
                                selectOne =
                                    SettingAlertState.SelectData(
                                        listSelect =
                                            listOf(
                                                (aiProvider == DataStoreManager.AI_PROVIDER_OPENAI) to openaiStr,
                                                (aiProvider == DataStoreManager.AI_PROVIDER_GEMINI) to geminiStr,
                                                (aiProvider == DataStoreManager.AI_PROVIDER_CUSTOM_OPENAI) to openaiCompatibleStr,
                                            ),
                                    ),
                                confirm =
                                    changeStr to { state ->
                                        viewModel.setAIProvider(
                                            when (state.selectOne?.getSelected()) {
                                                openaiStr -> DataStoreManager.AI_PROVIDER_OPENAI
                                                geminiStr -> DataStoreManager.AI_PROVIDER_GEMINI
                                                openaiCompatibleStr -> DataStoreManager.AI_PROVIDER_CUSTOM_OPENAI
                                                else -> DataStoreManager.AI_PROVIDER_OPENAI
                                            },
                                        )
                                    },
                                dismiss = cancelStr,
                            ),
                        )
                    },
                )
                val aiApiKeyTitle = stringResource(com.metrolist.music.R.string.ai_api_key)
                SettingItem(
                    title = aiApiKeyTitle,
                    subtitle = if (isHasApiKey) "XXXXXXXXXX" else "N/A",
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = aiApiKeyTitle,
                                textField =
                                    SettingAlertState.TextFieldData(
                                        label = aiApiKeyTitle,
                                        value = "",
                                        verifyCodeBlock = {
                                            (it.isNotEmpty()) to invalidStr
                                        },
                                    ),
                                message = "",
                                confirm =
                                    setStr to { state ->
                                        viewModel.setAIApiKey(state.textField?.value ?: "")
                                    },
                                dismiss = cancelStr,
                            ),
                        )
                    },
                )
                val customAiModelIdTitle = stringResource(com.metrolist.music.R.string.custom_ai_model_id)
                val defaultModelsStr = stringResource(com.metrolist.music.R.string.default_models)
                val customModelIdMessages = stringResource(com.metrolist.music.R.string.custom_model_id_messages)
                SettingItem(
                    title = customAiModelIdTitle,
                    subtitle = customModelId.ifEmpty { defaultModelsStr },
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = customAiModelIdTitle,
                                textField =
                                    SettingAlertState.TextFieldData(
                                        label = customAiModelIdTitle,
                                        value = "",
                                        verifyCodeBlock = {
                                            (it.isNotEmpty() && !it.contains(" ")) to invalidStr
                                        },
                                    ),
                                message = customModelIdMessages,
                                confirm =
                                    setStr to { state ->
                                        viewModel.setCustomModelId(state.textField?.value ?: "")
                                    },
                                dismiss = cancelStr,
                            ),
                        )
                    },
                )
                // Custom OpenAI Base URL - only show when Custom OpenAI is selected
                if (aiProvider == DataStoreManager.AI_PROVIDER_CUSTOM_OPENAI) {
                    SettingItem(
                        title = "Custom Base URL",
                        subtitle = customOpenAIBaseUrl.ifEmpty { "https://api.openai.com/v1/" },
                        onClick = {
                            viewModel.setAlertData(
                                SettingAlertState(
                                    title = "Custom Base URL",
                                    textField =
                                        SettingAlertState.TextFieldData(
                                            label = "Base URL",
                                            value = customOpenAIBaseUrl,
                                            verifyCodeBlock = {
                                                (it.isEmpty() || it.startsWith("http")) to "Invalid URL format"
                                            },
                                        ),
                                    message = "Enter OpenAI-compatible API base URL (e.g., https://api.openai.com/v1/)",
                                    confirm =
                                        setStr to { state ->
                                            viewModel.setCustomOpenAIBaseUrl(state.textField?.value ?: "")
                                        },
                                    dismiss = cancelStr,
                                ),
                            )
                        },
                    )
                    SettingItem(
                        title = "Custom Headers",
                        subtitle = if (customOpenAIHeaders.isNotEmpty()) "Configured" else "Not set",
                        onClick = {
                            viewModel.setAlertData(
                                SettingAlertState(
                                    title = "Custom Headers (JSON)",
                                    textField =
                                        SettingAlertState.TextFieldData(
                                            label = "Headers JSON",
                                            value = customOpenAIHeaders,
                                            verifyCodeBlock = { input ->
                                                if (input.isEmpty()) {
                                                    true to null
                                                } else {
                                                    try {
                                                        // Simple validation: check if it looks like JSON
                                                        val trimmed = input.trim()
                                                        (trimmed.startsWith("{") && trimmed.endsWith("}")) to "Invalid JSON format"
                                                    } catch (e: Exception) {
                                                        false to "Invalid JSON format"
                                                    }
                                                }
                                            },
                                        ),
                                    message = "Enter custom headers in JSON format:\n{\"key1\":\"value1\",\"key2\":\"value2\"}",
                                    confirm =
                                        setStr to { state ->
                                            viewModel.setCustomOpenAIHeaders(state.textField?.value ?: "")
                                        },
                                    dismiss = cancelStr,
                                ),
                            )
                        },
                    )
                }
                val useAiTranslationTitle = stringResource(com.metrolist.music.R.string.use_ai_translation)
                val useAiTranslationDescription = stringResource(com.metrolist.music.R.string.use_ai_translation_description)
                SettingItem(
                    title = useAiTranslationTitle,
                    subtitle = useAiTranslationDescription,
                    switch = (useAITranslation to { viewModel.setAITranslation(it) }),
                    isEnable = isHasApiKey,
                    onDisable = {
                        if (useAITranslation) {
                            viewModel.setAITranslation(false)
                        }
                    },
                )
            }
        }
        item(key = "spotify") {
            Column {
                Text(
                    text = stringResource(com.metrolist.music.R.string.spotify),
                    style = typo().labelMedium,
                    color = white,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                SettingItem(
                    title = stringResource(com.metrolist.music.R.string.log_in_to_spotify),
                    subtitle =
                        if (spotifyLoggedIn) {
                            stringResource(com.metrolist.music.R.string.logged_in)
                        } else {
                            stringResource(com.metrolist.music.R.string.intro_login_to_spotify)
                        },
                    onClick = {
                        if (spotifyLoggedIn) {
                            viewModel.setSpotifyLogIn(false)
                        } else {
                            navController.navigate(SpotifyLoginDestination)
                        }
                    },
                )
                SettingItem(
                    title = stringResource(com.metrolist.music.R.string.enable_spotify_lyrics),
                    subtitle = stringResource(com.metrolist.music.R.string.spotify_lyrícs_info),
                    switch = (spotifyLyrics to { viewModel.setSpotifyLyrics(it) }),
                    isEnable = spotifyLoggedIn,
                    onDisable = {
                        if (spotifyLyrics) {
                            viewModel.setSpotifyLyrics(false)
                        }
                    },
                )
                SettingItem(
                    title = stringResource(com.metrolist.music.R.string.enable_canvas),
                    subtitle = stringResource(com.metrolist.music.R.string.canvas_info),
                    switch = (spotifyCanvas to { viewModel.setSpotifyCanvas(it) }),
                    isEnable = spotifyLoggedIn,
                    onDisable = {
                        if (spotifyCanvas) {
                            viewModel.setSpotifyCanvas(false)
                        }
                    },
                )
            }
        }
        item(key = "discord") {
            Column {
                Text(
                    text = stringResource(com.metrolist.music.R.string.discord_integration),
                    style = typo().labelMedium,
                    color = white,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                SettingItem(
                    title = stringResource(com.metrolist.music.R.string.log_in_to_discord),
                    subtitle =
                        if (discordLoggedIn) {
                            stringResource(com.metrolist.music.R.string.logged_in)
                        } else {
                            stringResource(com.metrolist.music.R.string.intro_login_to_discord)
                        },
                    onClick = {
                        if (discordLoggedIn) {
                            viewModel.logOutDiscord()
                        } else {
                            navController.navigate(DiscordLoginDestination)
                        }
                    },
                )
                SettingItem(
                    title = stringResource(com.metrolist.music.R.string.enable_rich_presence),
                    subtitle = stringResource(com.metrolist.music.R.string.rich_presence_info),
                    switch = (richPresenceEnabled to { viewModel.setDiscordRichPresenceEnabled(it) }),
                    isEnable = discordLoggedIn,
                    onDisable = {
                        if (discordLoggedIn) {
                            viewModel.setDiscordRichPresenceEnabled(false)
                        }
                    },
                )
            }
        }
        item(key = "sponsor_block") {
            Column {
                Text(
                    text = stringResource(com.metrolist.music.R.string.sponsorBlock),
                    style = typo().labelMedium,
                    color = white,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                SettingItem(
                    title = stringResource(com.metrolist.music.R.string.enable_sponsor_block),
                    subtitle = stringResource(com.metrolist.music.R.string.skip_sponsor_part_of_video),
                    switch = (enableSponsorBlock to { viewModel.setSponsorBlockEnabled(it) }),
                )
                val listName =
                    SponsorBlockType.toList().map { it.displayString() }
                SettingItem(
                    title = stringResource(com.metrolist.music.R.string.categories_sponsor_block),
                    subtitle = stringResource(com.metrolist.music.R.string.what_segments_will_be_skipped),
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = categoriesSponsorBlockStr,
                                multipleSelect =
                                    SettingAlertState.SelectData(
                                        listSelect =
                                            listName
                                                .mapIndexed { index, item ->
                                                    (
                                                        skipSegments?.contains(
                                                            SponsorBlockType.toList()[index].value,
                                                        ) == true
                                                    ) to item
                                                }.also {
                                                    Logger.w("SettingScreen", "SettingAlertState: $skipSegments")
                                                    Logger.w("SettingScreen", "SettingAlertState: $it")
                                                },
                                    ),
                                confirm =
                                    saveStr to { state ->
                                        viewModel.setSponsorBlockCategories(
                                            state.multipleSelect
                                                ?.getListSelected()
                                                ?.map { selected ->
                                                    listName.indexOf(selected)
                                                }?.mapNotNull { s ->
                                                    SponsorBlockType.toList().getOrNull(s)?.value
                                                } ?: listOf(),
                                        )
                                    },
                                dismiss = cancelStr,
                            ),
                        )
                    },
                    isEnable = enableSponsorBlock,
                )
                val beforeUrl = stringResource(com.metrolist.music.R.string.sponsor_block_intro).substringBefore("https://sponsor.ajay.app/")
                val afterUrl = stringResource(com.metrolist.music.R.string.sponsor_block_intro).substringAfter("https://sponsor.ajay.app/")
                Text(
                    buildAnnotatedString {
                        append(beforeUrl)
                        withLink(
                            LinkAnnotation.Url(
                                "https://sponsor.ajay.app/",
                                TextLinkStyles(style = SpanStyle(color = md_theme_dark_primary)),
                            ),
                        ) {
                            append("https://sponsor.ajay.app/")
                        }
                        append(afterUrl)
                    },
                    style = typo().bodySmall,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                )
            }
        }
        if (true) {
            item(key = "storage") {
                val storageTitle = stringResource(com.metrolist.music.R.string.storage)
                val clearStr = stringResource(com.metrolist.music.R.string.clear)
                Column {
                    Text(
                        text = storageTitle,
                        style = typo().labelMedium,
                        color = white,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                    val clearPlayerCacheTitle = stringResource(com.metrolist.music.R.string.clear_player_cache)
                    SettingItem(
                        title = stringResource(com.metrolist.music.R.string.player_cache),
                        subtitle = "${playerCache.bytesToMB()} MB",
                        onClick = {
                            viewModel.setBasicAlertData(
                                SettingBasicAlertState(
                                    title = clearPlayerCacheTitle,
                                    message = null,
                                    confirm =
                                        clearStr to {
                                            viewModel.clearPlayerCache()
                                        },
                                    dismiss = cancelStr,
                                ),
                            )
                        },
                    )
                    val clearDownloadedCacheTitle = stringResource(com.metrolist.music.R.string.clear_downloaded_cache)
                    SettingItem(
                        title = stringResource(com.metrolist.music.R.string.downloaded_cache),
                        subtitle = "${downloadedCache.bytesToMB()} MB",
                        onClick = {
                            viewModel.setBasicAlertData(
                                SettingBasicAlertState(
                                    title = clearDownloadedCacheTitle,
                                    message = null,
                                    confirm =
                                        clearStr to {
                                            viewModel.clearDownloadedCache()
                                        },
                                    dismiss = cancelStr,
                                ),
                            )
                        },
                    )
                    val clearThumbnailCacheTitle = stringResource(com.metrolist.music.R.string.clear_thumbnail_cache)
                    SettingItem(
                        title = stringResource(com.metrolist.music.R.string.thumbnail_cache),
                        subtitle = "${thumbnailCache.bytesToMB()} MB",
                        onClick = {
                            viewModel.setBasicAlertData(
                                SettingBasicAlertState(
                                    title = clearThumbnailCacheTitle,
                                    message = null,
                                    confirm =
                                        clearStr to {
                                            viewModel.clearThumbnailCache(platformContext)
                                        },
                                    dismiss = cancelStr,
                                ),
                            )
                        },
                    )
                    val clearCanvasCacheTitle = stringResource(com.metrolist.music.R.string.clear_canvas_cache)
                    SettingItem(
                        title = stringResource(com.metrolist.music.R.string.spotify_canvas_cache),
                        subtitle = "${canvasCache.bytesToMB()} MB",
                        onClick = {
                            viewModel.setBasicAlertData(
                                SettingBasicAlertState(
                                    title = clearCanvasCacheTitle,
                                    message = null,
                                    confirm =
                                        clearStr to {
                                            viewModel.clearCanvasCache()
                                        },
                                    dismiss = cancelStr,
                                ),
                            )
                        },
                    )
                    val limitPlayerCacheTitle = stringResource(com.metrolist.music.R.string.limit_player_cache)
                    SettingItem(
                        title = limitPlayerCacheTitle,
                        subtitle = LIMIT_CACHE_SIZE.getItemFromData(limitPlayerCache).toString(),
                        onClick = {
                            viewModel.setAlertData(
                                SettingAlertState(
                                    title = limitPlayerCacheTitle,
                                    selectOne =
                                        SettingAlertState.SelectData(
                                            listSelect =
                                                LIMIT_CACHE_SIZE.items.map { item ->
                                                    (item == LIMIT_CACHE_SIZE.getItemFromData(limitPlayerCache)) to item.toString()
                                                },
                                        ),
                                    confirm =
                                        changeStr to { state ->
                                            viewModel.setPlayerCacheLimit(
                                                LIMIT_CACHE_SIZE.getDataFromItem(state.selectOne?.getSelected()),
                                            )
                                        },
                                    dismiss = cancelStr,
                                ),
                            )
                        },
                    )
                    Box(
                        Modifier.padding(
                            horizontal = 24.dp,
                            vertical = 16.dp,
                        ),
                    ) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(0.dp),
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .onGloballyPositioned { layoutCoordinates ->
                                        with(localDensity) {
                                            width =
                                                layoutCoordinates.size.width
                                                    .toDp()
                                                    .value
                                                    .toInt()
                                        }
                                    },
                        ) {
                            item {
                                Box(
                                    modifier =
                                        Modifier
                                            .width(
                                                (fraction.otherApp * width).dp,
                                            ).background(
                                                md_theme_dark_primary,
                                            ).fillMaxHeight(),
                                )
                            }
                            item {
                                Box(
                                    modifier =
                                        Modifier
                                            .width(
                                                (fraction.downloadCache * width).dp,
                                            ).background(
                                                Color(0xD540FF17),
                                            ).fillMaxHeight(),
                                )
                            }
                            item {
                                Box(
                                    modifier =
                                        Modifier
                                            .width(
                                                (fraction.playerCache * width).dp,
                                            ).background(
                                                Color(0xD5FFFF00),
                                            ).fillMaxHeight(),
                                )
                            }
                            item {
                                Box(
                                    modifier =
                                        Modifier
                                            .width(
                                                (fraction.canvasCache * width).dp,
                                            ).background(
                                                Color.Cyan,
                                            ).fillMaxHeight(),
                                )
                            }
                            item {
                                Box(
                                    modifier =
                                        Modifier
                                            .width(
                                                (fraction.thumbCache * width).dp,
                                            ).background(
                                                Color.Magenta,
                                            ).fillMaxHeight(),
                                )
                            }
                            item {
                                Box(
                                    modifier =
                                        Modifier
                                            .width(
                                                (fraction.appDatabase * width).dp,
                                            ).background(
                                                Color.White,
                                            ),
                                )
                            }
                            item {
                                Box(
                                    modifier =
                                        Modifier
                                            .width(
                                                (fraction.freeSpace * width).dp,
                                            ).background(
                                                Color.DarkGray,
                                            ).fillMaxHeight(),
                                )
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                    ) {
                        Box(
                            Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    md_theme_dark_primary,
                                ),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = stringResource(com.metrolist.music.R.string.other_app), style = typo().bodySmall)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                    ) {
                        Box(
                            Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    Color.Green,
                                ),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = stringResource(com.metrolist.music.R.string.downloaded_cache), style = typo().bodySmall)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                    ) {
                        Box(
                            Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    Color.Yellow,
                                ),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = stringResource(com.metrolist.music.R.string.player_cache), style = typo().bodySmall)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                    ) {
                        Box(
                            Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    Color.Cyan,
                                ),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = stringResource(com.metrolist.music.R.string.spotify_canvas_cache), style = typo().bodySmall)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                    ) {
                        Box(
                            Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    Color.Magenta,
                                ),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = stringResource(com.metrolist.music.R.string.thumbnail_cache), style = typo().bodySmall)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                    ) {
                        Box(
                            Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    Color.White,
                                ),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = stringResource(com.metrolist.music.R.string.database), style = typo().bodySmall)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                    ) {
                        Box(
                            Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    Color.LightGray,
                                ),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = stringResource(com.metrolist.music.R.string.free_space), style = typo().bodySmall)
                    }
                }
            }
        }
        item(key = "backup") {
            Column {
                Text(
                    text = stringResource(com.metrolist.music.R.string.backup),
                    style = typo().labelMedium,
                    color = white,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                SettingItem(
                    title = stringResource(com.metrolist.music.R.string.backup_downloaded),
                    subtitle = stringResource(com.metrolist.music.R.string.backup_downloaded_description),
                    switch = (backupDownloaded to { viewModel.setBackupDownloaded(it) }),
                )
                // Auto Backup (Android only)
                if (true) {
                    SettingItem(
                        title = stringResource(com.metrolist.music.R.string.auto_backup),
                        subtitle = stringResource(com.metrolist.music.R.string.auto_backup_description),
                        switch = (autoBackupEnabled to { viewModel.setAutoBackupEnabled(it) }),
                    )
                    AnimatedVisibility(visible = autoBackupEnabled) {
                        Column {
                            SettingItem(
                                title = stringResource(com.metrolist.music.R.string.backup_frequency),
                                subtitle =
                                    when (autoBackupFrequency) {
                                        DataStoreManager.AUTO_BACKUP_FREQUENCY_DAILY -> stringResource(com.metrolist.music.R.string.daily)
                                        DataStoreManager.AUTO_BACKUP_FREQUENCY_WEEKLY -> stringResource(com.metrolist.music.R.string.weekly)
                                        DataStoreManager.AUTO_BACKUP_FREQUENCY_MONTHLY -> stringResource(com.metrolist.music.R.string.monthly)
                                        else -> stringResource(com.metrolist.music.R.string.daily)
                                    },
                                onClick = {
                                    viewModel.setAlertData(
                                        SettingAlertState(
                                            title = backupFrequencyStr,
                                            selectOne =
                                                SettingAlertState.SelectData(
                                                    listSelect =
                                                        listOf(
                                                            (autoBackupFrequency == DataStoreManager.AUTO_BACKUP_FREQUENCY_DAILY) to dailyStr,
                                                            (autoBackupFrequency == DataStoreManager.AUTO_BACKUP_FREQUENCY_WEEKLY) to weeklyStr,
                                                            (autoBackupFrequency == DataStoreManager.AUTO_BACKUP_FREQUENCY_MONTHLY) to monthlyStr,
                                                        ),
                                                    ),
                                                    confirm =
                                                    changeStr to { state ->
                                                    val frequency =
                                                    when (state.selectOne?.getSelected()) {
                                                        dailyStr -> DataStoreManager.AUTO_BACKUP_FREQUENCY_DAILY
                                                        weeklyStr -> DataStoreManager.AUTO_BACKUP_FREQUENCY_WEEKLY
                                                        monthlyStr -> DataStoreManager.AUTO_BACKUP_FREQUENCY_MONTHLY
                                                        else -> DataStoreManager.AUTO_BACKUP_FREQUENCY_DAILY
                                                    }
                                                    viewModel.setAutoBackupFrequency(frequency)
                                                },
                                            dismiss = cancelStr,
                                        ),
                                    )
                                },
                            )
                            SettingItem(
                                title = stringResource(com.metrolist.music.R.string.keep_backups),
                                subtitle = stringResource(com.metrolist.music.R.string.keep_backups_format, "$autoBackupMaxFiles"),
                                onClick = {
                                    viewModel.setAlertData(
                                        SettingAlertState(
                                            title = keepBackupsStr,
                                            selectOne =
                                                SettingAlertState.SelectData(
                                                    listSelect =
                                                        listOf(
                                                            (autoBackupMaxFiles == 3) to "3",
                                                            (autoBackupMaxFiles == 5) to "5",
                                                            (autoBackupMaxFiles == 10) to "10",
                                                            (autoBackupMaxFiles == 15) to "15",
                                                        ),
                                                ),
                                            confirm =
                                                changeStr to { state ->
                                                    val maxFiles = state.selectOne?.getSelected()?.toIntOrNull() ?: 5
                                                    viewModel.setAutoBackupMaxFiles(maxFiles)
                                                },
                                            dismiss = cancelStr,
                                        ),
                                    )
                                },
                            )
                            SettingItem(
                                title = stringResource(com.metrolist.music.R.string.last_backup),
                                subtitle =
                                    if (autoBackupLastTime == 0L) {
                                        stringResource(com.metrolist.music.R.string.never)
                                    } else {
                                        DateTimeFormatter
                                            .ofPattern("yyyy-MM-dd HH:mm:ss")
                                            .withZone(ZoneId.systemDefault())
                                            .format(Instant.ofEpochMilli(autoBackupLastTime))
                                    },
                            )
                        }
                    }
                }
                SettingItem(
                    title = stringResource(com.metrolist.music.R.string.backup),
                    subtitle = stringResource(com.metrolist.music.R.string.save_all_your_playlist_data),
                    onClick = {
                        coroutineScope.launch {
                            backupLauncher.launch()
                        }
                    },
                )
                SettingItem(
                    title = stringResource(com.metrolist.music.R.string.restore_your_data),
                    subtitle = stringResource(com.metrolist.music.R.string.restore_your_saved_data),
                    onClick = {
                        coroutineScope.launch {
                            restoreLauncher.launch()
                        }
                    },
                )
            }
        }
        item(key = "about_us") {
            Column {
                Text(
                    text = stringResource(com.metrolist.music.R.string.about_us),
                    style = typo().labelMedium,
                    color = white,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                SettingItem(
                    title = stringResource(com.metrolist.music.R.string.version),
                    subtitle = stringResource(com.metrolist.music.R.string.version_format, VersionManager.getVersionName()),
                    onClick = {
                        navController.navigate(CreditDestination)
                    },
                )
                SettingItem(
                    title = stringResource(com.metrolist.music.R.string.auto_check_for_update),
                    subtitle = stringResource(com.metrolist.music.R.string.auto_check_for_update_description),
                    switch = (autoCheckUpdate to { viewModel.setAutoCheckUpdate(it) }),
                )
                val updateChannelTitle = stringResource(com.metrolist.music.R.string.update_channel)
                SettingItem(
                    title = updateChannelTitle,
                    subtitle =
                        if (updateChannel == DataStoreManager.FDROID) {
                            "F-Droid"
                        } else {
                            "Xevrae GitHub Release"
                        },
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = updateChannelTitle,
                                selectOne =
                                    SettingAlertState.SelectData(
                                        listSelect =
                                            listOf(
                                                (updateChannel == DataStoreManager.FDROID) to "F-Droid",
                                                (updateChannel == DataStoreManager.GITHUB) to "Xevrae GitHub Release",
                                            ),
                                    ),
                                confirm =
                                    changeStr to { state ->
                                        viewModel.setUpdateChannel(
                                            when (state.selectOne?.getSelected()) {
                                                "F-Droid" -> DataStoreManager.FDROID
                                                "Xevrae GitHub Release" -> DataStoreManager.GITHUB
                                                else -> DataStoreManager.GITHUB
                                            },
                                        )
                                    },
                                dismiss = cancelStr,
                            ),
                        )
                    },
                )
                SettingItem(
                    title = stringResource(com.metrolist.music.R.string.check_for_update),
                    subtitle = checkForUpdateSubtitle,
                    onClick = {
                        sharedViewModel.checkForUpdate()
                    },
                )
                SettingItem(
                    title = stringResource(com.metrolist.music.R.string.author),
                    subtitle = stringResource(com.metrolist.music.R.string._dev),
                    onClick = {
                        uriHandler.openUri("https://github.com/t4ulquiorra")
                    },
                )
                SettingItem(
                    title = stringResource(com.metrolist.music.R.string.buy_me_a_coffee),
                    subtitle = stringResource(com.metrolist.music.R.string.donation),
                    onClick = {
                        uriHandler.openUri("https://github.com/sponsors/t4ulquiorra")
                    },
                )
                SettingItem(
                    title = stringResource(com.metrolist.music.R.string.third_party_libraries),
                    subtitle = stringResource(com.metrolist.music.R.string.description_and_licenses),
                    onClick = {
                        showThirdPartyLibraries = true
                    },
                )
            }
        }
        item(key = "end") {
            EndOfPage()
        }
    }
    val basisAlertData by viewModel.basicAlertData.collectAsStateWithLifecycle()
    if (basisAlertData != null) {
        val alertBasicState = basisAlertData ?: return
        AlertDialog(
            onDismissRequest = { viewModel.setBasicAlertData(null) },
            title = {
                Text(
                    text = alertBasicState.title,
                    style = typo().titleSmall,
                )
            },
            text = {
                if (alertBasicState.message != null) {
                    Text(text = alertBasicState.message)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        alertBasicState.confirm.second.invoke()
                        viewModel.setBasicAlertData(null)
                    },
                ) {
                    Text(text = alertBasicState.confirm.first)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.setBasicAlertData(null)
                    },
                ) {
                    Text(text = alertBasicState.dismiss)
                }
            },
        )
    }
    if (showYouTubeAccountDialog) {
        val noAccountStr = stringResource(com.metrolist.music.R.string.no_account)
        val signedInStr = stringResource(com.metrolist.music.R.string.signed_in)
        val logOutWarningStr = stringResource(com.metrolist.music.R.string.log_out_warning)
        val logOutStr = stringResource(com.metrolist.music.R.string.log_out)
        BasicAlertDialog(
            onDismissRequest = { },
            modifier = Modifier.wrapContentSize(),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = Color(0xFF242424),
                tonalElevation = AlertDialogDefaults.TonalElevation,
                shadowElevation = 1.dp,
            ) {
                val googleAccounts by viewModel.googleAccounts.collectAsStateWithLifecycle(
                    minActiveState = Lifecycle.State.RESUMED,
                )
                LaunchedEffect(googleAccounts) {
                    Logger.w(
                        "SettingScreen",
                        "LaunchedEffect: ${
                            googleAccounts.data?.map {
                                it.name to it.isUsed
                            }
                        }",
                    )
                }
                LazyColumn(modifier = Modifier.padding(8.dp)) {
                    item {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                        ) {
                            IconButton(
                                onClick = { showYouTubeAccountDialog = false },
                                colors =
                                    IconButtonDefaults.iconButtonColors().copy(
                                        contentColor = Color.White,
                                    ),
                                modifier =
                                    Modifier
                                        .align(Alignment.CenterStart)
                                        .fillMaxHeight(),
                            ) {
                                Icon(Icons.Outlined.Close, null, tint = Color.White)
                            }
                            Text(
                                stringResource(com.metrolist.music.R.string.youtube_account),
                                style = typo().titleMedium,
                                modifier =
                                    Modifier
                                        .align(Alignment.Center)
                                        .wrapContentHeight(align = Alignment.CenterVertically)
                                        .wrapContentWidth(),
                            )
                        }
                    }
                    if (googleAccounts is LocalResource.Success) {
                        val data = googleAccounts.data
                        if (data.isNullOrEmpty()) {
                            item {
                                Text(
                                    noAccountStr,
                                    style = typo().bodyMedium,
                                    textAlign = TextAlign.Center,
                                    modifier =
                                        Modifier
                                            .padding(12.dp)
                                            .fillMaxWidth(),
                                )
                            }
                        } else {
                            items(data) {
                                Row(
                                    modifier =
                                        Modifier
                                            .padding(vertical = 8.dp)
                                            .clickable {
                                                viewModel.setUsedAccount(it)
                                            },
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Spacer(Modifier.width(24.dp))
                                    AsyncImage(
                                        model =
                                            ImageRequest
                                                .Builder(LocalPlatformContext.current)
                                                .data(it.thumbnailUrl)
                                                .crossfade(550)
                                                .build(),
                                        placeholder = painterResource(com.metrolist.music.R.drawable.baseline_people_alt_24),
                                        error = painterResource(com.metrolist.music.R.drawable.baseline_people_alt_24),
                                        contentDescription = it.name,
                                        modifier =
                                            Modifier
                                                .size(48.dp)
                                                .clip(CircleShape),
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(it.name, style = typo().labelMedium, color = white)
                                        Text(it.email, style = typo().bodySmall)
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    AnimatedVisibility(it.isUsed) {
                                        Text(
                                            signedInStr,
                                            style = typo().bodySmall,
                                            maxLines = 2,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.widthIn(0.dp, 64.dp),
                                        )
                                    }
                                    Spacer(Modifier.width(24.dp))
                                }
                            }
                        }
                    } else {
                        item {
                            CenterLoadingBox(
                                Modifier
                                    .fillMaxWidth()
                                    .height(80.dp),
                            )
                        }
                    }
                    item {
                        Column {
                            ActionButton(
                                icon = painterResource(com.metrolist.music.R.drawable.baseline_people_alt_24),
                                text = com.metrolist.music.R.string.guest,
                            ) {
                                viewModel.setUsedAccount(null)
                                showYouTubeAccountDialog = false
                            }
                            ActionButton(
                                icon = painterResource(com.metrolist.music.R.drawable.baseline_close_24),
                                text = com.metrolist.music.R.string.log_out,
                            ) {
                                viewModel.setBasicAlertData(
                                    SettingBasicAlertState(
                                        title = "Warning",
                                        message = logOutWarningStr,
                                        confirm =
                                            logOutStr to {
                                                viewModel.logOutAllYouTube()
                                                showYouTubeAccountDialog = false
                                            },
                                        dismiss = cancelStr,
                                    ),
                                )
                            }
                            ActionButton(
                                icon = painterResource(com.metrolist.music.R.drawable.baseline_playlist_add_24),
                                text = com.metrolist.music.R.string.add_an_account,
                            ) {
                                showYouTubeAccountDialog = false
                                navController.navigate(LoginDestination)
                            }
                        }
                    }
                }
            }
        }
    }
    val alertData by viewModel.alertData.collectAsStateWithLifecycle()
    if (alertData != null) {
        val alertState = alertData ?: return
        // AlertDialog
        AlertDialog(
            onDismissRequest = { viewModel.setAlertData(null) },
            title = {
                Text(
                    text = alertState.title,
                    style = typo().titleSmall,
                )
            },
            text = {
                if (alertState.message != null) {
                    Column {
                        Text(text = alertState.message)
                        if (alertState.textField != null) {
                            val verify =
                                alertState.textField.verifyCodeBlock?.invoke(
                                    alertState.textField.value,
                                ) ?: (true to null)
                            TextField(
                                value = alertState.textField.value,
                                onValueChange = {
                                    viewModel.setAlertData(
                                        alertState.copy(
                                            textField =
                                                alertState.textField.copy(
                                                    value = it,
                                                ),
                                        ),
                                    )
                                },
                                isError = !verify.first,
                                label = { Text(text = alertState.textField.label) },
                                supportingText = {
                                    if (!verify.first) {
                                        Text(
                                            modifier = Modifier.fillMaxWidth(),
                                            text = verify.second ?: "",
                                            color = DarkColors.error,
                                        )
                                    }
                                },
                                trailingIcon = {
                                    if (!verify.first) {
                                        Icons.Outlined.Error
                                    }
                                },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            vertical = 6.dp,
                                        ),
                            )
                        }
                    }
                } else if (alertState.selectOne != null) {
                    LazyColumn(
                        Modifier
                            .padding(vertical = 6.dp)
                            .heightIn(0.dp, 500.dp),
                    ) {
                        items(alertState.selectOne.listSelect) { item ->
                            val onSelect = {
                                viewModel.setAlertData(
                                    alertState.copy(
                                        selectOne =
                                            alertState.selectOne.copy(
                                                listSelect =
                                                    alertState.selectOne.listSelect.toMutableList().map {
                                                        if (it == item) {
                                                            true to it.second
                                                        } else {
                                                            false to it.second
                                                        }
                                                    },
                                            ),
                                    ),
                                )
                            }
                            Row(
                                Modifier
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        onSelect.invoke()
                                    }.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = item.first,
                                    onClick = {
                                        onSelect.invoke()
                                    },
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = item.second,
                                    style = typo().bodyMedium,
                                    maxLines = 1,
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .wrapContentHeight(align = Alignment.CenterVertically)
                                            .basicMarquee(
                                                iterations = Int.MAX_VALUE,
                                                animationMode = MarqueeAnimationMode.Immediately,
                                            ).focusable(),
                                )
                            }
                        }
                    }
                } else if (alertState.multipleSelect != null) {
                    LazyColumn(
                        Modifier.padding(vertical = 6.dp),
                    ) {
                        items(alertState.multipleSelect.listSelect) { item ->
                            val onCheck = {
                                viewModel.setAlertData(
                                    alertState.copy(
                                        multipleSelect =
                                            alertState.multipleSelect.copy(
                                                listSelect =
                                                    alertState.multipleSelect.listSelect.toMutableList().map {
                                                        if (it == item) {
                                                            !it.first to it.second
                                                        } else {
                                                            it
                                                        }
                                                    },
                                            ),
                                    ),
                                )
                            }
                            Row(
                                Modifier
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        onCheck.invoke()
                                    }.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(
                                    checked = item.first,
                                    onCheckedChange = {
                                        onCheck.invoke()
                                    },
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(text = item.second, style = typo().bodyMedium, maxLines = 1)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        alertState.confirm.second.invoke(alertState)
                        viewModel.setAlertData(null)
                    },
                    enabled =
                        if (alertState.textField?.verifyCodeBlock != null) {
                            alertState.textField.verifyCodeBlock
                                .invoke(
                                    alertState.textField.value,
                                ).first
                        } else {
                            true
                        },
                ) {
                    Text(text = alertState.confirm.first)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.setAlertData(null)
                    },
                ) {
                    Text(text = alertState.dismiss)
                }
            },
        )
    }

    if (showThirdPartyLibraries) {
        val libraries by produceLibraries {
            TODO("not implemented")
        }
        val lazyListState = rememberLazyListState()
        val canScrollBackward by remember {
            derivedStateOf {
                lazyListState.canScrollBackward
            }
        }
        val sheetState =
            rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
                confirmValueChange = {
                    !canScrollBackward
                },
            )
        val coroutineScope = rememberCoroutineScope()
        ModalBottomSheet(
            modifier =
                Modifier
                    .fillMaxHeight(),
            onDismissRequest = {
                showThirdPartyLibraries = false
            },
            containerColor = Color(0xFF121212),
            dragHandle = {},
            scrimColor = Color(0xFF121212),
            sheetState = sheetState,
            contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
            shape = RectangleShape,
        ) {
            LibrariesContainer(
                libraries?.copy(
                    libraries =
                        libraries
                            ?.libraries
                            ?.distinctBy {
                                it.name
                            }?.toImmutableList() ?: emptyList<Library>().toImmutableList(),
                ),
                Modifier.fillMaxSize(),
                lazyListState = lazyListState,
                showDescription = true,
                contentPadding = innerPadding,
                typography = typo(),
                colors =
                    LibraryDefaults.libraryColors(
                        licenseChipColors =
                            object : ChipColors {
                                override val containerColor: Color
                                    get() = Color.DarkGray
                                override val contentColor: Color
                                    get() = Color.White
                            },
                    ),
                header = {
                    item {
                        TopAppBar(
                            windowInsets = WindowInsets(0, 0, 0, 0),
                            title = {
                                Text(
                                    text =
                                        stringResource(
                                            com.metrolist.music.R.string.third_party_libraries,
                                        ),
                                    style = typo().titleMedium,
                                )
                            },
                            navigationIcon = {
                                Box(Modifier.padding(horizontal = 5.dp)) {
                                    RippleIconButton(
                                        com.metrolist.music.R.drawable.baseline_arrow_back_ios_new_24,
                                        Modifier
                                            .size(32.dp),
                                        true,
                                    ) {
                                        coroutineScope.launch {
                                            sheetState.hide()
                                            showThirdPartyLibraries = false
                                        }
                                    }
                                }
                            },
                        )
                    }
                },
            )
        }
    }

    TopAppBar(
        title = {
            Text(
                text =
                    stringResource(
                        com.metrolist.music.R.string.settings,
                    ),
                style = typo().titleMedium,
            )
        },
        navigationIcon = {
            Box(Modifier.padding(horizontal = 5.dp)) {
                RippleIconButton(
                    com.metrolist.music.R.drawable.baseline_arrow_back_ios_new_24,
                    Modifier
                        .size(32.dp),
                    true,
                ) {
                    navController.navigateUp()
                }
            }
        },
        modifier =
            Modifier
                .hazeChild(hazeState, style = HazeMaterials.ultraThin()) {
                },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
            ),
    )
}