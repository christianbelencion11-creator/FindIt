package com.example.findit.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.findit.MainActivity
import com.example.findit.R
import com.example.findit.model.Item
import com.example.findit.model.Note

object ReminderNotifications {
    const val CHANNEL_ID = "item_reminders"
    const val NOTES_CHANNEL_ID = "note_reminders"
    const val ACTION_SNOOZE = "com.example.findit.reminders.SNOOZE"
    const val ACTION_STOP = "com.example.findit.reminders.STOP"
    const val EXTRA_ITEM_ID = "item_id"
    const val EXTRA_NOTE_ID = "note_id"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Item reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminders about items you left behind"
        }
        manager.createNotificationChannel(channel)
        val notesChannel = NotificationChannel(
            NOTES_CHANNEL_ID,
            "Notes reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Reminders for your Notes lists"
        }
        manager.createNotificationChannel(notesChannel)
    }

    fun showReminder(context: Context, item: Item) {
        ensureChannel(context)
        val openApp = PendingIntent.getActivity(
            context,
            item.id.toInt(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_ITEM_ID, item.id)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val snooze = PendingIntent.getBroadcast(
            context,
            (item.id * 2).toInt(),
            Intent(context, ReminderActionReceiver::class.java).apply {
                action = ACTION_SNOOZE
                putExtra(EXTRA_ITEM_ID, item.id)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stop = PendingIntent.getBroadcast(
            context,
            (item.id * 2 + 1).toInt(),
            Intent(context, ReminderActionReceiver::class.java).apply {
                action = ACTION_STOP
                putExtra(EXTRA_ITEM_ID, item.id)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Remind Me: ${item.name}")
            .setContentText("You left this at ${item.location}")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("You left ${item.name} at ${item.location}. Confirm when you find it, or snooze / stop this reminder.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .addAction(0, "Snooze 1h", snooze)
            .addAction(0, "Stop", stop)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(item.id.toInt(), notification)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS not granted
        }
    }

    fun cancel(context: Context, itemId: Long) {
        NotificationManagerCompat.from(context).cancel(itemId.toInt())
    }

    fun showNoteReminder(context: Context, note: Note) {
        ensureChannel(context)
        val notificationId = (1_000_000 + note.id).toInt()
        val openApp = PendingIntent.getActivity(
            context,
            notificationId,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_NOTE_ID, note.id)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val preview = note.body.trim().ifBlank { "Open Notes to review your list." }
        val notification = NotificationCompat.Builder(context, NOTES_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Notes: ${note.title}")
            .setContentText(preview.take(120))
            .setStyle(NotificationCompat.BigTextStyle().bigText(preview))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS not granted
        }
    }

    fun cancelNote(context: Context, noteId: Long) {
        NotificationManagerCompat.from(context).cancel((1_000_000 + noteId).toInt())
    }
}
