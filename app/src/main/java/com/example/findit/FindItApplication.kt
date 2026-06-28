package com.example.findit

import android.app.Application
import com.example.findit.data.local.database.AppDatabase
import com.example.findit.data.repository.ItemRepository

class FindItApplication : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { ItemRepository(database.itemDao()) }
}
