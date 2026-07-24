package com.example.findit.model

data class Item(
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
    val remindNextAt: Long = 0,
    val remindActive: Boolean = false,
    /** 0 = active; otherwise soft-delete timestamp for History restore / 30-day purge. */
    val deletedAt: Long = 0
)
