package com.moneyfireworkers.paytrack.data.repository

import com.moneyfireworkers.paytrack.data.local.dao.PendingActionDao
import com.moneyfireworkers.paytrack.data.local.mapper.PendingActionMapper
import com.moneyfireworkers.paytrack.domain.model.PendingAction
import com.moneyfireworkers.paytrack.domain.repository.PendingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PendingRepositoryImpl(
    private val pendingActionDao: PendingActionDao,
) : PendingRepository {
    override suspend fun create(action: PendingAction): Long {
        return pendingActionDao.insert(PendingActionMapper.toEntity(action))
    }

    override suspend fun update(action: PendingAction) {
        pendingActionDao.update(PendingActionMapper.toEntity(action))
    }

    override suspend fun getById(id: Long): PendingAction? {
        return pendingActionDao.getById(id)?.let(PendingActionMapper::fromEntity)
    }

    override suspend fun getActiveActions(): List<PendingAction> {
        return pendingActionDao.getActiveActions().map(PendingActionMapper::fromEntity)
    }

    override fun observeById(id: Long): Flow<PendingAction?> {
        return pendingActionDao.observeById(id)
            .map { entity -> entity?.let(PendingActionMapper::fromEntity) }
    }

    override fun observeActiveActions(): Flow<List<PendingAction>> {
        return pendingActionDao.observeActiveActions()
            .map { actions -> actions.map(PendingActionMapper::fromEntity) }
    }
}
