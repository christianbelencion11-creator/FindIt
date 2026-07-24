package com.example.findit.data.repository

import com.example.findit.data.local.dao.NoteDao
import com.example.findit.data.local.entity.toEntity
import com.example.findit.data.local.entity.toNote
import com.example.findit.model.Note
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class NoteRepository(
    private val noteDao: NoteDao
) {
    private val _currentOwnerUid = MutableStateFlow("")
    val currentOwnerUid = _currentOwnerUid.asStateFlow()

    fun setOwnerUid(uid: String) {
        _currentOwnerUid.value = uid
    }

    val allNotes: Flow<List<Note>> = _currentOwnerUid.flatMapLatest { ownerUid ->
        if (ownerUid.isBlank()) {
            flowOf(emptyList())
        } else {
            noteDao.observeAll(ownerUid).map { list -> list.map { it.toNote() } }
        }
    }

    fun noteById(id: Long): Flow<Note?> = _currentOwnerUid.flatMapLatest { ownerUid ->
        if (ownerUid.isBlank()) {
            flowOf(null)
        } else {
            noteDao.observeById(ownerUid, id).map { it?.toNote() }
        }
    }

    suspend fun getByIdOnce(id: Long): Note? = noteDao.getByIdOnce(id)?.toNote()

    suspend fun getAllWithReminders(): List<Note> =
        noteDao.getAllWithReminders().map { it.toNote() }

    suspend fun upsert(note: Note): Long {
        val ownerUid = _currentOwnerUid.value
        require(ownerUid.isNotBlank()) { "Cannot save notes without a signed-in user." }
        val now = System.currentTimeMillis()
        val entity = note.copy(
            ownerUid = ownerUid,
            dateUpdated = now,
            dateCreated = if (note.id == 0L) now else note.dateCreated
        ).toEntity()
        return if (note.id == 0L) {
            noteDao.insert(entity)
        } else {
            noteDao.update(entity)
            note.id
        }
    }

    suspend fun delete(noteId: Long): Boolean {
        val ownerUid = _currentOwnerUid.value
        if (ownerUid.isBlank()) return false
        return noteDao.delete(ownerUid, noteId) > 0
    }
}
