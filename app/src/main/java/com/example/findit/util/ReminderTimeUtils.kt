package com.example.findit.util

import java.util.Calendar

object ReminderTimeUtils {
    const val REPEAT_INTERVAL_MS = 15 * 60 * 1000L
    const val SNOOZE_INTERVAL_MS = 60 * 60 * 1000L
    const val OVERDUE_FOUND_DAYS = 3

    /** Next occurrence of hour:minute today, or tomorrow if already passed. */
    fun nextWallClockMillis(hour: Int, minute: Int, fromMillis: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = fromMillis
            set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
            set(Calendar.MINUTE, minute.coerceIn(0, 59))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= fromMillis) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }

    fun formatTime(hour: Int, minute: Int): String {
        val h12 = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        val amPm = if (hour < 12) "AM" else "PM"
        return "%d:%02d %s".format(h12, minute, amPm)
    }

    fun isOverdueUnfound(dateCreated: Long, lastFoundAt: Long, now: Long = System.currentTimeMillis()): Boolean {
        if (lastFoundAt != 0L) return false
        val threshold = OVERDUE_FOUND_DAYS * 24L * 60L * 60L * 1000L
        return now - dateCreated >= threshold
    }
}
