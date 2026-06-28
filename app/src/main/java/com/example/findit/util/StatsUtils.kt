package com.example.findit.util

import com.example.findit.model.Item
import com.example.findit.ui.components.StatsData

fun computeStats(items: List<Item>): StatsData {
    val weekAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000)
    return StatsData(
        totalItems = items.size,
        categories = items.map { it.category }.distinct().size,
        recentlyAdded = items.count { it.dateCreated >= weekAgo }
    )
}
