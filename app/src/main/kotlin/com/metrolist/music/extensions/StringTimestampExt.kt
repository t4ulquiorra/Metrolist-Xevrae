package com.metrolist.music.extensions

/**
 * Parses a timestamp string in the format [mm:ss.xx] or [mm:ss.xxx] to milliseconds.
 * Returns -1L if the format is unrecognised.
 */
fun parseTimestampToMilliseconds(timestamp: String): Long {
    val cleaned = timestamp.trim().removePrefix("[").removeSuffix("]")
    return try {
        val parts = cleaned.split(":")
        if (parts.size != 2) return -1L
        val minutes = parts[0].toLong()
        val secParts = parts[1].split(".")
        val seconds = secParts[0].toLong()
        val millis = when {
            secParts.size < 2 -> 0L
            secParts[1].length <= 2 -> secParts[1].padEnd(3, '0').toLong()
            else -> secParts[1].take(3).toLong()
        }
        (minutes * 60 + seconds) * 1000L + millis
    } catch (_: Exception) { -1L }
}
