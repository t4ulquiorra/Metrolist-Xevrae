package com.metrolist.music.viewmodels.xevrae

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.metrolist.innertube.YouTube
import com.metrolist.innertube.models.AlbumItem
import com.metrolist.innertube.models.ArtistItem
import com.metrolist.innertube.models.PlaylistItem
import com.metrolist.innertube.models.PodcastItem
import com.metrolist.innertube.models.SongItem
import com.metrolist.innertube.models.YTItem
import com.metrolist.innertube.models.EpisodeItem
import com.metrolist.innertube.models.filterExplicit
import com.metrolist.innertube.models.filterVideoSongs
import com.metrolist.innertube.models.filterYoutubeShorts
import com.metrolist.music.constants.HideExplicitKey
import com.metrolist.music.constants.HideVideoSongsKey
import com.metrolist.music.constants.HideYoutubeShortsKey
import com.metrolist.music.db.MusicDatabase
import com.metrolist.music.db.entities.SearchHistory
import com.metrolist.music.models.xevrae.AlbumsResult
import com.metrolist.music.models.xevrae.ArtistsResult
import com.metrolist.music.models.xevrae.PlaylistsResult
import com.metrolist.music.models.xevrae.SearchResultType
import com.metrolist.music.models.xevrae.SongsResult
import com.metrolist.music.models.xevrae.VideosResult
import com.metrolist.music.models.xevrae.Thumbnail
import com.metrolist.music.models.xevrae.Artist
import com.metrolist.music.models.xevrae.Album
import com.metrolist.music.utils.dataStore
import com.metrolist.music.utils.get
import com.metrolist.music.utils.reportException
import com.metrolist.music.viewmodels.xevrae.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URLDecoder
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    @ApplicationContext context: android.content.Context,
    private val database: MusicDatabase,
    private val savedStateHandle: SavedStateHandle,
) : BaseViewModel(context) {

    private val _searchScreenState = MutableStateFlow(SearchScreenState())
    val searchScreenState: StateFlow<SearchScreenState> = _searchScreenState.asStateFlow()

    private val _searchScreenUIState = MutableStateFlow<SearchScreenUIState>(SearchScreenUIState.Empty)
    val searchScreenUIState: StateFlow<SearchScreenUIState> = _searchScreenUIState.asStateFlow()

    private val continuations = mutableMapOf<SearchType, String?>()

    init {
        val initialQuery = savedStateHandle.get<String>("query")
        if (initialQuery != null) {
            val decodedQuery = try {
                URLDecoder.decode(initialQuery, "UTF-8")
            } catch (e: Exception) {
                initialQuery
            }
            searchAll(decodedQuery)
        }
    }

    fun searchAll(query: String) {
        if (query.isBlank()) return
        
        _searchScreenUIState.value = SearchScreenUIState.Loading
        _searchScreenState.update { it.copy(searchType = SearchType.ALL) }
        
        viewModelScope.launch {
            YouTube.searchSummary(query)
                .onSuccess { page ->
                    val hideExplicit = context.dataStore.get(HideExplicitKey, false)
                    val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false)
                    val hideYoutubeShorts = context.dataStore.get(HideYoutubeShortsKey, false)
                    
                    val filteredPage = page.filterExplicit(hideExplicit)
                        .filterVideoSongs(hideVideoSongs)
                        .filterYoutubeShorts(hideYoutubeShorts)
                    
                    val allItems = mutableListOf<SearchResultType>()
                    var songs = emptyList<SongsResult>()
                    var videos = emptyList<VideosResult>()
                    var albums = emptyList<AlbumsResult>()
                    var artists = emptyList<ArtistsResult>()
                    var playlists = emptyList<PlaylistsResult>()
                    var featuredPlaylists = emptyList<PlaylistsResult>()
                    var podcasts = emptyList<PlaylistsResult>()

                    filteredPage.summaries.forEach { summary ->
                        when (summary.title) {
                            "Songs" -> {
                                songs = summary.items.mapNotNull { it as? SongItem }.map { it.toSongsResult() }
                                allItems.addAll(songs)
                            }
                            "Videos" -> {
                                videos = summary.items.mapNotNull { it as? SongItem }.map { it.toVideosResult() }
                                allItems.addAll(videos)
                            }
                            "Albums" -> {
                                albums = summary.items.mapNotNull { it as? AlbumItem }.map { it.toAlbumsResult() }
                                allItems.addAll(albums)
                            }
                            "Artists" -> {
                                artists = summary.items.mapNotNull { it as? ArtistItem }.map { it.toArtistsResult() }
                                allItems.addAll(artists)
                            }
                            "Playlists", "Community playlists" -> {
                                playlists = summary.items.mapNotNull { it as? PlaylistItem }.map { it.toPlaylistsResult() }
                                allItems.addAll(playlists)
                            }
                            "Featured playlists" -> {
                                featuredPlaylists = summary.items.mapNotNull { it as? PlaylistItem }.map { it.toPlaylistsResult() }
                                allItems.addAll(featuredPlaylists)
                            }
                            "Podcasts" -> {
                                podcasts = summary.items.mapNotNull { it as? PodcastItem }.map { it.asPlaylistItem().toPlaylistsResult() }
                                allItems.addAll(podcasts)
                            }
                        }
                    }

                    _searchScreenState.update { 
                        it.copy(
                            searchAllResult = allItems,
                            searchSongsResult = songs,
                            searchVideosResult = videos,
                            searchAlbumsResult = albums,
                            searchArtistsResult = artists,
                            searchPlaylistsResult = playlists,
                            searchFeaturedPlaylistsResult = featuredPlaylists,
                            searchPodcastsResult = podcasts
                        )
                    }
                    _searchScreenUIState.value = SearchScreenUIState.Success
                    insertSearchHistory(query)
                }
                .onFailure {
                    _searchScreenUIState.value = SearchScreenUIState.Error
                    reportException(it)
                }
        }
    }

    fun searchSongs(query: String) = searchWithType(query, SearchType.SONGS, YouTube.SearchFilter.FILTER_SONG)
    fun searchVideos(query: String) = searchWithType(query, SearchType.VIDEOS, YouTube.SearchFilter.FILTER_VIDEO)
    fun searchAlbums(query: String) = searchWithType(query, SearchType.ALBUMS, YouTube.SearchFilter.FILTER_ALBUM)
    fun searchArtists(query: String) = searchWithType(query, SearchType.ARTISTS, YouTube.SearchFilter.FILTER_ARTIST)
    fun searchPlaylists(query: String) = searchWithType(query, SearchType.PLAYLISTS, YouTube.SearchFilter.FILTER_COMMUNITY_PLAYLIST)
    fun searchFeaturedPlaylist(query: String) = searchWithType(query, SearchType.FEATURED_PLAYLISTS, YouTube.SearchFilter.FILTER_FEATURED_PLAYLIST)
    fun searchPodcast(query: String) = searchWithType(query, SearchType.PODCASTS, YouTube.SearchFilter.FILTER_PODCAST)

    private fun searchWithType(query: String, type: SearchType, filter: YouTube.SearchFilter) {
        if (query.isBlank()) return
        
        _searchScreenUIState.value = SearchScreenUIState.Loading
        _searchScreenState.update { it.copy(searchType = type) }
        
        viewModelScope.launch {
            YouTube.search(query, filter)
                .onSuccess { result ->
                    val hideExplicit = context.dataStore.get(HideExplicitKey, false)
                    val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false)
                    val hideYoutubeShorts = context.dataStore.get(HideYoutubeShortsKey, false)
                    
                    val filteredItems = result.items
                        .filterExplicit(hideExplicit)
                        .filterVideoSongs(hideVideoSongs)
                        .filterYoutubeShorts(hideYoutubeShorts)
                    
                    continuations[type] = result.continuation
                    
                    _searchScreenState.update { state ->
                        when (type) {
                            SearchType.SONGS -> state.copy(searchSongsResult = filteredItems.mapNotNull { it as? SongItem }.map { it.toSongsResult() })
                            SearchType.VIDEOS -> state.copy(searchVideosResult = filteredItems.mapNotNull { it as? SongItem }.map { it.toVideosResult() })
                            SearchType.ALBUMS -> state.copy(searchAlbumsResult = filteredItems.mapNotNull { it as? AlbumItem }.map { it.toAlbumsResult() })
                            SearchType.ARTISTS -> state.copy(searchArtistsResult = filteredItems.mapNotNull { it as? ArtistItem }.map { it.toArtistsResult() })
                            SearchType.PLAYLISTS -> state.copy(searchPlaylistsResult = filteredItems.mapNotNull { it as? PlaylistItem }.map { it.toPlaylistsResult() })
                            SearchType.FEATURED_PLAYLISTS -> state.copy(searchFeaturedPlaylistsResult = filteredItems.mapNotNull { it as? PlaylistItem }.map { it.toPlaylistsResult() })
                            SearchType.PODCASTS -> state.copy(searchPodcastsResult = filteredItems.mapNotNull { it as? PodcastItem }.map { it.asPlaylistItem().toPlaylistsResult() })
                            else -> state
                        }
                    }
                    _searchScreenUIState.value = SearchScreenUIState.Success
                }
                .onFailure {
                    _searchScreenUIState.value = SearchScreenUIState.Error
                    reportException(it)
                }
        }
    }

    fun loadMore(query: String) {
        val type = _searchScreenState.value.searchType
        val continuation = continuations[type] ?: return
        
        viewModelScope.launch {
            YouTube.searchContinuation(continuation)
                .onSuccess { result ->
                    val hideExplicit = context.dataStore.get(HideExplicitKey, false)
                    val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false)
                    val hideYoutubeShorts = context.dataStore.get(HideYoutubeShortsKey, false)
                    
                    val filteredItems = result.items
                        .filterExplicit(hideExplicit)
                        .filterVideoSongs(hideVideoSongs)
                        .filterYoutubeShorts(hideYoutubeShorts)
                    
                    continuations[type] = result.continuation
                    
                    _searchScreenState.update { state ->
                        when (type) {
                            SearchType.SONGS -> state.copy(searchSongsResult = state.searchSongsResult + filteredItems.mapNotNull { it as? SongItem }.map { it.toSongsResult() })
                            SearchType.VIDEOS -> state.copy(searchVideosResult = state.searchVideosResult + filteredItems.mapNotNull { it as? SongItem }.map { it.toVideosResult() })
                            SearchType.ALBUMS -> state.copy(searchAlbumsResult = state.searchAlbumsResult + filteredItems.mapNotNull { it as? AlbumItem }.map { it.toAlbumsResult() })
                            SearchType.ARTISTS -> state.copy(searchArtistsResult = state.searchArtistsResult + filteredItems.mapNotNull { it as? ArtistItem }.map { it.toArtistsResult() })
                            SearchType.PLAYLISTS -> state.copy(searchPlaylistsResult = state.searchPlaylistsResult + filteredItems.mapNotNull { it as? PlaylistItem }.map { it.toPlaylistsResult() })
                            SearchType.FEATURED_PLAYLISTS -> state.copy(searchFeaturedPlaylistsResult = state.searchFeaturedPlaylistsResult + filteredItems.mapNotNull { it as? PlaylistItem }.map { it.toPlaylistsResult() })
                            SearchType.PODCASTS -> state.copy(searchPodcastsResult = state.searchPodcastsResult + filteredItems.mapNotNull { it as? PodcastItem }.map { it.asPlaylistItem().toPlaylistsResult() })
                            else -> state
                        }
                    }
                }
                .onFailure {
                    reportException(it)
                }
        }
    }

    fun setSearchType(type: SearchType) {
        _searchScreenState.update { it.copy(searchType = type) }
        val query = savedStateHandle.get<String>("query") ?: return
        when (type) {
            SearchType.ALL -> searchAll(query)
            SearchType.SONGS -> searchSongs(query)
            SearchType.VIDEOS -> searchVideos(query)
            SearchType.ALBUMS -> searchAlbums(query)
            SearchType.ARTISTS -> searchArtists(query)
            SearchType.PLAYLISTS -> searchPlaylists(query)
            SearchType.FEATURED_PLAYLISTS -> searchFeaturedPlaylist(query)
            SearchType.PODCASTS -> searchPodcast(query)
        }
    }

    fun suggestQuery(query: String) {
        if (query.isBlank()) {
            _searchScreenState.update { it.copy(suggestQueries = emptyList(), suggestYTItems = emptyList()) }
            return
        }
        viewModelScope.launch {
            YouTube.searchSuggestions(query)
                .onSuccess { suggestions ->
                    _searchScreenState.update { it.copy(
                        suggestQueries = suggestions.queries,
                        suggestYTItems = suggestions.recommendedItems.mapNotNull { item ->
                            when (item) {
                                is SongItem -> item.toSongsResult()
                                is AlbumItem -> item.toAlbumsResult()
                                is ArtistItem -> item.toArtistsResult()
                                is PlaylistItem -> item.toPlaylistsResult()
                                else -> null
                            }
                        }
                    ) }
                }
        }
    }

    fun deleteSearchHistory() {
        viewModelScope.launch {
            database.clearSearchHistory()
        }
    }

    fun insertSearchHistory(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            database.insert(SearchHistory(query = query))
        }
    }

    private fun SongItem.toSongsResult() = SongsResult(
        album = this.album?.let { Album(it.name, it.id) },
        artists = this.artists.map { Artist(it.name, it.id ?: "") },
        duration = this.duration?.let { formatDuration(it) },
        durationSeconds = this.duration,
        isExplicit = this.explicit,
        thumbnails = listOf(Thumbnail(this.thumbnail ?: "")),
        title = this.title,
        videoId = this.id,
        videoType = if (this.isVideoSong) "VIDEO" else "MUSIC",
        category = null,
        feedbackTokens = null,
        resultType = "SONG"
    )

    private fun SongItem.toVideosResult() = VideosResult(
        artists = this.artists.map { Artist(it.name, it.id ?: "") },
        duration = this.duration?.let { formatDuration(it) },
        durationSeconds = this.duration,
        thumbnails = listOf(Thumbnail(this.thumbnail ?: "")),
        title = this.title,
        videoId = this.id,
        videoType = if (this.isVideoSong) "VIDEO" else "MUSIC",
        category = null,
        resultType = "VIDEO"
    )

    private fun AlbumItem.toAlbumsResult() = AlbumsResult(
        browseId = this.browseId ?: "",
        thumbnails = listOf(Thumbnail(this.thumbnail ?: "")),
        title = this.title,
        year = this.year?.toString(),
        playlistId = this.playlistId
    )

    private fun ArtistItem.toArtistsResult() = ArtistsResult(
        artist = this.title,
        browseId = this.id,
        thumbnails = listOf(Thumbnail(this.thumbnail ?: "")),
        title = this.title
    )

    private fun PlaylistItem.toPlaylistsResult() = PlaylistsResult(
        browseId = this.id,
        thumbnails = listOf(Thumbnail(this.thumbnail ?: "")),
        title = this.title,
        author = this.author?.name,
        trackCount = null
    )

    private fun formatDuration(seconds: Int): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) {
            "${h}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
        } else {
            "${m}:${s.toString().padStart(2, '0')}"
        }
    }
}

