/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

@file:OptIn(ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)

package com.metrolist.music.viewmodels

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.metrolist.innertube.YouTube
import com.metrolist.innertube.models.ArtistItem
import com.metrolist.innertube.models.PlaylistItem
import com.metrolist.innertube.utils.completed
import com.metrolist.music.models.xevrae.LibraryChipType
import com.metrolist.music.constants.AlbumFilter
import com.metrolist.music.constants.AlbumFilterKey
import com.metrolist.music.constants.AlbumSortDescendingKey
import com.metrolist.music.constants.AlbumSortType
import com.metrolist.music.constants.AlbumSortTypeKey
import com.metrolist.music.constants.ArtistFilter
import com.metrolist.music.constants.ArtistFilterKey
import com.metrolist.music.constants.ArtistSongSortDescendingKey
import com.metrolist.music.constants.ArtistSongSortType
import com.metrolist.music.constants.ArtistSongSortTypeKey
import com.metrolist.music.constants.ArtistSortDescendingKey
import com.metrolist.music.constants.ArtistSortType
import com.metrolist.music.constants.ArtistSortTypeKey
import com.metrolist.music.constants.HideExplicitKey
import com.metrolist.music.constants.HideVideoSongsKey
import com.metrolist.music.constants.HideYoutubeShortsKey
import com.metrolist.music.constants.LibraryFilter
import com.metrolist.music.constants.PlaylistSortDescendingKey
import com.metrolist.music.constants.PlaylistSortType
import com.metrolist.music.constants.PlaylistSortTypeKey
import com.metrolist.music.constants.SongFilter
import com.metrolist.music.constants.SongFilterKey
import com.metrolist.music.constants.SongSortDescendingKey
import com.metrolist.music.constants.SongSortType
import com.metrolist.music.constants.SongSortTypeKey
import com.metrolist.music.constants.TopSize
import com.metrolist.music.db.MusicDatabase
import com.metrolist.music.db.entities.Album
import com.metrolist.music.db.entities.AlbumEntity
import com.metrolist.music.db.entities.Artist
import com.metrolist.music.db.entities.ArtistEntity
import com.metrolist.music.db.entities.Playlist
import com.metrolist.music.db.entities.PlaylistEntity
import com.metrolist.music.db.entities.Song
import com.metrolist.music.db.entities.SongEntity
import com.metrolist.music.domain.mediaservice.handler.PlaylistType as MetrolistPlaylistType
import com.metrolist.music.domain.mediaservice.handler.QueueData
import com.metrolist.music.utils.LocalResource
import com.metrolist.music.utils.PodcastRefreshTrigger
import com.metrolist.music.extensions.toEnum
import com.metrolist.music.extensions.normalizeForSearch
import com.metrolist.music.extensions.filterExplicitAlbums
import com.metrolist.music.extensions.filterYoutubeShorts
import com.metrolist.music.extensions.filterExplicit
import com.metrolist.music.extensions.filterVideoSongs
import com.metrolist.music.extensions.matchesNormalizedQuery
import com.metrolist.music.models.toMediaMetadata
import com.metrolist.music.models.xevrae.ChartItem
import com.metrolist.music.models.xevrae.PlaylistType
import com.metrolist.music.models.xevrae.PlaylistsResult
import com.metrolist.music.models.xevrae.RecentlyType
import com.metrolist.music.models.xevrae.Thumbnail
import com.metrolist.music.models.xevrae.XevraePlaylist
import com.metrolist.music.models.xevrae.XevraeRecently
import com.metrolist.music.playback.DownloadUtil
import com.metrolist.music.ui.screens.xevrae.library.LibraryDynamicPlaylistType
import com.metrolist.music.utils.Resource
import com.metrolist.music.utils.SyncUtils
import com.metrolist.music.utils.dataStore
import com.metrolist.music.utils.reportException
import com.metrolist.music.viewmodels.xevrae.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class LibrarySongsViewModel
@Inject
constructor(
    @ApplicationContext context: Context,
    database: MusicDatabase,
    downloadUtil: DownloadUtil,
    private val syncUtils: SyncUtils,
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    val debouncedSearchQuery = _searchQuery
        .debounce(300)
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    val allSongs =
        context.dataStore.data
            .map {
                Triple(
                    Triple(
                        it[SongFilterKey].toEnum(SongFilter.LIKED),
                        it[SongSortTypeKey].toEnum(SongSortType.CREATE_DATE),
                        (it[SongSortDescendingKey] ?: true),
                    ),
                    it[HideExplicitKey] ?: false,
                    it[HideVideoSongsKey] ?: false
                )
            }.distinctUntilChanged()
            .flatMapLatest { (filterSort, hideExplicit, hideVideoSongs) ->
                val (filter, sortType, descending) = filterSort
                when (filter) {
                    SongFilter.LIBRARY -> database.songs(sortType, descending).map { it.filterExplicit(hideExplicit).filterVideoSongs(hideVideoSongs) }
                    SongFilter.LIKED -> database.likedSongs(sortType, descending).map { it.filterExplicit(hideExplicit).filterVideoSongs(hideVideoSongs) }
                    SongFilter.DOWNLOADED -> database.downloadedSongs(sortType, descending).map { it.filterExplicit(hideExplicit).filterVideoSongs(hideVideoSongs) }
                    SongFilter.UPLOADED -> database.uploadedSongs(sortType, descending).map { it.filterExplicit(hideExplicit).filterVideoSongs(hideVideoSongs) }
                }
            }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun syncLikedSongs() {
        viewModelScope.launch(Dispatchers.IO) { syncUtils.syncLikedSongs() }
    }

    fun syncLibrarySongs() {
        viewModelScope.launch(Dispatchers.IO) { syncUtils.syncLibrarySongs() }
    }

    fun syncUploadedSongs() {
        viewModelScope.launch(Dispatchers.IO) { syncUtils.syncUploadedSongs() }
    }
}

