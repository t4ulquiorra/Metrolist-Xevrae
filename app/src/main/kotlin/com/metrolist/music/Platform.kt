package com.metrolist.music

interface Platform {
    val name: String

    companion object {
        val Android = object : Platform { override val name: String = "Android" }
        val Desktop = object : Platform { override val name: String = "Desktop" }
    }
}

class AndroidPlatform : Platform {
    override val name: String = "Android ${android.os.Build.VERSION.SDK_INT}"
}

fun getPlatform(): Platform = AndroidPlatform()
