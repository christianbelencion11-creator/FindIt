package com.example.findit.reminders

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.findit.FindItApplication

class NoteReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val noteId = inputData.getLong(KEY_NOTE_ID, -1L)
        if (noteId <= 0L) return Result.failure()
        val app = applicationContext as? FindItApplication ?: return Result.failure()
        val note = app.noteRepository.getByIdOnce(noteId) ?: return Result.success()
        if (!note.remindEnabled) return Result.success()
        ReminderNotifications.showNoteReminder(applicationContext, note)
        return Result.success()
    }

    companion object {
        const val KEY_NOTE_ID = "note_id"
    }
}
