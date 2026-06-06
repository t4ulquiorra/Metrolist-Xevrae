package com.metrolist.music.viewmodels.xevrae

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.metrolist.music.constants.DataSyncIdKey
import com.metrolist.music.playback.PlayerConnectionProvider
import com.metrolist.music.constants.DiscordTokenKey
import com.metrolist.music.constants.InnerTubeCookieKey
import com.metrolist.music.constants.VisitorDataKey
import com.metrolist.music.utils.dataStore
import com.metrolist.music.viewmodels.xevrae.base.BaseViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

val SpotifySpdcKey = stringPreferencesKey("spotify_spdc")

@HiltViewModel
class LogInViewModel @Inject constructor(
    @ApplicationContext context: Context,
    playerConnectionProvider: PlayerConnectionProvider,
) : BaseViewModel(context, playerConnectionProvider) {
    private val _spotifyStatus: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val spotifyStatus: StateFlow<Boolean> get() = _spotifyStatus

    private val _fullSpotifyCookies: MutableStateFlow<List<Pair<String, String?>>> = MutableStateFlow(emptyList())
    val fullSpotifyCookies: StateFlow<List<Pair<String, String?>>> get() = _fullSpotifyCookies.asStateFlow()

    private val _fullYouTubeCookies: MutableStateFlow<List<Pair<String, String?>>> = MutableStateFlow(emptyList())
    val fullYouTubeCookies: StateFlow<List<Pair<String, String?>>> get() = _fullYouTubeCookies.asStateFlow()

    fun saveSpotifySpdc(cookie: String) {
        viewModelScope.launch {
            cookie
                .split("; ")
                .filter { it.isNotEmpty() }
                .associate {
                    val parts = it.split("=")
                    if (parts.size == 2) parts[0] to parts[1] else "" to ""
                }.let {
                    val spdc = it["sp_dc"] ?: ""
                    context.dataStore.edit { settings ->
                        settings[SpotifySpdcKey] = spdc
                    }
                    _spotifyStatus.value = true
                }
        }
    }

    fun setVisitorData(visitorData: String) {
        viewModelScope.launch {
            context.dataStore.edit { it[VisitorDataKey] = visitorData }
        }
    }

    fun setDataSyncId(dataSyncId: String) {
        viewModelScope.launch {
            context.dataStore.edit { it[DataSyncIdKey] = dataSyncId }
        }
    }

    fun setFullSpotifyCookies(cookies: List<Pair<String, String?>>) {
        viewModelScope.launch {
            _fullSpotifyCookies.value = cookies
        }
    }

    fun setFullYouTubeCookies(cookies: List<Pair<String, String?>>) {
        viewModelScope.launch {
            _fullYouTubeCookies.value = cookies
        }
    }

    fun saveDiscordToken(token: String) {
        viewModelScope.launch {
            context.dataStore.edit { it[DiscordTokenKey] = token }
        }
    }
}