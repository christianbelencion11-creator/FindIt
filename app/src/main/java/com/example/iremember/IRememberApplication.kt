package com.example.iremember

import android.app.Application
import com.example.iremember.data.local.database.AppDatabase
import com.example.iremember.data.repository.BankCardRepository
import com.example.iremember.data.repository.ItemRepository
import com.example.iremember.data.repository.NoteRepository
import com.example.iremember.reminders.NoteReminderScheduler
import com.example.iremember.reminders.ReminderNotifications
import com.example.iremember.reminders.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class IRememberApplication : Application() {
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
            ReminderScheduler.rescheduleAll(this@IRememberApplication, active)
            val noteReminders = noteRepository.getAllWithReminders()
            NoteReminderScheduler.rescheduleAll(this@IRememberApplication, noteReminders)
        }
    }
}
