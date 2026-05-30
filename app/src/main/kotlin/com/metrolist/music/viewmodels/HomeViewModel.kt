/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 * Merged with Xevrae UI
 */

package com.metrolist.music.viewmodels

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.metrolist.innertube.YouTube
import com.metrolist.innertube.models.AlbumItem
import com.metrolist.innertube.models.Artist
import com.metrolist.innertube.models.ArtistItem
import com.metrolist.innertube.models.BrowseEndpoint
import com.metrolist.innertube.models.PlaylistItem
import com.metrolist.innertube.models.PodcastItem
import com.metrolist.innertube.models.SongItem
import com.metrolist.innertube.models.WatchEndpoint
import com.metrolist.innertube.models.YTItem
import com.metrolist.innertube.models.filterExplicit
import com.metrolist.innertube.models.filterVideoSongs
import com.metrolist.innertube.models.filterYoutubeShorts
import com.metrolist.innertube.pages.ChartsPage
import com.metrolist.innertube.pages.ExplorePage
import com.metrolist.innertube.pages.HomePage
import com.metrolist.innertube.utils.completed
import com.metrolist.music.constants.ContentCountryKey
import com.metrolist.music.constants.ContentLanguageKey
import com.metrolist.music.constants.HideExplicitKey
import com.metrolist.music.constants.HideVideoSongsKey
import com.metrolist.music.constants.HideYoutubeShortsKey
import com.metrolist.music.constants.InnerTubeCookieKey
import com.metrolist.music.constants.QuickPicks
import com.metrolist.music.constants.QuickPicksKey
import com.metrolist.music.constants.ShowWrappedCardKey
import com.metrolist.music.constants.WrappedSeenKey
import com.metrolist.music.db.MusicDatabase
import com.metrolist.music.db.entities.Album
import com.metrolist.music.db.entities.LocalItem
import com.metrolist.music.db.entities.Song
import com.metrolist.music.db.entities.SongEntity as MetrolistSongEntity
import com.metrolist.music.db.entities.SpeedDialItem
import com.metrolist.music.extensions.filterVideoSongs
import com.metrolist.music.extensions.toEnum
import com.metrolist.music.models.SimilarRecommendation
import com.metrolist.music.models.xevrae.*
import com.metrolist.music.ui.screens.wrapped.WrappedAudioService
import com.metrolist.music.ui.screens.wrapped.WrappedManager
import com.metrolist.music.utils.SyncUtils
import com.metrolist.music.utils.dataStore
import com.metrolist.music.utils.get
import com.metrolist.music.utils.reportException
import com.metrolist.music.viewmodels.xevrae.ListState
import com.metrolist.music.viewmodels.xevrae.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.random.Random

data class DailyDiscoverItem(
    val seed: Song,
    val recommendation: YTItem,
    val relatedEndpoint: BrowseEndpoint?
)

