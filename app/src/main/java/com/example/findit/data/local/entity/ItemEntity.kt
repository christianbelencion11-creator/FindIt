package com.example.findit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.findit.model.Item

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val location: String,
    val category: String,
    val notes: String,
    val imageUri: String = "",
    val dateCreated: Long = System.currentTimeMillis()
)

fun ItemEntity.toItem(): Item = Item(
    id = id,
    name = name,
    location = location,
    category = category,
    notes = notes,
    imageUri = imageUri,
    dateCreated = dateCreated
)

fun Item.toEntity(): ItemEntity = ItemEntity(
    id = id,
    name = name,
    location = location,
    category = category,
    notes = notes,
    imageUri = imageUri,
    dateCreated = dateCreated
)
