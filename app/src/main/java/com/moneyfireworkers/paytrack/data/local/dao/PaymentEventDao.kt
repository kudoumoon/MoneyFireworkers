package com.moneyfireworkers.paytrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.moneyfireworkers.paytrack.data.local.entity.PaymentEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PaymentEventEntity): Long

    @Update
    suspend fun update(entity: PaymentEventEntity)

    @Query("SELECT * FROM payment_events WHERE id = :id")
    suspend fun getById(id: Long): PaymentEventEntity?

    @Query("SELECT * FROM payment_events WHERE id = :id")
    fun observeById(id: Long): Flow<PaymentEventEntity?>
}
