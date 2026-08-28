package com.example.findit

import android.app.Application
import com.example.findit.data.local.database.AppDatabase
import com.example.findit.data.repository.BankCardRepository
import com.example.findit.data.repository.ItemRepository
import com.example.findit.data.repository.NoteRepository
import com.example.findit.reminders.NoteReminderScheduler
import com.example.findit.reminders.ReminderNotifications
import com.example.findit.reminders.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FindItApplication : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy {
        ItemRepository(database.itemDao(), database.itemHistoryDao())
    }
    val noteRepository by lazy {
        NoteRepository(database.noteDao())
    }
    val bankCardRepository by lazy {
        BankCardRepository(database.bankCardDao())
    }

    override fun onCreate() {
        super.onCreate()
        ReminderNotifications.ensureChannel(this)
        appScope.launch {
            val active = repository.getAllActiveReminders()
            ReminderScheduler.rescheduleAll(this@FindItApplication, active)
            val noteReminders = noteRepository.getAllWithReminders()
            NoteReminderScheduler.rescheduleAll(this@FindItApplication, noteReminders)
        }
    }
}
