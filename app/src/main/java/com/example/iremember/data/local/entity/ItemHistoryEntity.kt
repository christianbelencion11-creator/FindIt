package com.example.iremember.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.iremember.model.HistoryAction
import com.example.iremember.model.ItemHistory

@Entity(tableName = "item_history")
data class ItemHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ownerUid: String,
    val itemId: Long = 0,
    val itemName: String,
    val action: String,
    val detail: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

fun ItemHistoryEntity.toItemHistory(): ItemHistory = ItemHistory(
    id = id,
    ownerUid = ownerUid,
    itemId = itemId,
    itemName = itemName,
    action = runCatching { HistoryAction.valueOf(action) }.getOrDefault(HistoryAction.EDITED),
    detail = detail,
    createdAt = createdAt
)

fun ItemHistory.toEntity(): ItemHistoryEntity = ItemHistoryEntity(
    id = id,
    ownerUid = ownerUid,
    itemId = itemId,
    itemName = itemName,
    action = action.name,
    detail = detail,
    createdAt = createdAt
)
