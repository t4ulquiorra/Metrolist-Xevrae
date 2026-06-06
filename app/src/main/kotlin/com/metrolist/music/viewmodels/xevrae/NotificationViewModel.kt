package com.metrolist.music.viewmodels.xevrae

import com.metrolist.music.viewmodels.xevrae.base.BaseViewModel
import com.metrolist.music.playback.PlayerConnectionProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class NotificationViewModel @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context,
    playerConnectionProvider: PlayerConnectionProvider,
) : BaseViewModel(context, playerConnectionProvider) {
    private var _listNotification: MutableStateFlow<List<com.metrolist.music.models.xevrae.NotificationEntity>?> =
        MutableStateFlow(emptyList())
    val listNotification: StateFlow<List<com.metrolist.music.models.xevrae.NotificationEntity>?> = _listNotification

    init {
        // Metrolist doesn't have a notification feature yet, returning empty list
    }
}