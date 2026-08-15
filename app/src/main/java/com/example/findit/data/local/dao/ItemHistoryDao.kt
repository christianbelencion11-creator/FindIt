package com.example.findit.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.findit.data.local.entity.ItemHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ItemHistoryEntity): Long

    @Query(
        """
        SELECT * FROM item_history
        WHERE ownerUid = :ownerUid
        ORDER BY createdAt DESC
        """
    )
    fun observeAll(ownerUid: String): Flow<List<ItemHistoryEntity>>

    @Query(
        """
        DELETE FROM item_history
        WHERE ownerUid = :ownerUid AND createdAt < :beforeTimestamp
        """
    )
    suspend fun deleteOlderThan(ownerUid: String, beforeTimestamp: Long): Int

    @Query("DELETE FROM item_history WHERE ownerUid = :ownerUid AND id = :id")
    suspend fun deleteById(ownerUid: String, id: Long): Int

    @Query("DELETE FROM item_history WHERE ownerUid = :ownerUid")
    suspend fun deleteAll(ownerUid: String): Int
}
