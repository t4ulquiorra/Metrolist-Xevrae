package com.metrolist.music.utils

import timber.log.Timber

object Logger {
    fun d(tag: String, message: String) {
        Timber.tag(tag).d(message)
    }

    fun i(tag: String, message: String) {
        Timber.tag(tag).i(message)
    }

    fun w(tag: String, message: String) {
        Timber.tag(tag).w(message)
    }

    fun e(tag: String, message: String, e: Throwable? = null) {
        Timber.tag(tag).e(e, message)
    }
}
