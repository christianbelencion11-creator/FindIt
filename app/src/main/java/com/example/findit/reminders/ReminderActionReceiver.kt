package com.example.findit.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.findit.FindItApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val itemId = intent?.getLongExtra(ReminderNotifications.EXTRA_ITEM_ID, -1L) ?: -1L
        if (itemId < 0L) return
        val app = context.applicationContext as? FindItApplication ?: return
        val pending = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent?.action) {
                    ReminderNotifications.ACTION_SNOOZE -> {
                        val updated = app.repository.snoozeReminder(itemId)
                        ReminderNotifications.cancel(context, itemId)
                        if (updated != null) {
                            ReminderScheduler.schedule(context, updated)
                        }
                    }
                    ReminderNotifications.ACTION_STOP -> {
                        app.repository.stopReminder(itemId)
                        ReminderScheduler.cancel(context, itemId)
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }
}