data class SearchScreenState(
    val searchType: SearchType = SearchType.ALL,
    val searchAllResult: List<SearchResultType> = emptyList(),
    val searchSongsResult: List<SongsResult> = emptyList(),
    val searchVideosResult: List<VideosResult> = emptyList(),
    val searchAlbumsResult: List<AlbumsResult> = emptyList(),
    val searchArtistsResult: List<ArtistsResult> = emptyList(),
    val searchPlaylistsResult: List<PlaylistsResult> = emptyList(),
    val searchFeaturedPlaylistsResult: List<PlaylistsResult> = emptyList(),
    val searchPodcastsResult: List<PlaylistsResult> = emptyList(),
    val suggestQueries: List<String> = emptyList(),
    val suggestYTItems: List<SearchResultType> = emptyList(),
)

enum class SearchScreenUIState {
    Empty,
    Loading,
    Success,
    Error
}

fun SearchType.toStringRes(): Int = when (this) {
    SearchType.ALL -> com.metrolist.music.R.string.all
    SearchType.SONGS -> com.metrolist.music.R.string.song
    SearchType.VIDEOS -> com.metrolist.music.R.string.videos
    SearchType.ALBUMS -> com.metrolist.music.R.string.albums
    SearchType.ARTISTS -> com.metrolist.music.R.string.artists
    SearchType.PLAYLISTS -> com.metrolist.music.R.string.playlists
    SearchType.FEATURED_PLAYLISTS -> com.metrolist.music.R.string.featured_playlists
    SearchType.PODCASTS -> com.metrolist.music.R.string.podcasts
}

enum class SearchType {
    ALL,
    SONGS,
    VIDEOS,
    ALBUMS,
    ARTISTS,
    PLAYLISTS,
    FEATURED_PLAYLISTS,
    PODCASTS,
}
