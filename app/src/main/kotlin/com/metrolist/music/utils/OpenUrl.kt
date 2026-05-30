package com.metrolist.music.utils

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.core.net.toUri

fun openUrl(context: Context, url: String) {
    val browserIntent =
        Intent(
            Intent.ACTION_VIEW,
            url.toUri(),
        )
    browserIntent.setFlags(FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(browserIntent)
}

fun shareUrl(
    context: Context,
    title: String,
    url: String,
) {
    val shareIntent = Intent(Intent.ACTION_SEND)
    shareIntent.type = "text/plain"
    shareIntent.putExtra(Intent.EXTRA_TEXT, url)
    shareIntent.setFlags(FLAG_ACTIVITY_NEW_TASK)
    val chooserIntent =
        Intent.createChooser(shareIntent, title)
    context.startActivity(chooserIntent)
}
