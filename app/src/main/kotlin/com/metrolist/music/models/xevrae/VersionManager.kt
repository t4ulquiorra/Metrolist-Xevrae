package com.metrolist.music.models.xevrae

import com.metrolist.music.BuildConfig

object VersionManager {
    private var versionName: String? = null

    fun initialize() {
        if (versionName == null) {
            versionName =
                try {
                    BuildConfig.VERSION_NAME
                } catch (_: Exception) {
                    ""
                }
        }
    }

    fun getVersionName(): String = removeDevSuffix(versionName ?: "")

    private fun removeDevSuffix(versionName: String): String {
        return if (versionName.endsWith("-dev")) {
            versionName.replace("-dev", "")
        } else {
            versionName
        }
    }
}
