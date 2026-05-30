package com.metrolist.music

interface Platform {
    val name: String
}

class AndroidPlatform : Platform {
    override val name: String = "Android ${android.os.Build.VERSION.SDK_INT}"
}

fun getPlatform(): Platform = AndroidPlatform()
