package com.example.findit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.findit.model.Note

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ownerUid: String,
    val title: String,
    val body: String = "",
    val remindAt: Long = 0L,
    val remindEnabled: Boolean = false,
    val pinned: Boolean = false,
    val accent: Int = 0,
    val isChecklist: Boolean = true,
    val dateCreated: Long = System.currentTimeMillis(),
    val dateUpdated: Long = System.currentTimeMillis()
)

fun NoteEntity.toNote(): Note = Note(
    id = id,
    ownerUid = ownerUid,
    title = title,
    body = body,
    remindAt = remindAt,
    remindEnabled = remindEnabled,
    pinned = pinned,
    accent = accent,
    isChecklist = isChecklist,
    dateCreated = dateCreated,
    dateUpdated = dateUpdated
)

fun Note.toEntity(): NoteEntity = NoteEntity(
    id = id,
    ownerUid = ownerUid,
    title = title,
    body = body,
    remindAt = remindAt,
    remindEnabled = remindEnabled,
    pinned = pinned,
    accent = accent,
    isChecklist = isChecklist,
    dateCreated = dateCreated,
    dateUpdated = dateUpdated
)
