package com.metrolist.music.utils

import android.os.Environment

fun getDownloadFolderPath(): String =
    Environment
        .getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS,
        ).path
