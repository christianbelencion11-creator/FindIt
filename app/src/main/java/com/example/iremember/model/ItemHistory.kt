package com.example.iremember.model

enum class HistoryAction {
    FOUND,
    EDITED,
    DELETED
}

data class ItemHistory(
    val id: Long = 0,
    val ownerUid: String = "",
    val itemId: Long = 0,
    val itemName: String,
    val action: HistoryAction,
    val detail: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
