package com.metrolist.music.viewmodels.xevrae

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.metrolist.music.db.MusicDatabase
import com.metrolist.music.playback.PlayerConnectionProvider
import com.metrolist.music.db.entities.SongEntity
import com.metrolist.music.viewmodels.xevrae.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@HiltViewModel
class RecentlySongsViewModel @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context,
    private val database: MusicDatabase,
    playerConnectionProvider: PlayerConnectionProvider,
) : BaseViewModel(context, playerConnectionProvider) {
    val recentlySongs: Flow<PagingData<SongEntity>> =
        database.events()
            .map { events ->
                events.map { it.song.song }.distinctBy { it.id }
            }
            .map { songs ->
                PagingData.from(songs)
            }.cachedIn(viewModelScope)
}
