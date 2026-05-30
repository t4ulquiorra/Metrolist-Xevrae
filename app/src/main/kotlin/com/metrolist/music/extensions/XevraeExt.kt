/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.extensions

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.Clock

import com.metrolist.music.db.entities.SearchHistory

fun List<SearchHistory>.toQueryList(): List<String> = map { it.query }

fun now(): LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())


fun LocalDateTime.isBefore(other: LocalDateTime): Boolean = this < other

fun LocalDateTime.isAfter(other: LocalDateTime): Boolean = this > other

fun LocalDateTime.plusSeconds(seconds: Long): LocalDateTime =
    this
        .toInstant(TimeZone.currentSystemDefault())
        .plus(seconds, DateTimeUnit.SECOND, TimeZone.currentSystemDefault())
        .toLocalDateTime(TimeZone.currentSystemDefault())

fun startTimestampOfThisYear(): LocalDateTime = LocalDateTime(now().year, 1, 1, 0, 0, 0, 0)

fun LocalDateTime.beforeXDays(x: Int): LocalDateTime = this.date.minus(x, DateTimeUnit.DAY).atTime(this.time)

fun LocalDateTime.toEpochMilli(): Long = this.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