@HiltViewModel
class LibraryArtistsViewModel
@Inject
constructor(
    @ApplicationContext context: Context,
    database: MusicDatabase,
    private val syncUtils: SyncUtils,
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    val debouncedSearchQuery = _searchQuery
        .debounce(300)
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    val allArtists =
        context.dataStore.data
            .map {
                Triple(
                    it[ArtistFilterKey].toEnum(ArtistFilter.LIKED),
                    it[ArtistSortTypeKey].toEnum(ArtistSortType.CREATE_DATE),
                    it[ArtistSortDescendingKey] ?: true,
                )
            }.distinctUntilChanged()
            .flatMapLatest { (filter, sortType, descending) ->
                when (filter) {
                    ArtistFilter.LIKED -> database.artistsBookmarked(sortType, descending)
                    ArtistFilter.LIBRARY -> database.artists(sortType, descending)
                }
            }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val filteredArtists =
        combine(allArtists, searchQuery) { artists, query ->
            val normalizedQuery = query.normalizeForSearch()
            artists
                .filter { artist ->
                    matchesNormalizedQuery(normalizedQuery, artist.artist.name)
                }
                .distinctBy { it.id }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun sync() {
        viewModelScope.launch(Dispatchers.IO) { syncUtils.syncArtistsSubscriptions() }
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            allArtists.collect { artists ->
                artists
                    .map { it.artist }
                    .filter {
                        it.thumbnailUrl == null || Duration.between(
                            it.lastUpdateTime,
                            LocalDateTime.now()
                        ) > Duration.ofDays(10)
                    }.take(5)
                    .forEach { artist ->
                        YouTube.artist(artist.id).onSuccess { artistPage ->
                            database.query {
                                update(artist, artistPage)
                            }
                        }
                    }
            }
        }
    }
}

