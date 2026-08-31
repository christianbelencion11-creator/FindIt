package com.example.iremember.util

import com.example.iremember.model.Item
import com.example.iremember.ui.components.StatsData

fun computeStats(items: List<Item>): StatsData {
    val weekAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000)
    return StatsData(
        totalItems = items.size,
        categories = items.map { it.category }.distinct().size,
        recentlyAdded = items.count { it.dateCreated >= weekAgo }
    )
}
