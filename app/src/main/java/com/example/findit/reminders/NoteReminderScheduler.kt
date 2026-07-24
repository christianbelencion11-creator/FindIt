package com.example.findit.reminders

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.findit.model.Note
import java.util.concurrent.TimeUnit

object NoteReminderScheduler {
    private fun workName(noteId: Long) = "remind_note_$noteId"

    fun schedule(context: Context, note: Note) {
        if (!note.remindEnabled || note.remindAt <= 0L) {
            cancel(context, note.id)
            return
        }
        val delay = (note.remindAt - System.currentTimeMillis()).coerceAtLeast(0L)
        val request = OneTimeWorkRequestBuilder<NoteReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(NoteReminderWorker.KEY_NOTE_ID to note.id))
            .addTag(workName(note.id))
            .build()
        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            workName(note.id),
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancel(context: Context, noteId: Long) {
        WorkManager.getInstance(context.applicationContext).cancelUniqueWork(workName(noteId))
        ReminderNotifications.cancelNote(context, noteId)
    }

    fun rescheduleAll(context: Context, notes: List<Note>) {
        notes.filter { it.remindEnabled && it.remindAt > 0L }.forEach { schedule(context, it) }
    }
}