@HiltViewModel
class LibraryAlbumsViewModel
@Inject
constructor(
    @ApplicationContext context: Context,
    database: MusicDatabase,
    private val syncUtils: SyncUtils,
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    val debouncedSearchQuery = _searchQuery
        .debounce(300)
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    val allAlbums =
        context.dataStore.data
            .map {
                Pair(
                    Triple(
                        it[AlbumFilterKey].toEnum(AlbumFilter.LIKED),
                        it[AlbumSortTypeKey].toEnum(AlbumSortType.CREATE_DATE),
                        it[AlbumSortDescendingKey] ?: true,
                    ),
                    it[HideExplicitKey] ?: false
                )
            }.distinctUntilChanged()
            .flatMapLatest { (filterSort, hideExplicit) ->
                val (filter, sortType, descending) = filterSort
                when (filter) {
                    AlbumFilter.LIKED -> database.albumsLiked(sortType, descending).map { it.filterExplicitAlbums(hideExplicit) }
                    AlbumFilter.LIBRARY -> database.albums(sortType, descending).map { it.filterExplicitAlbums(hideExplicit) }
                    AlbumFilter.UPLOADED -> database.albumsUploaded(sortType, descending).map { it.filterExplicitAlbums(hideExplicit) }
                }
            }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun sync() {
        viewModelScope.launch(Dispatchers.IO) { syncUtils.syncLikedAlbums() }
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            allAlbums.collect { albums ->
                albums
                    .filter {
                        it.album.songCount == 0
                    }.take(5)
                    .forEach { album ->
                        YouTube
                            .album(album.id)
                            .onSuccess { albumPage ->
                                database.query {
                                    update(album.album, albumPage, album.artists)
                                }
                            }.onFailure {
                                reportException(it)
                            }
                    }
            }
        }
    }
}

@HiltViewModel
class LibraryPlaylistsViewModel
@Inject
constructor(
    @ApplicationContext context: Context,
    database: MusicDatabase,
    private val syncUtils: SyncUtils,
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    val debouncedSearchQuery = _searchQuery
        .debounce(300)
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    val allPlaylists =
        context.dataStore.data
            .map {
                Triple(
                    it[PlaylistSortTypeKey].toEnum(PlaylistSortType.CREATE_DATE),
                    it[PlaylistSortDescendingKey] ?: true,
                    it[HideYoutubeShortsKey] ?: false
                )
            }.distinctUntilChanged()
            .flatMapLatest { (sortType, descending, hideYoutubeShorts) ->
                database.playlists(sortType, descending).map { it.filterYoutubeShorts(hideYoutubeShorts) }
            }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun sync() {
        viewModelScope.launch(Dispatchers.IO) { syncUtils.syncSavedPlaylists() }
    }

    val topValue =
        context.dataStore.data
            .map { it[TopSize] ?: "50" }
            .distinctUntilChanged()
}

@HiltViewModel
class ArtistSongsViewModel
@Inject
constructor(
    @ApplicationContext context: Context,
    database: MusicDatabase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val artistId = savedStateHandle.get<String>("artistId")!!
    val artist =
        database
            .artist(artistId)
            .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val songs =
        context.dataStore.data
            .map {
                Triple(
                    it[ArtistSongSortTypeKey].toEnum(ArtistSongSortType.CREATE_DATE) to (it[ArtistSongSortDescendingKey]
                        ?: true),
                    it[HideExplicitKey] ?: false,
                    it[HideVideoSongsKey] ?: false
                )
            }.distinctUntilChanged()
            .flatMapLatest { (sortDesc, hideExplicit, hideVideoSongs) ->
                val (sortType, descending) = sortDesc
                database.artistSongs(artistId, sortType, descending).map { it.filterExplicit(hideExplicit).filterVideoSongs(hideVideoSongs) }
            }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}

