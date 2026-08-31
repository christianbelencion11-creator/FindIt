package com.example.iremember.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.iremember.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query(
        """
        SELECT * FROM notes
        WHERE ownerUid = :ownerUid
        ORDER BY pinned DESC, dateUpdated DESC
        """
    )
    fun observeAll(ownerUid: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE ownerUid = :ownerUid AND id = :id LIMIT 1")
    fun observeById(ownerUid: String, id: Long): Flow<NoteEntity?>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getByIdOnce(id: Long): NoteEntity?

    @Query("SELECT * FROM notes WHERE remindEnabled = 1 AND remindAt > 0")
    suspend fun getAllWithReminders(): List<NoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity): Long

    @Update
    suspend fun update(note: NoteEntity): Int

    @Query("DELETE FROM notes WHERE ownerUid = :ownerUid AND id = :id")
    suspend fun delete(ownerUid: String, id: Long): Int
}
