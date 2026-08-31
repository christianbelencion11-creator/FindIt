package com.example.iremember.util

import com.example.iremember.model.Item

private const val RECENTLY_FOUND_WINDOW_MS = 7L * 24 * 60 * 60 * 1000
const val FOUND_RETENTION_MS = 30L * 24 * 60 * 60 * 1000

fun isRecentlyFound(item: Item, now: Long = System.currentTimeMillis()): Boolean {
    if (item.lastFoundAt <= 0L) return false
    return now - item.lastFoundAt <= RECENTLY_FOUND_WINDOW_MS
}

/** Found items older than 30 days are eligible for automatic removal. */
fun isExpiredFound(item: Item, now: Long = System.currentTimeMillis()): Boolean {
    if (item.lastFoundAt <= 0L) return false
    return now - item.lastFoundAt >= FOUND_RETENTION_MS
}
