package com.metrolist.music.viewmodels.xevrae.base

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.metrolist.music.playback.PlayerConnection
import com.metrolist.music.logger.LogLevel
import com.metrolist.music.logger.Logger
import javax.inject.Inject
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import android.widget.Toast
import com.metrolist.music.domain.mediaservice.handler.QueueData
import com.metrolist.music.extensions.toMediaItem
import com.metrolist.music.playback.queues.ListQueue

abstract class BaseViewModel(
    protected val context: Context
) : ViewModel() {
    @Inject
    protected lateinit var playerConnection: PlayerConnection
    private val _nowPlayingVideoId: MutableStateFlow<String> = MutableStateFlow("")

    /**
     * Get now playing video id
     * If empty, no video is playing
     */
    val nowPlayingVideoId: StateFlow<String> get() = _nowPlayingVideoId

    /**
     * Tag for logging
     */
    protected val tag: String = this::class.simpleName ?: "BaseViewModel"

    /**
     * Log with viewModel tag
     */
    protected fun log(
        message: String,
        logType: LogLevel = LogLevel.WARN,
    ) {
        when (logType) {
            LogLevel.DEBUG -> Logger.d(tag, message)
            LogLevel.INFO -> Logger.i(tag, message)
            LogLevel.WARN -> Logger.w(tag, message)
            LogLevel.ERROR -> Logger.e(tag, message)
        }
    }

    /**
     * Cancel all jobs
     */
    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
        log("ViewModel cleared", LogLevel.WARN)
    }

    init {
        viewModelScope.launch {
            // Wait for playerConnection to be initialized by Hilt
            // This is a bit hacky but since it's @Inject it should be fine
            getNowPlayingVideoId()
        }
    }

    fun makeToast(message: String?) {
        Toast.makeText(context, message ?: "NO MESSAGE", Toast.LENGTH_SHORT).show()
    }

    protected fun getString(resId: Int): String = context.getString(resId)

    // Loading dialog
    private val _showLoadingDialog: MutableStateFlow<Pair<Boolean, String>> = MutableStateFlow(false to "")
    val showLoadingDialog: StateFlow<Pair<Boolean, String>> get() = _showLoadingDialog

    fun showLoadingDialog(message: String? = null) {
        viewModelScope.launch {
            _showLoadingDialog.value = true to (message ?: getString(com.metrolist.music.R.string.loading))
        }
    }

    fun hideLoadingDialog() {
        viewModelScope.launch {
            _showLoadingDialog.value = false to getString(com.metrolist.music.R.string.loading)
        }
    }

    private fun getNowPlayingVideoId() {
        viewModelScope.launch {
            try {
                combine(playerConnection.mediaMetadata, playerConnection.isPlaying) { metadata, isPlaying ->
                    Pair(metadata, isPlaying)
                }.collect { (metadata, isPlaying) ->
                    if (isPlaying) {
                        _nowPlayingVideoId.value = metadata?.id ?: ""
                    } else {
                        _nowPlayingVideoId.value = ""
                    }
                }
            } catch (e: Exception) {
                // playerConnection might not be initialized yet
            }
        }
    }

    /**
     * Communicate with PlayerConnection to load media item
     */
    fun setQueueData(queueData: QueueData.Data) {
        val queue = ListQueue(
            title = queueData.playlistName,
            items = queueData.listTracks.map { track ->
                track.toMediaItem()
            }
        )
        playerConnection.playQueue(queue, queueData.listTracks.indexOf(queueData.firstPlayedTrack).coerceAtLeast(0))
    }

    fun <T> loadMediaItem(
        anyTrack: T,
        type: String,
        index: Int? = null,
    ) {
        index?.let {
            playerConnection.player.seekTo(it, 0)
            playerConnection.player.play()
        }
    }

    fun shufflePlaylist(firstPlayIndex: Int = 0) {
        playerConnection.player.shuffleModeEnabled = true
        playerConnection.player.seekTo(firstPlayIndex, 0)
        playerConnection.player.play()
    }
}
