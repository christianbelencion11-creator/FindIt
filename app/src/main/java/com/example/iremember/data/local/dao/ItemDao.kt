package com.example.iremember.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.iremember.data.local.entity.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query(
        """
        SELECT * FROM items
        WHERE ownerUid = :ownerUid AND deletedAt = 0
        ORDER BY dateCreated DESC
        """
    )
    fun getAllItems(ownerUid: String): Flow<List<ItemEntity>>

    @Query(
        """
        SELECT * FROM items
        WHERE ownerUid = :ownerUid AND id = :id AND deletedAt = 0
        """
    )
    fun getItemById(ownerUid: String, id: Long): Flow<ItemEntity?>

    @Query("SELECT * FROM items WHERE id = :id LIMIT 1")
    suspend fun getItemByIdOnce(id: Long): ItemEntity?

    @Query(
        """
        SELECT * FROM items
        WHERE ownerUid = :ownerUid
          AND deletedAt = 0
          AND (name LIKE '%' || :query || '%'
           OR location LIKE '%' || :query || '%'
           OR category LIKE '%' || :query || '%'
           OR notes LIKE '%' || :query || '%')
        ORDER BY dateCreated DESC
        """
    )
    fun searchItems(ownerUid: String, query: String): Flow<List<ItemEntity>>

    @Query(
        """
        SELECT * FROM items
        WHERE ownerUid = :ownerUid AND remindActive = 1 AND deletedAt = 0
        ORDER BY remindNextAt ASC
        """
    )
    fun getActiveReminders(ownerUid: String): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE remindActive = 1 AND deletedAt = 0")
    suspend fun getAllActiveReminders(): List<ItemEntity>

    @Query("SELECT COUNT(*) FROM items WHERE ownerUid = :ownerUid AND deletedAt = 0")
    suspend fun getItemCount(ownerUid: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity): Long

    @Update
    suspend fun updateItem(item: ItemEntity): Int

    @Query(
        """
        UPDATE items SET lastFoundAt = :timestamp
        WHERE ownerUid = :ownerUid AND id = :id AND lastFoundAt = 0 AND deletedAt = 0
        """
    )
    suspend fun markItemFound(ownerUid: String, id: Long, timestamp: Long): Int

    @Query(
        """
        UPDATE items SET
          remindEnabled = :enabled,
          remindHour = :hour,
          remindMinute = :minute,
          remindNextAt = :nextAt,
          remindActive = :active
        WHERE ownerUid = :ownerUid AND id = :id AND deletedAt = 0
        """
    )
    suspend fun updateReminder(
        ownerUid: String,
        id: Long,
        enabled: Boolean,
        hour: Int,
        minute: Int,
        nextAt: Long,
        active: Boolean
    ): Int

    @Query(
        """
        UPDATE items SET remindNextAt = :nextAt, remindActive = :active
        WHERE id = :id
        """
    )
    suspend fun updateReminderSchedule(id: Long, nextAt: Long, active: Boolean): Int

    @Query(
        """
        UPDATE items SET remindActive = 0, remindEnabled = 0, remindNextAt = 0
        WHERE id = :id
        """
    )
    suspend fun stopReminder(id: Long): Int

    @Query(
        """
        UPDATE items SET deletedAt = :deletedAt, remindActive = 0, remindEnabled = 0, remindNextAt = 0
        WHERE ownerUid = :ownerUid AND id = :id AND deletedAt = 0
        """
    )
    suspend fun softDeleteItem(ownerUid: String, id: Long, deletedAt: Long): Int

    @Query(
        """
        UPDATE items SET deletedAt = 0
        WHERE ownerUid = :ownerUid AND id = :id AND deletedAt > 0
        """
    )
    suspend fun restoreItem(ownerUid: String, id: Long): Int

    @Query("DELETE FROM items WHERE ownerUid = :ownerUid AND id = :id")
    suspend fun deleteItem(ownerUid: String, id: Long): Int

    @Query(
        """
        DELETE FROM items
        WHERE ownerUid = :ownerUid
          AND deletedAt > 0
          AND deletedAt <= :beforeTimestamp
        """
    )
    suspend fun purgeSoftDeletedItems(ownerUid: String, beforeTimestamp: Long): Int

    @Query(
        """
        SELECT id FROM items
        WHERE ownerUid = :ownerUid
          AND deletedAt > 0
          AND deletedAt <= :beforeTimestamp
        """
    )
    suspend fun getSoftDeletedIdsForPurge(ownerUid: String, beforeTimestamp: Long): List<Long>

    @Query(
        """
        DELETE FROM items
        WHERE ownerUid = :ownerUid
          AND deletedAt = 0
          AND lastFoundAt > 0
          AND lastFoundAt <= :beforeTimestamp
        """
    )
    suspend fun deleteExpiredFoundItems(ownerUid: String, beforeTimestamp: Long): Int

    @Query(
        """
        SELECT id FROM items
        WHERE ownerUid = :ownerUid
          AND deletedAt = 0
          AND lastFoundAt > 0
          AND lastFoundAt <= :beforeTimestamp
        """
    )
    suspend fun getExpiredFoundItemIds(ownerUid: String, beforeTimestamp: Long): List<Long>
}
