package com.metrolist.music.data.db

import java.time.LocalDateTime
import java.time.ZoneOffset

class Converters {
    fun dateToTimestamp(date: LocalDateTime?): Long? {
        return date?.toInstant(ZoneOffset.UTC)?.toEpochMilli()
    }
}