@HiltViewModel
class LibraryMixViewModel
@Inject
constructor(
    @ApplicationContext context: Context,
    database: MusicDatabase,
    private val syncUtils: SyncUtils,
) : ViewModel() {
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    val debouncedSearchQuery = _searchQuery
        .debounce(300)
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    val syncAllLibrary = {
         viewModelScope.launch(Dispatchers.IO) {
             syncUtils.tryAutoSync()
         }
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            _isRefreshing.value = true
            syncUtils.performFullSyncSuspend()
            _isRefreshing.value = false
        }
    }

    val topValue =
        context.dataStore.data
            .map { it[TopSize] ?: "50" }
            .distinctUntilChanged()
    var artists =
        database
            .artistsBookmarked(
                ArtistSortType.CREATE_DATE,
                true,
            ).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    var albums = context.dataStore.data
        .map { it[HideExplicitKey] ?: false }
        .distinctUntilChanged()
        .flatMapLatest { hideExplicit ->
            database.albumsLiked(AlbumSortType.CREATE_DATE, true).map { it.filterExplicitAlbums(hideExplicit) }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    var songs = context.dataStore.data
        .map { Triple(it[HideExplicitKey] ?: false, it[HideVideoSongsKey] ?: false, it[HideYoutubeShortsKey] ?: false) }
        .distinctUntilChanged()
        .flatMapLatest { (hideExplicit, hideVideoSongs, _) ->
            combine(
                database.songs(SongSortType.CREATE_DATE, true),
                database.songsInBookmarkedPlaylists()
            ) { librarySongs, playlistSongs ->
                (librarySongs + playlistSongs)
                    .distinctBy { it.id }
                    .filterExplicit(hideExplicit)
                    .filterVideoSongs(hideVideoSongs)
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    var playlists = context.dataStore.data
        .map { it[HideYoutubeShortsKey] ?: false }
        .distinctUntilChanged()
        .flatMapLatest { hideYoutubeShorts ->
            database.playlists(PlaylistSortType.CREATE_DATE, true).map { it.filterYoutubeShorts(hideYoutubeShorts) }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            albums.collect { albums ->
                albums
                    .filter {
                        it.album.songCount == 0
                    }.take(5)
                    .forEach { album ->
                        YouTube
                            .album(album.id)
                            .onSuccess { albumPage ->
                                database.query {
                                    update(album.album, albumPage, album.artists)
                                }
                            }.onFailure {
                                reportException(it)
                            }
                    }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            artists.collect { artists ->
                artists
                    .map { it.artist }
                    .filter {
                        it.thumbnailUrl == null ||
                                Duration.between(
                                    it.lastUpdateTime,
                                    LocalDateTime.now(),
                                ) > Duration.ofDays(10)
                    }.take(5)
                    .forEach { artist ->
                        YouTube.artist(artist.id).onSuccess { artistPage ->
                            database.query {
                                update(artist, artistPage)
                            }
                        }
                    }
            }
        }
    }
}

@HiltViewModel
class LibraryPodcastsViewModel
@Inject
constructor(
    @ApplicationContext context: Context,
    private val database: MusicDatabase,
    private val syncUtils: SyncUtils,
) : ViewModel() {
    // Subscribed podcast channels synced from YT Music
    val subscribedChannels = database.subscribedPodcasts()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // SE "Episodes for Later" playlist fetched from YT Music (like AccountScreen)
    private val _sePlaylist = MutableStateFlow<com.metrolist.innertube.models.PlaylistItem?>(null)
    val sePlaylist = _sePlaylist.asStateFlow()

    // RDPN "New Episodes" playlist fetched from YouTube Music (real thumbnail + episode count)
    private val _rdpnPlaylist = MutableStateFlow<com.metrolist.innertube.models.PlaylistItem?>(null)
    val rdpnPlaylist = _rdpnPlaylist.asStateFlow()

    // Podcast host channels fetched from YT Music library/podcast_channels
    private val _apiPodcastChannels = MutableStateFlow<List<ArtistItem>>(emptyList())

    // Podcast channels: API subscriptions + locally bookmarked artists that have podcasts
    // Only shows channels explicitly subscribed to (not derived from saved podcasts)
    val podcastChannels = kotlinx.coroutines.flow.combine(
        _apiPodcastChannels,
        database.bookmarkedPodcastChannels()
    ) { apiChannels, localPodcastChannels ->
        // Convert locally bookmarked podcast channels to ArtistItem format
        val localAsArtistItems = localPodcastChannels.map { artist ->
            ArtistItem(
                id = artist.id,
                title = artist.artist.name,
                thumbnail = artist.artist.thumbnailUrl,
                shuffleEndpoint = null,
                radioEndpoint = null,
            )
        }

        // Combine and deduplicate by ID (prefer API version if exists)
        val apiIds = apiChannels.map { it.id }.toSet()
        val uniqueLocalChannels = localAsArtistItems.filter { it.id !in apiIds }
        apiChannels + uniqueLocalChannels
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Downloaded podcast episodes
    val downloadedEpisodes =
        context.dataStore.data
            .map {
                Pair(
                    it[SongSortTypeKey].toEnum(SongSortType.CREATE_DATE) to (it[SongSortDescendingKey] ?: true),
                    it[HideExplicitKey] ?: false
                )
            }.distinctUntilChanged()
            .flatMapLatest { (sortDesc, hideExplicit) ->
                val (sortType, descending) = sortDesc
                database.downloadedPodcastEpisodes(sortType, descending).map { it.filterExplicit(hideExplicit) }
            }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Saved podcast episodes (in library, not necessarily downloaded)
    val savedEpisodes =
        context.dataStore.data
            .map {
                Pair(
                    it[SongSortTypeKey].toEnum(SongSortType.CREATE_DATE) to (it[SongSortDescendingKey] ?: true),
                    it[HideExplicitKey] ?: false
                )
            }.distinctUntilChanged()
            .flatMapLatest { (sortDesc, hideExplicit) ->
                val (sortType, descending) = sortDesc
                database.savedPodcastEpisodes(sortType, descending).map { it.filterExplicit(hideExplicit) }
            }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private suspend fun fetchSePlaylist() {
        YouTube.library("FEmusic_liked_playlists").completed().onSuccess {
            _sePlaylist.value = it.items
                .filterIsInstance<com.metrolist.innertube.models.PlaylistItem>()
                .find { it.id == "SE" }
        }.onFailure {
            timber.log.Timber.e(it, "[PODCAST] Failed to fetch SE playlist")
        }
    }

    private suspend fun fetchPodcastChannels() {
        YouTube.libraryPodcastChannels().onSuccess { page ->
            val channels = page.items.filterIsInstance<ArtistItem>()
            _apiPodcastChannels.value = channels
            timber.log.Timber.d("[PODCAST] Fetched ${channels.size} podcast channels from YT Music")
        }.onFailure {
            timber.log.Timber.e(it, "[PODCAST] Failed to fetch podcast channels")
        }
    }

    private suspend fun fetchRdpnPlaylist() {
        YouTube.newEpisodesPlaylistInfo().onSuccess { item ->
            _rdpnPlaylist.value = item
            timber.log.Timber.d("[PODCAST] RDPN playlist: ${item.title}, thumbnail: ${item.thumbnail}")
        }.onFailure {
            timber.log.Timber.e(it, "[PODCAST] Failed to fetch RDPN playlist info")
        }
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            fetchSePlaylist()
        }
        viewModelScope.launch(Dispatchers.IO) {
            fetchPodcastChannels()
        }
        viewModelScope.launch(Dispatchers.IO) {
            fetchRdpnPlaylist()
        }
        viewModelScope.launch(Dispatchers.IO) {
            syncUtils.syncPodcastSubscriptionsSuspend()
        }
        // Observe refresh trigger for auto-refresh after subscribe/unsubscribe
        viewModelScope.launch(Dispatchers.IO) {
            PodcastRefreshTrigger.refreshFlow.collect {
                // Small delay to allow YouTube's backend to update
                kotlinx.coroutines.delay(1500)
                fetchPodcastChannels()
            }
        }
    }

    fun clearPodcastData() {
        viewModelScope.launch(Dispatchers.IO) {
            syncUtils.clearPodcastData()
        }
    }

    suspend fun refreshAll() {
        fetchSePlaylist()
        fetchPodcastChannels()
        fetchRdpnPlaylist()
        syncUtils.syncPodcastSubscriptionsSuspend()
        syncUtils.syncEpisodesForLaterSuspend()
    }

    /**
     * Force refresh podcast channels. Called when screen becomes visible.
     */
    fun refreshChannels() {
        viewModelScope.launch(Dispatchers.IO) {
            fetchPodcastChannels()
        }
    }
}

@HiltViewModel
class LibraryViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val database: MusicDatabase,
) : BaseViewModel(context) {
    private val dataStore = context.dataStore
    private val libraryCurrentScreenKey = stringPreferencesKey("library_current_screen")

    private val curScreen = mutableStateOf(LibraryFilter.LIBRARY)
    val filter: MutableState<LibraryFilter> = curScreen

    private val _currentScreen: MutableStateFlow<LibraryChipType> = MutableStateFlow(LibraryChipType.YOUR_LIBRARY)
    val currentScreen: StateFlow<LibraryChipType> get() = _currentScreen.asStateFlow()

    private val _recentlyAdded: MutableStateFlow<LocalResource<List<RecentlyType>>> =
        MutableStateFlow(LocalResource.Loading())
    val recentlyAdded: StateFlow<LocalResource<List<RecentlyType>>> get() = _recentlyAdded.asStateFlow()

    private val _yourLocalPlaylist: MutableStateFlow<LocalResource<List<PlaylistEntity>>> =
        MutableStateFlow(LocalResource.Loading())
    val yourLocalPlaylist: StateFlow<LocalResource<List<PlaylistEntity>>> get() = _yourLocalPlaylist.asStateFlow()

    private val _youTubePlaylist: MutableStateFlow<LocalResource<List<PlaylistsResult>>> =
        MutableStateFlow(LocalResource.Loading())
    val youTubePlaylist: StateFlow<LocalResource<List<PlaylistsResult>>> get() = _youTubePlaylist.asStateFlow()

    private val _youTubeMixForYou: MutableStateFlow<LocalResource<List<PlaylistsResult>>> =
        MutableStateFlow(LocalResource.Loading())
    val youTubeMixForYou: StateFlow<LocalResource<List<PlaylistsResult>>> get() = _youTubeMixForYou.asStateFlow()

    private val _favoritePlaylist: MutableStateFlow<LocalResource<List<PlaylistType>>> =
        MutableStateFlow(LocalResource.Loading())
    val favoritePlaylist: StateFlow<LocalResource<List<PlaylistType>>> get() = _favoritePlaylist.asStateFlow()

    private val _favoritePodcasts: MutableStateFlow<LocalResource<List<PlaylistType>>> =
        MutableStateFlow(LocalResource.Loading())
    val favoritePodcasts: StateFlow<LocalResource<List<PlaylistType>>> get() = _favoritePodcasts.asStateFlow()

    private val _downloadedPlaylist: MutableStateFlow<LocalResource<List<PlaylistType>>> =
        MutableStateFlow(LocalResource.Loading())
    val downloadedPlaylist: StateFlow<LocalResource<List<PlaylistType>>> get() = _downloadedPlaylist.asStateFlow()

    private val _chartPlaylists: MutableStateFlow<LocalResource<List<ChartItem>>> =
        MutableStateFlow(LocalResource.Loading())
    val chartPlaylists: StateFlow<LocalResource<List<ChartItem>>> get() = _chartPlaylists.asStateFlow()

    private val _listCanvasSong: MutableStateFlow<LocalResource<List<SongEntity>>> =
        MutableStateFlow(LocalResource.Loading())
    val listCanvasSong: StateFlow<LocalResource<List<SongEntity>>> get() = _listCanvasSong.asStateFlow()

    private val _accountThumbnail: MutableStateFlow<String?> = MutableStateFlow(null)
    val accountThumbnail: StateFlow<String?> get() = _accountThumbnail.asStateFlow()

    val youtubeLoggedIn = flow {
        emit(YouTube.cookie != null)
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    init {
        viewModelScope.launch {
            dataStore.data.first()[libraryCurrentScreenKey]?.let { chipType ->
                LibraryChipType.fromStringValue(chipType)?.let {
                    _currentScreen.value = it
                }
            }
        }
    }

    fun setCurrentScreen(chipType: LibraryChipType) {
        _currentScreen.value = chipType
        viewModelScope.launch {
            dataStore.edit { it[libraryCurrentScreenKey] = chipType.toStringValue() }
        }
    }

    fun getRecentlyAdded() {
        viewModelScope.launch {
            database.events().collectLatest { events ->
                val songs = events.map { it.song }.distinctBy { it.id }.take(20)
                val temp = songs.map { 
                    XevraeRecently(song = it.song, type = RecentlyType.Type.SONG) 
                }
                _recentlyAdded.value = LocalResource.Success(temp.toImmutableList())
            }
        }
    }

    fun getYouTubePlaylist() {
        _youTubePlaylist.value = LocalResource.Loading()
        viewModelScope.launch {
            YouTube.library().onSuccess { page ->
                val playlists = page.items.filterIsInstance<PlaylistItem>().map {
                    PlaylistsResult(
                        browseId = it.id,
                        title = it.title,
                        thumbnails = it.thumbnails?.map { t -> Thumbnail(t.url) }
                    )
                }
                _youTubePlaylist.value = LocalResource.Success(playlists)
            }.onFailure {
                _youTubePlaylist.value = LocalResource.Error(it.message)
            }
        }
    }

    fun getYouTubeMixedForYou() {
        _youTubeMixForYou.value = LocalResource.Loading()
        viewModelScope.launch {
            YouTube.home().onSuccess { page ->
                val mixedForYou = page.sections.find { it.title?.contains("Mixed for you", ignoreCase = true) == true }
                    ?.items?.filterIsInstance<PlaylistItem>()?.map {
                        PlaylistsResult(
                            browseId = it.id,
                            title = it.title,
                            thumbnails = it.thumbnails?.map { t -> Thumbnail(t.url) }
                        )
                    } ?: emptyList()
                _youTubeMixForYou.value = LocalResource.Success(mixedForYou)
            }.onFailure {
                _youTubeMixForYou.value = LocalResource.Error(it.message)
            }
        }
    }

    fun getPlaylistFavorite() {
        viewModelScope.launch {
            combine(
                database.albumsLikedByNameAsc(),
                database.playlistsByNameAsc()
            ) { albums, playlists ->
                val temp = mutableListOf<PlaylistType>()
                temp.addAll(albums.map { XevraePlaylist(albumEntity = it.album, type = PlaylistType.Type.ALBUM) })
                temp.addAll(playlists.filter { !it.playlist.isEditable }.map { XevraePlaylist(entity = it.playlist, type = PlaylistType.Type.YOUTUBE_PLAYLIST) })
                temp
            }.collectLatest {
                _favoritePlaylist.value = LocalResource.Success(it)
            }
        }
    }

    fun getFavoritePodcasts() {
        viewModelScope.launch {
            database.podcasts().collectLatest { podcasts ->
                // Map podcast entities to XevraePlaylist if applicable
                _favoritePodcasts.value = LocalResource.Success(emptyList())
            }
        }
    }

    fun getCanvasSong() {
        _listCanvasSong.value = LocalResource.Loading()
        viewModelScope.launch {
            database.likedSongs(SongSortType.CREATE_DATE, true).collectLatest { songs ->
                _listCanvasSong.value = LocalResource.Success(songs.take(5).map { it.song })
            }
        }
    }

    fun getLocalPlaylist() {
        _yourLocalPlaylist.value = LocalResource.Loading()
        viewModelScope.launch {
            database.editablePlaylistsByNameAsc().collectLatest { values ->
                _yourLocalPlaylist.value = LocalResource.Success(values.map { it.playlist })
            }
        }
    }

    fun getDownloadedPlaylist() {
        viewModelScope.launch {
            database.playlistsByNameAsc().collectLatest { playlists ->
                val downloaded = playlists.filter { it.id == PlaylistEntity.DOWNLOADED_PLAYLIST_ID }
                    .map { XevraePlaylist(entity = it.playlist, type = PlaylistType.Type.LOCAL) }
                _downloadedPlaylist.value = LocalResource.Success(downloaded)
            }
        }
    }

    fun getChartPlaylists() {
        _chartPlaylists.value = LocalResource.Success(emptyList())
    }

    fun createPlaylist(title: String) {
        viewModelScope.launch {
            val playlist = PlaylistEntity(
                name = title,
                isEditable = true
            )
            database.insert(playlist)
            getLocalPlaylist()
        }
    }

    fun deleteSong(videoId: String) {
        _recentlyAdded.value = LocalResource.Loading()
        viewModelScope.launch {
            database.getSongById(videoId)?.let { song ->
                database.update(song.song.copy(inLibrary = null))
            }
            delay(500)
            getRecentlyAdded()
        }
    }
}

@HiltViewModel
class LibraryDynamicPlaylistViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val database: MusicDatabase,
) : BaseViewModel(context) {
    private val _listFavoriteSong: MutableStateFlow<List<SongEntity>> = MutableStateFlow(emptyList())
    val listFavoriteSong: StateFlow<List<SongEntity>> get() = _listFavoriteSong

    private val _listFollowedArtist: MutableStateFlow<List<ArtistEntity>> = MutableStateFlow(emptyList())
    val listFollowedArtist: StateFlow<List<ArtistEntity>> get() = _listFollowedArtist

    private val _listMostPlayedSong: MutableStateFlow<List<SongEntity>> = MutableStateFlow(emptyList())
    val listMostPlayedSong: StateFlow<List<SongEntity>> get() = _listMostPlayedSong

    private val _listDownloadedSong: MutableStateFlow<List<SongEntity>> = MutableStateFlow(emptyList())
    val listDownloadedSong: StateFlow<List<SongEntity>> get() = _listDownloadedSong

    init {
        getFavoriteSong()
        getFollowedArtist()
        getMostPlayedSong()
        getDownloadedSong()
    }

    private fun getFavoriteSong() {
        viewModelScope.launch {
            database.likedSongsByCreateDateAsc().collectLatest { likedSong ->
                _listFavoriteSong.value = likedSong.map { it.song }.reversed()
            }
        }
    }

    private fun getFollowedArtist() {
        viewModelScope.launch {
            database.artistsBookmarkedByNameAsc().collectLatest { followedArtist ->
                _listFollowedArtist.value = followedArtist.map { it.artist }
            }
        }
    }

    private fun getMostPlayedSong() {
        viewModelScope.launch {
            database.mostPlayedSongs().collectLatest { mostPlayedSong ->
                _listMostPlayedSong.value = mostPlayedSong.map { it.song }
            }
        }
    }

    private fun getDownloadedSong() {
        viewModelScope.launch {
            database.downloadedSongsByCreateDateAsc().collectLatest { downloadedSong ->
                _listDownloadedSong.value = downloadedSong.map { it.song }.reversed()
            }
        }
    }

    fun playSong(
        videoId: String,
        type: LibraryDynamicPlaylistType,
    ) {
        val targetList = getSongList(type)
        if (targetList.isEmpty()) return

        viewModelScope.launch {
            val metadataList = targetList.map { song ->
                database.getSongByIdBlocking(song.id)?.toMediaMetadata()
            }.filterNotNull()

            val startIndex = metadataList.indexOfFirst { it.id == videoId }.coerceAtLeast(0)
            playerConnection.play(metadataList, startIndex)
        }
    }

    private fun getSongList(type: LibraryDynamicPlaylistType): List<SongEntity> =
        when (type) {
            LibraryDynamicPlaylistType.Favorite -> listFavoriteSong.value
            LibraryDynamicPlaylistType.Downloaded -> listDownloadedSong.value
            LibraryDynamicPlaylistType.MostPlayed -> listMostPlayedSong.value
            else -> emptyList()
        }

    fun playAll(type: LibraryDynamicPlaylistType) {
        val targetList = getSongList(type)
        if (targetList.isEmpty()) return
        
        viewModelScope.launch {
            val metadataList = targetList.map { song ->
                database.getSongByIdBlocking(song.id)?.toMediaMetadata()
            }.filterNotNull()
            
            playerConnection.play(metadataList)
        }
    }

    fun shuffle(type: LibraryDynamicPlaylistType) {
        val targetList = getSongList(type)
        if (targetList.isEmpty()) return
        
        viewModelScope.launch {
            val metadataList = targetList.shuffled().map { song ->
                database.getSongByIdBlocking(song.id)?.toMediaMetadata()
            }.filterNotNull()
            
            playerConnection.play(metadataList)
            playerConnection.player.shuffleModeEnabled = true
        }
    }
}
