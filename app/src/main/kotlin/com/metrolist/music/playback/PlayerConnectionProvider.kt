package com.metrolist.music.playback

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerConnectionProvider @Inject constructor() {
    @Volatile
    var connection: PlayerConnection? = null
}
