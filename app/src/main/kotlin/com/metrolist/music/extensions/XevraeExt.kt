/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.extensions

import com.metrolist.music.db.entities.SearchHistory
import java.time.LocalDateTime
import java.time.ZoneId

fun List<SearchHistory>.toQueryList(): List<String> = map { it.query }

fun now(): LocalDateTime = LocalDateTime.now()

fun LocalDateTime.isBefore2(other: LocalDateTime): Boolean = this.isBefore(other)

fun LocalDateTime.isAfter2(other: LocalDateTime): Boolean = this.isAfter(other)

fun LocalDateTime.plusSeconds(seconds: Long): LocalDateTime = this.plusSeconds(seconds)

fun startTimestampOfThisYear(): LocalDateTime = LocalDateTime.of(LocalDateTime.now().year, 1, 1, 0, 0, 0, 0)

fun LocalDateTime.beforeXDays(x: Int): LocalDateTime = this.minusDays(x.toLong())

fun LocalDateTime.toEpochMilli(): Long = this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
