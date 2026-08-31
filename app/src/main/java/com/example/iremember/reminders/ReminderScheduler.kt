package com.example.iremember.reminders

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.iremember.model.Item
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    private fun workName(itemId: Long) = "remind_item_$itemId"

    fun schedule(context: Context, item: Item) {
        if (!item.remindActive || item.remindNextAt <= 0L) {
            cancel(context, item.id)
            return
        }
        val delay = (item.remindNextAt - System.currentTimeMillis()).coerceAtLeast(0L)
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(ReminderWorker.KEY_ITEM_ID to item.id))
            .addTag(workName(item.id))
            .build()
        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            workName(item.id),
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancel(context: Context, itemId: Long) {
        WorkManager.getInstance(context.applicationContext).cancelUniqueWork(workName(itemId))
        ReminderNotifications.cancel(context, itemId)
    }

    fun rescheduleAll(context: Context, items: List<Item>) {
        items.filter { it.remindActive }.forEach { schedule(context, it) }
    }
}
