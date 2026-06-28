package com.example.findit.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.findit.data.local.entity.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items ORDER BY dateCreated DESC")
    fun getAllItems(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE id = :id")
    fun getItemById(id: Long): Flow<ItemEntity?>

    @Query(
        """
        SELECT * FROM items
        WHERE name LIKE '%' || :query || '%'
           OR location LIKE '%' || :query || '%'
           OR category LIKE '%' || :query || '%'
           OR notes LIKE '%' || :query || '%'
        ORDER BY dateCreated DESC
        """
    )
    fun searchItems(query: String): Flow<List<ItemEntity>>

    @Query("SELECT COUNT(*) FROM items")
    suspend fun getItemCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ItemEntity>)
}