data class CommunityPlaylistItem(
    val playlist: PlaylistItem,
    val songs: List<SongItem>
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: MusicDatabase,
    val syncUtils: SyncUtils,
    val wrappedManager: WrappedManager,
    private val wrappedAudioService: WrappedAudioService,
    private val savedStateHandle: SavedStateHandle,
) : BaseViewModel(context) {

    // --- Metrolist Original Flows ---
    val isRefreshing = MutableStateFlow(false)
    val isLoading = MutableStateFlow(false)
    val isRandomizing = MutableStateFlow(false)

    private val quickPicksEnum = context.dataStore.data.map {
        it[QuickPicksKey].toEnum(QuickPicks.QUICK_PICKS)
    }.distinctUntilChanged()

    val quickPicks = MutableStateFlow<List<Song>?>(null)
    val dailyDiscover = MutableStateFlow<List<DailyDiscoverItem>?>(null)
    val forgottenFavorites = MutableStateFlow<List<Song>?>(null)
    val keepListening = MutableStateFlow<List<LocalItem>?>(null)
    val similarRecommendations = MutableStateFlow<List<SimilarRecommendation>?>(null)
    val accountPlaylists = MutableStateFlow<List<PlaylistItem>?>(null)
    val homePage = MutableStateFlow<HomePage?>(null)
    val explorePage = MutableStateFlow<ExplorePage?>(null)
    val communityPlaylists = MutableStateFlow<List<CommunityPlaylistItem>?>(null)
    val selectedChip = MutableStateFlow<HomePage.Chip?>(null)
    private val previousHomePage = MutableStateFlow<HomePage?>(null)

    val savedPodcastShows = MutableStateFlow<List<PodcastItem>>(emptyList())
    val episodesForLater = MutableStateFlow<List<SongItem>>(emptyList())

    val allLocalItems = MutableStateFlow<List<LocalItem>>(emptyList())
    val allYtItems = MutableStateFlow<List<YTItem>>(emptyList())

    val pinnedSpeedDialItems: StateFlow<List<SpeedDialItem>> =
        database.speedDialDao.getAll()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val speedDialItems: StateFlow<List<YTItem>> =
        combine(
            database.speedDialDao.getAll(),
            keepListening,
            quickPicks
        ) { pinned, keepListening, quick ->
            val pinnedItems = pinned.map { it.toYTItem() }
            val filled = pinnedItems.toMutableList()
            val targetSize = 27

            if (filled.size < targetSize) {
                keepListening?.let { k ->
                    val needed = targetSize - filled.size
                    val available = k.filter { item ->
                        filled.none { p -> p.id == item.id }
                    }.mapNotNull { item ->
                        when (item) {
                            is Song -> SongItem(
                                id = item.id,
                                title = item.title,
                                artists = item.artists.map { Artist(name = it.name, id = it.id) },
                                thumbnail = item.thumbnailUrl ?: "",
                                explicit = false
                            )
                            is Album -> AlbumItem(
                                browseId = item.id,
                                playlistId = item.album.playlistId ?: "",
                                title = item.title,
                                artists = item.artists.map { Artist(name = it.name, id = it.id) },
                                year = item.album.year,
                                thumbnail = item.thumbnailUrl ?: ""
                            )
                            is com.metrolist.music.db.entities.Artist -> ArtistItem(
                                id = item.id,
                                title = item.title,
                                thumbnail = item.thumbnailUrl,
                                shuffleEndpoint = null,
                                radioEndpoint = null
                            )
                            else -> null
                        }
                    }
                    filled.addAll(available.take(needed))
                }
            }

            if (filled.size < targetSize) {
                quick?.let { q ->
                    val needed = targetSize - filled.size
                    val available = q.filter { song ->
                        filled.none { p -> p.id == song.id }
                    }.map { song ->
                        SongItem(
                            id = song.id,
                            title = song.title,
                            artists = song.artists.map { Artist(name = it.name, id = it.id) },
                            thumbnail = song.thumbnailUrl ?: "",
                            explicit = false
                        )
                    }
                    filled.addAll(available.take(needed))
                }
            }
            filled.take(targetSize)
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val accountName = MutableStateFlow("Guest")
    val accountImageUrl = MutableStateFlow<String?>(null)

    val showWrappedCard: StateFlow<Boolean> = context.dataStore.data.map { prefs ->
        val showWrappedPref = prefs[ShowWrappedCardKey] ?: false
        val seen = prefs[WrappedSeenKey] ?: false
        val isBeforeDate = LocalDate.now().isBefore(LocalDate.of(2026, 2, 1))
        isBeforeDate && (!seen || showWrappedPref)
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    // --- Xevrae UI Specific States ---
    private val _homeItemList: MutableStateFlow<List<HomeItem>> = MutableStateFlow(emptyList())
    val homeItemList: StateFlow<List<HomeItem>> = _homeItemList

    private var _homeListState = MutableStateFlow<ListState>(ListState.IDLE)
    val homeListState: StateFlow<ListState> = _homeListState

    private var _continuation = MutableStateFlow<String?>(null)
    val continuation: StateFlow<String?> = _continuation

    private val _exploreMoodItem: MutableStateFlow<Mood?> = MutableStateFlow(null)
    val exploreMoodItem: StateFlow<Mood?> = _exploreMoodItem

    private val _accountInfo: MutableStateFlow<Pair<String?, String?>?> = MutableStateFlow(null)
    val accountInfo: StateFlow<Pair<String?, String?>?> = _accountInfo

    private val _chart: MutableStateFlow<Chart?> = MutableStateFlow(null)
    val chart: StateFlow<Chart?> = _chart

    private val _newRelease: MutableStateFlow<List<HomeItem>> = MutableStateFlow(emptyList())
    val newRelease: StateFlow<List<HomeItem>> = _newRelease

    val regionCodeChart: MutableStateFlow<String?> = MutableStateFlow(null)
    val loading = MutableStateFlow<Boolean>(true)
    val loadingChart = MutableStateFlow<Boolean>(true)

    private val _showLogInAlert: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val showLogInAlert: StateFlow<Boolean> = _showLogInAlert

    private val _params: MutableStateFlow<String?> = MutableStateFlow(null)
    val params: StateFlow<String?> = _params

    private val _mainHomeThumbnail: MutableStateFlow<String?> = MutableStateFlow(null)
    val mainHomeThumbnail: StateFlow<String?> = _mainHomeThumbnail

    init {
        viewModelScope.launch {
            val cookie = context.dataStore.data.first()[InnerTubeCookieKey] ?: ""
            val shouldShowAlert = context.dataStore.data.first()[ShouldShowLogInAlertKey] ?: true
            if (cookie.isEmpty() && shouldShowAlert) {
                _showLogInAlert.value = true
            }

            regionCodeChart.value = context.dataStore.data.first()[ChartKey]
            
            // Sync with YouTube home changes
            launch {
                homePage.collect { page ->
                    if (page != null) {
                        _continuation.value = page.continuation
                        _homeItemList.value = page.sections.map { it.toHomeItem() }
                    }
                }
            }

            // Sync with account info
            launch {
                combine(accountName, accountImageUrl) { name, thumb -> name to thumb }.collect {
                    _accountInfo.value = it
                }
            }

            // Sync main thumbnail
            launch {
                homeItemList.collect { list ->
                    _mainHomeThumbnail.value = list.firstOrNull()?.contents?.firstOrNull()?.thumbnails?.lastOrNull()?.url
                }
            }

            // Sync region/language changes to reload home
            launch {
                combine(
                    context.dataStore.data.map { it[ContentCountryKey] ?: "" }.distinctUntilChanged(),
                    context.dataStore.data.map { it[ContentLanguageKey] ?: "" }.distinctUntilChanged(),
                    context.dataStore.data.map { it[InnerTubeCookieKey] ?: "" }.distinctUntilChanged()
                ) { _, _, _ -> Unit }.collect {
                    load()
                }
            }

            // Listen for wrapped data
            launch {
                showWrappedCard.collect { shouldShow ->
                    if (shouldShow && !wrappedManager.state.value.isDataReady) {
                        try {
                            wrappedManager.prepare()
                        } catch (e: Exception) {
                            reportException(e)
                        }
                    }
                }
            }
        }

        // Initialize sync
        viewModelScope.launch(Dispatchers.IO) {
            syncUtils.tryAutoSync()
        }
    }

    suspend fun load() {
        isLoading.update { true }
        loading.update { true }
        
        val hideExplicit = context.dataStore.get(HideExplicitKey, false)
        val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false)
        val hideYoutubeShorts = context.dataStore.get(HideYoutubeShortsKey, false)
        val fromTimeStamp = System.currentTimeMillis() - 86400000L * 7 * 2

        coroutineScope {
            launch(Dispatchers.IO) { getQuickPicks() }
            launch(Dispatchers.IO) {
                forgottenFavorites.value = database.forgottenFavorites().first()
                    .filterVideoSongs(hideVideoSongs).shuffled().take(20)
            }
            launch(Dispatchers.IO) {
                val songs = database.mostPlayedSongs(fromTimeStamp, limit = 15, offset = 5).first()
                    .filterVideoSongs(hideVideoSongs).shuffled().take(10)
                val albums = database.mostPlayedAlbums(fromTimeStamp, limit = 8, offset = 2).first()
                    .filter { it.album.thumbnailUrl != null }.shuffled().take(5)
                val artists = database.mostPlayedArtists(fromTimeStamp).first()
                    .filter { it.artist.isYouTubeArtist && it.artist.thumbnailUrl != null }.shuffled().take(5)
                keepListening.value = (songs + albums + artists).shuffled()
            }

            launch(Dispatchers.IO) {
                YouTube.home(params = params.value).onSuccess { page ->
                    homePage.value = page.copy(
                        sections = page.sections.mapNotNull { section ->
                            val filtered = section.items
                                .filterExplicit(hideExplicit)
                                .filterVideoSongs(hideVideoSongs)
                                .filterYoutubeShorts(hideYoutubeShorts)
                            if (filtered.isEmpty()) null else section.copy(items = filtered)
                        }
                    )
                }.onFailure { reportException(it) }
            }

            launch(Dispatchers.IO) {
                YouTube.getChartsPage().onSuccess { page ->
                    _chart.value = page.toChart()
                }
            }

            launch(Dispatchers.IO) {
                YouTube.explore().onSuccess { page ->
                    explorePage.value = page.copy(
                        newReleaseAlbums = page.newReleaseAlbums.filterExplicit(hideExplicit)
                    )
                    _newRelease.value = listOf(HomeItem(
                        title = getString(com.metrolist.music.R.string.new_release),
                        contents = page.newReleaseAlbums.map { it.toContent() }
                    ))
                    _exploreMoodItem.value = Mood(
                        genres = page.moodAndGenres.map { Genre(it.endpoint.params ?: "", it.title) },
                        moodsMoments = emptyList()
                    )
                }
            }

            if (YouTube.cookie != null) {
                launch(Dispatchers.IO) { loadAccountPlaylists() }
                launch(Dispatchers.IO) {
                    YouTube.accountInfo().onSuccess { info ->
                        accountName.value = info.name
                        accountImageUrl.value = info.thumbnailUrl
                    }
                }
            }
        }

        allLocalItems.value = (quickPicks.value.orEmpty() + forgottenFavorites.value.orEmpty() + keepListening.value.orEmpty())
            .filter { it is Song || it is Album }
        
        isLoading.value = false
        loading.value = false
        loadingChart.value = false
        
        // Background heavy tasks
        viewModelScope.launch(Dispatchers.IO) { getDailyDiscover() }
        viewModelScope.launch(Dispatchers.IO) { getCommunityPlaylists() }
        viewModelScope.launch(Dispatchers.IO) {
            val fromTime = System.currentTimeMillis() - 86400000L * 7 * 2
            val artistRecommendations = database.mostPlayedArtists(fromTime, limit = 15).first()
                .filter { it.artist.isYouTubeArtist }
                .shuffled().take(4)
                .mapNotNull {
                    val items = mutableListOf<YTItem>()
                    YouTube.artist(it.id).onSuccess { page ->
                        page.sections.takeLast(3).forEach { section -> items += section.items }
                    }
                    SimilarRecommendation(
                        title = it,
                        items = items
                            .distinctBy { item -> item.id }
                            .filterExplicit(hideExplicit)
                            .filterVideoSongs(hideVideoSongs)
                            .shuffled().take(12)
                            .ifEmpty { return@mapNotNull null }
                    )
                }
            similarRecommendations.value = artistRecommendations.shuffled()
            allYtItems.value = similarRecommendations.value?.flatMap { it.items }.orEmpty() +
                    homePage.value?.sections?.flatMap { it.items }.orEmpty()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            isRefreshing.value = true
            load()
            isRefreshing.value = false
            syncUtils.tryAutoSync()
        }
    }

    fun loadHomeData() {
        viewModelScope.launch {
            load()
        }
    }

    fun getContinueHomeItem(continuation: String?) {
        if (continuation == null || _homeListState.value == ListState.PAGINATING) return
        
        viewModelScope.launch(Dispatchers.IO) {
            _homeListState.value = ListState.PAGINATING
            val hideExplicit = context.dataStore.get(HideExplicitKey, false)
            val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false)
            val hideYoutubeShorts = context.dataStore.get(HideYoutubeShortsKey, false)

            YouTube.home(continuation = continuation).onSuccess { page ->
                _continuation.value = page.continuation
                val nextSections = page.sections.mapNotNull { section ->
                    val filtered = section.items.filterExplicit(hideExplicit).filterVideoSongs(hideVideoSongs).filterYoutubeShorts(hideYoutubeShorts)
                    if (filtered.isEmpty()) null else section.copy(items = filtered)
                }
                homePage.update { current ->
                    current?.copy(
                        sections = current.sections + nextSections,
                        continuation = page.continuation
                    )
                }
                _homeListState.value = if (page.continuation == null) ListState.PAGINATION_EXHAUST else ListState.IDLE
            }.onFailure {
                _homeListState.value = ListState.ERROR
            }
        }
    }

    fun setParams(params: String?) {
        _params.value = params
        viewModelScope.launch {
            load()
        }
    }

    fun exploreChart(region: String) {
        viewModelScope.launch {
            loadingChart.value = true
            context.dataStore.edit { it[ChartKey] = region }
            regionCodeChart.value = region
            YouTube.getChartsPage().onSuccess { page ->
                _chart.value = page.toChart()
            }
            loadingChart.value = false
        }
    }

    fun doneShowLogInAlert(neverShowAgain: Boolean = false) {
        viewModelScope.launch {
            _showLogInAlert.value = false
            if (neverShowAgain) {
                context.dataStore.edit { it[ShouldShowLogInAlertKey] = false }
            }
        }
    }

    // --- Private Helper Methods ---

    private suspend fun getQuickPicks() {
        val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false)
        val relatedSongs = database.quickPicks().first().filterVideoSongs(hideVideoSongs)
        quickPicks.value = relatedSongs.shuffled().take(20)
    }

    private suspend fun getDailyDiscover() {
        val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false)
        val likedSongs = database.likedSongsByCreateDateAsc().first()
        if (likedSongs.isEmpty()) return
        val seeds = likedSongs.shuffled().take(5)
        val items = mutableListOf<DailyDiscoverItem>()
        coroutineScope {
            seeds.forEach { seed ->
                launch {
                    val endpoint = YouTube.next(WatchEndpoint(videoId = seed.id)).getOrNull()?.relatedEndpoint
                    if (endpoint != null) {
                        YouTube.related(endpoint).onSuccess { page ->
                            val recommendation = page.songs.filter { !it.explicit && (!hideVideoSongs || !it.isVideoSong) }.shuffled().firstOrNull()
                            if (recommendation != null) items.add(DailyDiscoverItem(seed, recommendation, endpoint))
                        }
                    }
                }
            }
        }
        dailyDiscover.value = items.shuffled()
    }

    private suspend fun getCommunityPlaylists() {
        YouTube.home().onSuccess { page ->
            val playlists = page.sections.flatMap { it.items }.filterIsInstance<PlaylistItem>().shuffled().take(5)
            val communityItems = mutableListOf<CommunityPlaylistItem>()
            coroutineScope {
                playlists.forEach { playlist ->
                    launch {
                        YouTube.playlist(playlist.id).onSuccess { p ->
                            communityItems.add(CommunityPlaylistItem(playlist, p.songs.take(10)))
                        }
                    }
                }
            }
            communityPlaylists.value = communityItems
        }
    }

    private suspend fun loadAccountPlaylists() {
        YouTube.library("FEmusic_liked_playlists").completed().onSuccess {
            accountPlaylists.value = it.items.filterIsInstance<PlaylistItem>().filterNot { p -> p.id == "SE" }
        }
    }

    // --- Mapping Helpers ---

    private fun HomePage.Section.toHomeItem() = HomeItem(
        title = title,
        subtitle = label,
        thumbnail = thumbnail?.let { listOf(Thumbnail(it)) },
        contents = items.mapNotNull { it.toContent() }
    )

    private fun YTItem.toContent() = when (this) {
        is SongItem -> Content(
            title = title,
            artists = artists.map { Artist(it.name, it.id ?: "") },
            album = album?.let { Album(it.name, it.id) },
            thumbnails = listOf(Thumbnail(thumbnail)),
            videoId = id,
            isExplicit = explicit,
            durationSeconds = duration,
            radio = endpoint?.watchPlaylistEndpoint?.params
        )
        is AlbumItem -> Content(
            title = title,
            artists = artists?.map { Artist(it.name, it.id ?: "") },
            thumbnails = listOf(Thumbnail(thumbnail)),
            browseId = browseId,
            playlistId = playlistId,
            isExplicit = explicit
        )
        is ArtistItem -> Content(
            title = title,
            thumbnails = thumbnail?.let { listOf(Thumbnail(it)) } ?: emptyList(),
            browseId = id
        )
        is PlaylistItem -> Content(
            title = title,
            artists = author?.let { listOf(Artist(it.name, it.id ?: "")) },
            thumbnails = thumbnail?.let { listOf(Thumbnail(it)) } ?: emptyList(),
            playlistId = id
        )
        else -> null
    }

    private fun ChartsPage.toChart() = Chart(
        artists = sections.find { it.chartType == ChartsPage.ChartType.TOP }?.items?.filterIsInstance<ArtistItem>()?.map {
            ArtistItemCompat(it.title, it.id, it.thumbnail?.let { t -> listOf(Thumbnail(t)) } ?: emptyList())
        } ?: emptyList(),
        listChartItem = sections.map { section ->
            ChartItemPlaylist(
                title = section.title,
                playlists = section.items.mapNotNull { item ->
                    when (item) {
                        is PlaylistItem -> PlaylistCompat(item.title, item.id, item.thumbnail?.let { listOf(Thumbnail(it)) } ?: emptyList())
                        is AlbumItem -> PlaylistCompat(item.title, item.playlistId, listOf(Thumbnail(item.thumbnail)))
                        else -> null
                    }
                }
            )
        }
    )

    companion object {
        val ShouldShowLogInAlertKey = booleanPreferencesKey("shouldShowLogInRequiredAlert")
        val ChartKey = stringPreferencesKey("chartKey")
        
        // Home params from Xevrae
        const val HOME_PARAMS_RELAX = "ggM8SgQIBxADSgQIBRABSgQICRABSgQIChABSgQIDRABSgQICBABSgQIBBABSgQIDhABSgQIAxABSgQIBhAB"
        const val HOME_PARAMS_SLEEP = "ggM8SgQIBxABSgQIBRADSgQICRABSgQIChABSgQIDRABSgQICBABSgQIBBABSgQIDhABSgQIAxABSgQIBhAB"
        const val HOME_PARAMS_ENERGIZE = "ggM8SgQIBxABSgQIBRABSgQICRADSgQIChABSgQIDRABSgQICBABSgQIBBABSgQIDhABSgIAxABSgQIBhAB"
        const val HOME_PARAMS_SAD = "ggM8SgQIBxABSgQIBRABSgQICRABSgQIChADSgQIDRABSgQICBABSgQIBBABSgQIDhABSgQIAxABSgQIBhAB"
        const val HOME_PARAMS_ROMANCE = "ggM8SgQIBxABSgQIBRABSgQICRABSgQIChABSgQIDRADSgQICBABSgQIBBABSgQIDhABSgQIAxABSgQIBhAB"
        const val HOME_PARAMS_FEEL_GOOD = "ggM8SgQIBxABSgQIBRABSgQICRABSgQIChABSgQIDRABSgQICBADSgQIBBABSgQIDhABSgQIAxABSgQIBhAB"
        const val HOME_PARAMS_WORKOUT = "ggM8SgQIBxABSgQIBRABSgQICRABSgQIChABSgQIDRABSgQICBABSgQIBBADSgQIDhABSgQIAxABSgQIBhAB"
        const val HOME_PARAMS_PARTY = "ggM8SgQIBxABSgQIBRABSgQICRABSgQIChABSgQIDRABSgQICBABSgQIBBABSgQIDhADSgQIAxABSgQIBhAB"
        const val HOME_PARAMS_COMMUTE = "ggM8SgQIBxABSgQIBRABSgQICRABSgQIChABSgQIDRABSgQICBABSgQIBBABSgQIDhABSgQIAxADSgQIBhAB"
        const val HOME_PARAMS_FOCUS = "ggM8SgQIBxABSgQIBRABSgQICRABSgQIChABSgQIDRABSgQICBABSgQIBBABSgQIDhABSgQIAxABSgQIBhAD"
    }
}
