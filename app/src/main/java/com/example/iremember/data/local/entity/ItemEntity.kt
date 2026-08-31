package com.example.iremember.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.iremember.model.Item

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val location: String,
    val category: String,
    val notes: String,
    val imageUri: String = "",
    val dateCreated: Long = System.currentTimeMillis(),
    val ownerUid: String = "",
    val lastFoundAt: Long = 0,
    val remindEnabled: Boolean = false,
    val remindHour: Int = 8,
    val remindMinute: Int = 0,
    val remindNextAt: Long = 0L,
    val remindActive: Boolean = false,
    val deletedAt: Long = 0L
)

fun ItemEntity.toItem(): Item = Item(
    id = id,
    name = name,
    location = location,
    category = category,
    notes = notes,
    imageUri = imageUri,
    dateCreated = dateCreated,
    ownerUid = ownerUid,
    lastFoundAt = lastFoundAt,
    remindEnabled = remindEnabled,
    remindHour = remindHour,
    remindMinute = remindMinute,
    remindNextAt = remindNextAt,
    remindActive = remindActive,
    deletedAt = deletedAt
)

fun Item.toEntity(): ItemEntity = ItemEntity(
    id = id,
    name = name,
    location = location,
    category = category,
    notes = notes,
    imageUri = imageUri,
    dateCreated = dateCreated,
    ownerUid = ownerUid,
    lastFoundAt = lastFoundAt,
    remindEnabled = remindEnabled,
    remindHour = remindHour,
    remindMinute = remindMinute,
    remindNextAt = remindNextAt,
    remindActive = remindActive,
    deletedAt = deletedAt
)
