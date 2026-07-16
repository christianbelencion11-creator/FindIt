package com.example.findit.reminders

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.findit.FindItApplication

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val itemId = inputData.getLong(KEY_ITEM_ID, -1L)
        if (itemId < 0L) return Result.failure()

        val app = applicationContext as? FindItApplication
            ?: return Result.retry()
        val repository = app.repository
        val item = repository.getItemByIdOnce(itemId) ?: return Result.success()

        if (!item.remindActive) {
            ReminderScheduler.cancel(applicationContext, itemId)
            return Result.success()
        }

        ReminderNotifications.showReminder(applicationContext, item)
        val updated = repository.bumpReminderAfterNotify(itemId)
        if (updated != null && updated.remindActive) {
            ReminderScheduler.schedule(applicationContext, updated)
        }
        return Result.success()
    }

    companion object {
        const val KEY_ITEM_ID = "item_id"
    }
}
