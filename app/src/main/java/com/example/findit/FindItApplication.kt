package com.example.findit

import android.app.Application
import com.example.findit.data.local.database.AppDatabase
import com.example.findit.data.repository.ItemRepository
import com.example.findit.reminders.ReminderNotifications
import com.example.findit.reminders.ReminderScheduler
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
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

    override fun onCreate() {
        super.onCreate()
        ReminderNotifications.ensureChannel(this)
        // Ensure Firebase is ready and Firestore is online so auth/create doesn't hang offline.
        FirebaseApp.initializeApp(this)
        runCatching {
            FirebaseFirestore.getInstance().enableNetwork()
        }
        appScope.launch {
            val active = repository.getAllActiveReminders()
            ReminderScheduler.rescheduleAll(this@FindItApplication, active)
        }
    }
}
