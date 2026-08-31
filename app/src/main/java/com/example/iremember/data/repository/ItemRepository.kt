package com.example.iremember.data.repository

import com.example.iremember.data.local.dao.ItemDao
import com.example.iremember.data.local.dao.ItemHistoryDao
import com.example.iremember.data.local.entity.toEntity
import com.example.iremember.data.local.entity.toItem
import com.example.iremember.data.local.entity.toItemHistory
import com.example.iremember.model.HistoryAction
import com.example.iremember.model.Item
import com.example.iremember.model.ItemHistory
import com.example.iremember.util.ReminderTimeUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class ItemRepository(
    private val itemDao: ItemDao,
    private val historyDao: ItemHistoryDao
) {

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

    val historyEntries: Flow<List<ItemHistory>> = _currentOwnerUid.flatMapLatest { ownerUid ->
        if (ownerUid.isBlank()) {
            flowOf(emptyList())
        } else {
            historyDao.observeAll(ownerUid).map { entities ->
                entities.map { it.toItemHistory() }
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

    suspend fun updateItem(item: Item): Boolean {
        val ownerUid = _currentOwnerUid.value
        if (ownerUid.isBlank() || item.id <= 0L) return false
        if (item.ownerUid.isNotBlank() && item.ownerUid != ownerUid) return false
        val updated = itemDao.updateItem(item.copy(ownerUid = ownerUid).toEntity())
        if (updated > 0) {
            insertHistory(
                itemId = item.id,
                itemName = item.name,
                action = HistoryAction.EDITED,
                detail = item.location.ifBlank { "Updated item details" }
            )
        }
        return updated > 0
    }

    suspend fun markItemFound(itemId: Long): Boolean {
        val ownerUid = _currentOwnerUid.value
        if (ownerUid.isBlank()) return false
        val existing = getItemByIdOnce(itemId)
        val updated = itemDao.markItemFound(ownerUid, itemId, System.currentTimeMillis())
        if (updated > 0 && existing != null) {
            insertHistory(
                itemId = itemId,
                itemName = existing.name,
                action = HistoryAction.FOUND,
                detail = existing.location.ifBlank { "Marked as found" }
            )
        }
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
        val existing = getItemByIdOnce(itemId) ?: return false
        if (existing.deletedAt > 0L) return false
        val now = System.currentTimeMillis()
        val softDeleted = itemDao.softDeleteItem(ownerUid, itemId, now) > 0
        if (softDeleted) {
            insertHistory(
                itemId = itemId,
                itemName = existing.name,
                action = HistoryAction.DELETED,
                detail = existing.location.ifBlank { "Item removed" }
            )
        }
        return softDeleted
    }

    suspend fun restoreItem(itemId: Long): Boolean {
        val ownerUid = _currentOwnerUid.value
        if (ownerUid.isBlank()) return false
        val existing = getItemByIdOnce(itemId) ?: return false
        if (existing.deletedAt == 0L) return false
        val restored = itemDao.restoreItem(ownerUid, itemId) > 0
        if (restored) {
            insertHistory(
                itemId = itemId,
                itemName = existing.name,
                action = HistoryAction.EDITED,
                detail = "Restored from History"
            )
        }
        return restored
    }

    /**
     * Permanently removes soft-deleted items older than [beforeTimestamp].
     * Returns purged item ids (for cancelling reminders / cleanup).
     */
    suspend fun purgeSoftDeletedItems(beforeTimestamp: Long): List<Long> {
        val ownerUid = _currentOwnerUid.value
        if (ownerUid.isBlank()) return emptyList()
        val ids = itemDao.getSoftDeletedIdsForPurge(ownerUid, beforeTimestamp)
        if (ids.isEmpty()) return emptyList()
        itemDao.purgeSoftDeletedItems(ownerUid, beforeTimestamp)
        return ids
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

    suspend fun purgeExpiredHistory(beforeTimestamp: Long) {
        val ownerUid = _currentOwnerUid.value
        if (ownerUid.isBlank()) return
        historyDao.deleteOlderThan(ownerUid, beforeTimestamp)
    }

    suspend fun deleteHistoryEntry(id: Long): Boolean {
        val ownerUid = _currentOwnerUid.value
        if (ownerUid.isBlank()) return false
        return historyDao.deleteById(ownerUid, id) > 0
    }

    suspend fun clearHistory(): Boolean {
        val ownerUid = _currentOwnerUid.value
        if (ownerUid.isBlank()) return false
        return historyDao.deleteAll(ownerUid) > 0
    }

    private suspend fun insertHistory(
        itemId: Long,
        itemName: String,
        action: HistoryAction,
        detail: String
    ) {
        val ownerUid = _currentOwnerUid.value
        if (ownerUid.isBlank()) return
        historyDao.insert(
            ItemHistory(
                ownerUid = ownerUid,
                itemId = itemId,
                itemName = itemName,
                action = action,
                detail = detail
            ).toEntity()
        )
    }
}
