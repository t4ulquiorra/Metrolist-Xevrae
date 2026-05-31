package com.metrolist.music.viewmodels.xevrae

import com.metrolist.music.playback.PlayerConnection
import com.metrolist.music.viewmodels.xevrae.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class NotificationViewModel @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context,
    playerConnection: PlayerConnection,
) : BaseViewModel(context) {
    private var _listNotification: MutableStateFlow<List<com.metrolist.music.models.xevrae.NotificationEntity>?> =
        MutableStateFlow(emptyList())
    val listNotification: StateFlow<List<com.metrolist.music.models.xevrae.NotificationEntity>?> = _listNotification

    init {
        // Metrolist doesn't have a notification feature yet, returning empty list
    }
}