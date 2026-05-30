package com.metrolist.music.domain.manager

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.metrolist.music.utils.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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

    companion object {
        private val LOCAL_PLAYLIST_FILTER_KEY = intPreferencesKey("local_playlist_filter")
        
        const val LOCAL_PLAYLIST_FILTER_OLDER_FIRST = 0
        const val LOCAL_PLAYLIST_FILTER_NEWER_FIRST = 1
        const val LOCAL_PLAYLIST_FILTER_TITLE = 2
        const val LOCAL_PLAYLIST_FILTER_CUSTOM_ORDER = 3
    }
}
