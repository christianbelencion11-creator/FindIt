package com.example.findit.data.repository

import com.example.findit.data.local.dao.ItemDao
import com.example.findit.data.local.entity.toEntity
import com.example.findit.data.local.entity.toItem
import com.example.findit.model.Item
import com.example.findit.util.ReminderTimeUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class ItemRepository(private val itemDao: ItemDao) {

    private val _currentOwnerUid = MutableStateFlow("")
    val currentOwnerUid = _currentOwnerUid.asStateFlow()

    fun setOwnerUid(uid: String) {
        _currentOwnerUid.value = uid
    }

    val allItems: Flow<List<Item>> = _currentOwnerUid.flatMapLatest { ownerUid ->
        if (ownerUid.isBlank()) {
            flowOf(emptyList())
        } else {
            itemDao.getAllItems(ownerUid).map { entities ->
                entities.map { it.toItem() }
            }
        }
    }

    val activeReminders: Flow<List<Item>> = _currentOwnerUid.flatMapLatest { ownerUid ->
        if (ownerUid.isBlank()) {
            flowOf(emptyList())
        } else {
            itemDao.getActiveReminders(ownerUid).map { entities ->
                entities.map { it.toItem() }
            }
        }
    }

    fun getItemById(id: Long): Flow<Item?> = _currentOwnerUid.flatMapLatest { ownerUid ->
        if (ownerUid.isBlank()) {
            flowOf(null)
        } else {
            itemDao.getItemById(ownerUid, id).map { it?.toItem() }
        }
    }

    fun searchItems(query: String): Flow<List<Item>> = _currentOwnerUid.flatMapLatest { ownerUid ->
        if (ownerUid.isBlank()) {
            flowOf(emptyList())
        } else {
            itemDao.searchItems(ownerUid, query).map { entities ->
                entities.map { it.toItem() }
            }
        }
    }

    suspend fun getItemByIdOnce(id: Long): Item? =
        itemDao.getItemByIdOnce(id)?.toItem()

    suspend fun getAllActiveReminders(): List<Item> =
        itemDao.getAllActiveReminders().map { it.toItem() }

    suspend fun insertItem(item: Item): Long {
        val ownerUid = _currentOwnerUid.value
        require(ownerUid.isNotBlank()) { "Cannot save items without a signed-in user." }
        return itemDao.insertItem(item.copy(ownerUid = ownerUid).toEntity())
    }

    suspend fun markItemFound(itemId: Long): Boolean {
        val ownerUid = _currentOwnerUid.value
        if (ownerUid.isBlank()) return false
        val updated = itemDao.markItemFound(ownerUid, itemId, System.currentTimeMillis())
        return updated > 0
    }

    /**
     * Enables reminder at [hour]:[minute]; schedules first fire at next occurrence.
     */
    suspend fun setReminder(itemId: Long, hour: Int, minute: Int): Item? {
        val ownerUid = _currentOwnerUid.value
        if (ownerUid.isBlank()) return null
        val nextAt = ReminderTimeUtils.nextWallClockMillis(hour, minute)
        itemDao.updateReminder(
            ownerUid = ownerUid,
            id = itemId,
            enabled = true,
            hour = hour,
            minute = minute,
            nextAt = nextAt,
            active = true
        )
        return getItemByIdOnce(itemId)
    }

    suspend fun snoozeReminder(itemId: Long): Item? {
        val nextAt = System.currentTimeMillis() + ReminderTimeUtils.SNOOZE_INTERVAL_MS
        itemDao.updateReminderSchedule(itemId, nextAt, active = true)
        return getItemByIdOnce(itemId)
    }

    suspend fun stopReminder(itemId: Long): Boolean {
        return itemDao.stopReminder(itemId) > 0
    }

    suspend fun bumpReminderAfterNotify(itemId: Long): Item? {
        val nextAt = System.currentTimeMillis() + ReminderTimeUtils.REPEAT_INTERVAL_MS
        itemDao.updateReminderSchedule(itemId, nextAt, active = true)
        return getItemByIdOnce(itemId)
    }

    suspend fun deleteItem(itemId: Long): Boolean {
        val ownerUid = _currentOwnerUid.value
        if (ownerUid.isBlank()) return false
        return itemDao.deleteItem(ownerUid, itemId) > 0
    }

    /**
     * Removes found items whose [Item.lastFoundAt] is at or before [beforeTimestamp].
     * Returns deleted item ids (for cancelling reminders).
     */
    suspend fun deleteExpiredFoundItems(beforeTimestamp: Long): List<Long> {
        val ownerUid = _currentOwnerUid.value
        if (ownerUid.isBlank()) return emptyList()
        val ids = itemDao.getExpiredFoundItemIds(ownerUid, beforeTimestamp)
        if (ids.isEmpty()) return emptyList()
        itemDao.deleteExpiredFoundItems(ownerUid, beforeTimestamp)
        return ids
    }
}
