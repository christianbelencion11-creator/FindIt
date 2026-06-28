package com.example.findit.model

data class Item(
    val id: Long = 0,
    val name: String,
    val location: String,
    val category: String,
    val notes: String,
    val imageUri: String = "",
    val dateCreated: Long = System.currentTimeMillis()
)
