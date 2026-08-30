package com.wolfeleo2.myuon.data.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/** Shared display date pattern used across mock models (e.g. "15 Sep 2026"). */
const val DISPLAY_DATE_PATTERN = "dd MMM yyyy"

private val displayDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(DISPLAY_DATE_PATTERN)

/**
 * Parses [dateString] (format [DISPLAY_DATE_PATTERN]) and returns the number of days
 * between [today] and it (negative if it's in the past), or null if it doesn't parse.
 */
fun daysUntil(dateString: String, today: LocalDate = LocalDate.now()): Int? {
    return try {
        val parsed = LocalDate.parse(dateString, displayDateFormatter)
        java.time.temporal.ChronoUnit.DAYS.between(today, parsed).toInt()
    } catch (e: DateTimeParseException) {
        null
    }
}

/** Human-readable label for a day offset, e.g. "Today", "Tomorrow", "in 12 days", "3 days ago". */
fun relativeDayLabel(days: Int?): String {
    if (days == null) return ""
    return when {
        days == 0 -> "Today"
        days == 1 -> "Tomorrow"
        days == -1 -> "Yesterday"
        days > 1 -> "in $days days"
        else -> "${-days} days ago"
    }
}
