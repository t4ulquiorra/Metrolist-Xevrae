/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.extensions


import kotlinx.datetime.atTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant

import com.metrolist.music.db.entities.SearchHistory

fun List<SearchHistory>.toQueryList(): List<String> = map { it.query }

fun now(): java.time.LocalDateTime = java.time.LocalDateTime.now()


fun java.time.LocalDateTime.isBefore2(other: java.time.LocalDateTime): Boolean = this.isBefore(other)

fun java.time.LocalDateTime.isAfter2(other: java.time.LocalDateTime): Boolean = this.isAfter(other)

fun LocalDateTime.plusSeconds(seconds: Long): LocalDateTime =
    this
        .toInstant(TimeZone.currentSystemDefault())
        .plus(seconds, DateTimeUnit.SECOND, TimeZone.currentSystemDefault())
        .toLocalDateTime(TimeZone.currentSystemDefault())

fun startTimestampOfThisYear(): LocalDateTime = LocalDateTime(now().year, 1, 1, 0, 0, 0, 0)

fun LocalDateTime.beforeXDays(x: Int): LocalDateTime = this.date.minus(x, DateTimeUnit.DAY).atTime(this.time)

fun LocalDateTime.toEpochMilli(): Long = this.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
