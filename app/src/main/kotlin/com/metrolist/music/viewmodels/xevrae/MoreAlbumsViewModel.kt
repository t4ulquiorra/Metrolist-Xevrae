package com.metrolist.music.viewmodels.xevrae

import androidx.lifecycle.viewModelScope
import com.metrolist.innertube.YouTube
import com.metrolist.innertube.models.AlbumItem
import com.metrolist.music.models.xevrae.AlbumsResult
import com.metrolist.music.models.xevrae.Thumbnail
import com.metrolist.music.viewmodels.xevrae.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MoreAlbumsViewModel @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val appContext: android.content.Context,) : BaseViewModel(appContext) {
    private val _uiState = MutableStateFlow<MoreAlbumsUIState>(MoreAlbumsUIState.Loading)
    val uiState: StateFlow<MoreAlbumsUIState> get() = _uiState

    fun getAlbumMore(id: String) {
        viewModelScope.launch {
            _uiState.value = MoreAlbumsUIState.Loading
            YouTube.browse(id, ALBUM_PARAM).onSuccess { result ->
                _uiState.value = MoreAlbumsUIState.Success(
                    title = result.title ?: "",
                    albumItems = result.items.flatMap { it.items }.filterIsInstance<AlbumItem>().map { item ->
                        AlbumsResult(
                            browseId = item.browseId,
                            thumbnails = listOf(Thumbnail(item.thumbnail)),
                            title = item.title,
                            year = item.year?.toString(),
                            playlistId = item.playlistId
                        )
                    }
                )
            }.onFailure {
                _uiState.value = MoreAlbumsUIState.Error(message = it.message ?: "Unknown error")
            }
        }
    }

    fun getSingleMore(id: String) {
        viewModelScope.launch {
            _uiState.value = MoreAlbumsUIState.Loading
            YouTube.browse(id, SINGLE_PARAM).onSuccess { result ->
                _uiState.value = MoreAlbumsUIState.Success(
                    title = result.title ?: "",
                    albumItems = result.items.flatMap { it.items }.filterIsInstance<AlbumItem>().map { item ->
                        AlbumsResult(
                            browseId = item.browseId,
                            thumbnails = listOf(Thumbnail(item.thumbnail)),
                            title = item.title,
                            year = item.year?.toString(),
                            playlistId = item.playlistId
                        )
                    }
                )
            }.onFailure {
                _uiState.value = MoreAlbumsUIState.Error(message = it.message ?: "Unknown error")
            }
        }
    }

    companion object {
        const val ALBUM_PARAM = "ggMIegYIARoCAQI%3D"
        const val SINGLE_PARAM = "ggMIegYIAhoCAQI%3D"
    }
}

sealed class MoreAlbumsUIState {
    data class Success(
        val title: String,
        val albumItems: List<AlbumsResult>,
    ) : MoreAlbumsUIState()

    data class Error(
        val message: String,
    ) : MoreAlbumsUIState()

    object Loading : MoreAlbumsUIState()
}
