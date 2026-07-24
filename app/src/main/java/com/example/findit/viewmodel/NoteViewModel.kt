package com.example.findit.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.findit.FindItApplication
import com.example.findit.data.repository.NoteRepository
import com.example.findit.model.Note
import com.example.findit.reminders.NoteReminderScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteViewModel(
    application: Application,
    private val repository: NoteRepository
) : AndroidViewModel(application) {

    val allNotes: StateFlow<List<Note>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun noteById(id: Long): Flow<Note?> = repository.noteById(id)

    fun saveNote(
        noteId: Long,
        title: String,
        body: String,
        remindEnabled: Boolean,
        remindAt: Long,
        pinned: Boolean = false,
        accent: Int = 0,
        isChecklist: Boolean = true,
        onSaved: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val existing = if (noteId > 0L) repository.getByIdOnce(noteId) else null
            val note = Note(
                id = noteId.takeIf { it > 0L } ?: 0L,
                title = title.trim().ifBlank { "Untitled note" },
                body = body.trim(),
                remindEnabled = remindEnabled,
                remindAt = if (remindEnabled) remindAt else 0L,
                pinned = pinned,
                accent = accent.coerceIn(0, 4),
                isChecklist = isChecklist,
                dateCreated = existing?.dateCreated ?: System.currentTimeMillis()
            )
            val id = repository.upsert(note)
            val saved = repository.getByIdOnce(id)
            if (saved != null) {
                if (saved.remindEnabled) {
                    NoteReminderScheduler.schedule(getApplication(), saved)
                } else {
                    NoteReminderScheduler.cancel(getApplication(), id)
                }
            }
            onSaved(id)
        }
    }

    fun deleteNote(noteId: Long, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            NoteReminderScheduler.cancel(getApplication(), noteId)
            onDone(repository.delete(noteId))
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as FindItApplication
                NoteViewModel(app, app.noteRepository)
            }
        }
    }
}
