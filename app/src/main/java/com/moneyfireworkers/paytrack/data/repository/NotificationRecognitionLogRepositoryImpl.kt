package com.moneyfireworkers.paytrack.data.repository

import com.moneyfireworkers.paytrack.data.local.dao.NotificationRecognitionLogDao
import com.moneyfireworkers.paytrack.data.local.mapper.NotificationRecognitionLogMapper
import com.moneyfireworkers.paytrack.domain.model.NotificationRecognitionLog
import com.moneyfireworkers.paytrack.domain.repository.NotificationRecognitionLogRepository
import kotlinx.coroutines.flow.map

class NotificationRecognitionLogRepositoryImpl(
    private val logDao: NotificationRecognitionLogDao,
) : NotificationRecognitionLogRepository {
    override suspend fun create(log: NotificationRecognitionLog): Long {
        return logDao.insert(NotificationRecognitionLogMapper.toEntity(log))
    }

    override fun observeRecent(limit: Int) = logDao.observeRecent(limit).map { logs ->
        logs.map(NotificationRecognitionLogMapper::fromEntity)
    }
}
