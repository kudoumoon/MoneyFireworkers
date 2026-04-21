package com.moneyfireworkers.paytrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.moneyfireworkers.paytrack.data.local.entity.LedgerEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LedgerEntryEntity): Long

    @Update
    suspend fun update(entity: LedgerEntryEntity)

    @Query("SELECT * FROM ledger_entries WHERE id = :id")
    suspend fun getById(id: Long): LedgerEntryEntity?

    @Query("SELECT * FROM ledger_entries WHERE id = :id")
    fun observeById(id: Long): Flow<LedgerEntryEntity?>

    @Query("SELECT * FROM ledger_entries ORDER BY occurredAt DESC")
    fun observeAll(): Flow<List<LedgerEntryEntity>>

    @Query("SELECT * FROM ledger_entries ORDER BY occurredAt DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<LedgerEntryEntity>

    @Query("SELECT * FROM ledger_entries ORDER BY occurredAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<LedgerEntryEntity>>
}
