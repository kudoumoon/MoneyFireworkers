package com.moneyfireworkers.paytrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.moneyfireworkers.paytrack.data.local.entity.NotificationRecognitionLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationRecognitionLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: NotificationRecognitionLogEntity): Long

    @Query("SELECT * FROM notification_recognition_logs ORDER BY createdAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<NotificationRecognitionLogEntity>>
}
