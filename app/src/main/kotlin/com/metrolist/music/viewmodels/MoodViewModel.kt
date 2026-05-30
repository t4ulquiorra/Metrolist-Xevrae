/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 * Merged with Xevrae UI
 */

package com.metrolist.music.viewmodels

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.metrolist.innertube.YouTube
import com.metrolist.innertube.models.AlbumItem
import com.metrolist.innertube.models.ArtistItem
import com.metrolist.innertube.models.PlaylistItem
import com.metrolist.innertube.models.SongItem
import com.metrolist.innertube.pages.MoodAndGenres
import com.metrolist.music.models.xevrae.MoodContent
import com.metrolist.music.models.xevrae.MoodItem
import com.metrolist.music.models.xevrae.MoodsMomentObject
import com.metrolist.music.models.xevrae.Thumbnail
import com.metrolist.music.utils.reportException
import com.metrolist.music.viewmodels.xevrae.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoodViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : BaseViewModel(context) {

    // --- Metrolist Original State ---
    val moodAndGenres = MutableStateFlow<List<MoodAndGenres>?>(null)

    // --- Xevrae UI State ---
    private val _moodsMomentObject: MutableStateFlow<MoodsMomentObject?> = MutableStateFlow(null)
    val moodsMomentObject: StateFlow<MoodsMomentObject?> = _moodsMomentObject
    val loading = MutableStateFlow<Boolean>(false)

    init {
        loadTopLevelMoods()
    }

    fun loadTopLevelMoods() {
        viewModelScope.launch {
            YouTube.moodAndGenres()
                .onSuccess {
                    moodAndGenres.value = it
                }.onFailure {
                    reportException(it)
                }
        }
    }

    fun getMood(params: String) {
        if (params.isEmpty()) return
        
        loading.value = true
        viewModelScope.launch {
            YouTube.browse(params, null).onSuccess { result ->
                _moodsMomentObject.value = MoodsMomentObject(
                    header = result.title ?: "",
                    items = result.items.map { item ->
                        MoodItem(
                            header = item.title ?: "",
                            contents = item.items.map { ytItem ->
                                MoodContent(
                                    title = ytItem.title,
                                    subtitle = when (ytItem) {
                                        is SongItem -> ytItem.artists.joinToString { it.name }
                                        is AlbumItem -> ytItem.artists?.joinToString { it.name }
                                        is PlaylistItem -> ytItem.author?.name
                                        is ArtistItem -> null
                                        else -> null
                                    },
                                    thumbnails = ytItem.thumbnail?.let { listOf(Thumbnail(it)) },
                                    playlistBrowseId = when (ytItem) {
                                        is AlbumItem -> ytItem.browseId
                                        is PlaylistItem -> ytItem.id
                                        is ArtistItem -> ytItem.id
                                        else -> ytItem.id
                                    }
                                )
                            }
                        )
                    }
                )
            }.onFailure {
                _moodsMomentObject.value = null
            }
            loading.value = false
        }
    }
}
