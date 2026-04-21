package com.moneyfireworkers.paytrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.moneyfireworkers.paytrack.data.local.entity.PendingActionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingActionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PendingActionEntity): Long

    @Update
    suspend fun update(entity: PendingActionEntity)

    @Query("SELECT * FROM pending_actions WHERE id = :id")
    suspend fun getById(id: Long): PendingActionEntity?

    @Query("SELECT * FROM pending_actions WHERE id = :id")
    fun observeById(id: Long): Flow<PendingActionEntity?>

    @Query("SELECT * FROM pending_actions WHERE pendingStatus NOT IN ('RESOLVED', 'EXPIRED')")
    suspend fun getActiveActions(): List<PendingActionEntity>

    @Query("SELECT * FROM pending_actions WHERE pendingStatus NOT IN ('RESOLVED', 'EXPIRED')")
    fun observeActiveActions(): Flow<List<PendingActionEntity>>
}
