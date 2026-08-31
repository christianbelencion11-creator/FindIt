package com.example.iremember.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.iremember.data.local.entity.BankCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BankCardDao {
    @Query(
        """
        SELECT * FROM bank_cards
        WHERE ownerUid = :ownerUid
        ORDER BY dateUpdated DESC
        """
    )
    fun observeAll(ownerUid: String): Flow<List<BankCardEntity>>

    @Query("SELECT * FROM bank_cards WHERE ownerUid = :ownerUid AND id = :id LIMIT 1")
    fun observeById(ownerUid: String, id: Long): Flow<BankCardEntity?>

    @Query("SELECT * FROM bank_cards WHERE id = :id LIMIT 1")
    suspend fun getByIdOnce(id: Long): BankCardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: BankCardEntity): Long

    @Update
    suspend fun update(card: BankCardEntity): Int

    @Query("DELETE FROM bank_cards WHERE ownerUid = :ownerUid AND id = :id")
    suspend fun delete(ownerUid: String, id: Long): Int
}
