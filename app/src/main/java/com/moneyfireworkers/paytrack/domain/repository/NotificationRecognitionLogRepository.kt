package com.moneyfireworkers.paytrack.domain.repository

import com.moneyfireworkers.paytrack.domain.model.NotificationRecognitionLog
import kotlinx.coroutines.flow.Flow

interface NotificationRecognitionLogRepository {
    suspend fun create(log: NotificationRecognitionLog): Long
    fun observeRecent(limit: Int): Flow<List<NotificationRecognitionLog>>
}
