package com.example.findit.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.findit.FindItApplication
import com.example.findit.data.repository.ItemRepository
import com.example.findit.model.Item
import com.example.findit.reminders.ReminderScheduler
import com.example.findit.util.FOUND_RETENTION_MS
import com.example.findit.util.ReminderTimeUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ItemViewModel(
    application: Application,
    private val repository: ItemRepository
) : AndroidViewModel(application) {

    init {
        viewModelScope.launch {
            repository.currentOwnerUid.collect { uid ->
                if (uid.isNotBlank()) {
                    val cutoff = System.currentTimeMillis() - FOUND_RETENTION_MS
                    val deletedIds = repository.deleteExpiredFoundItems(cutoff)
                    deletedIds.forEach { id ->
                        ReminderScheduler.cancel(getApplication(), id)
                    }
                }
            }
        }
    }

    val allItems: StateFlow<List<Item>> = repository.allItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val activeReminders: StateFlow<List<Item>> = repository.activeReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val overdueUnfoundItems: StateFlow<List<Item>> = allItems
        .map { items ->
            items.filter {
                ReminderTimeUtils.isOverdueUnfound(it.dateCreated, it.lastFoundAt)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<Item>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.allItems
            } else {
                repository.searchItems(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun itemById(id: Long): Flow<Item?> = repository.getItemById(id)

    fun saveItem(
        name: String,
        location: String,
        category: String,
        notes: String,
        imageUri: String,
        remindEnabled: Boolean = false,
        remindHour: Int = 8,
        remindMinute: Int = 0,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            val nextAt = if (remindEnabled) {
                ReminderTimeUtils.nextWallClockMillis(remindHour, remindMinute)
            } else {
                0L
            }
            val id = repository.insertItem(
                Item(
                    name = name.trim(),
                    location = location.trim(),
                    category = category.trim(),
                    notes = notes.trim(),
                    imageUri = imageUri,
                    remindEnabled = remindEnabled,
                    remindHour = remindHour,
                    remindMinute = remindMinute,
                    remindNextAt = nextAt,
                    remindActive = remindEnabled
                )
            )
            if (remindEnabled && id > 0L) {
                repository.getItemByIdOnce(id)?.let { item ->
                    ReminderScheduler.schedule(getApplication(), item)
                }
            }
            onSaved()
        }
    }

    fun setReminder(itemId: Long, hour: Int, minute: Int, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val updated = repository.setReminder(itemId, hour, minute)
            if (updated != null) {
                ReminderScheduler.schedule(getApplication(), updated)
                onDone(true)
            } else {
                onDone(false)
            }
        }
    }

    fun snoozeReminder(itemId: Long, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val updated = repository.snoozeReminder(itemId)
            if (updated != null) {
                ReminderScheduler.schedule(getApplication(), updated)
                onDone(true)
            } else {
                onDone(false)
            }
        }
    }

    fun stopReminder(itemId: Long, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val ok = repository.stopReminder(itemId)
            ReminderScheduler.cancel(getApplication(), itemId)
            onDone(ok)
        }
    }

    fun markItemFound(itemId: Long, onMarked: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val success = repository.markItemFound(itemId)
            if (success) {
                repository.stopReminder(itemId)
                ReminderScheduler.cancel(getApplication(), itemId)
            }
            onMarked(success)
        }
    }

    fun deleteItem(itemId: Long, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            ReminderScheduler.cancel(getApplication(), itemId)
            val ok = repository.deleteItem(itemId)
            onDone(ok)
        }
    }

    fun purgeExpiredFoundItems() {
        viewModelScope.launch {
            val cutoff = System.currentTimeMillis() - FOUND_RETENTION_MS
            val deletedIds = repository.deleteExpiredFoundItems(cutoff)
            deletedIds.forEach { id ->
                ReminderScheduler.cancel(getApplication(), id)
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as FindItApplication
                ItemViewModel(app, app.repository)
            }
        }
    }
}
