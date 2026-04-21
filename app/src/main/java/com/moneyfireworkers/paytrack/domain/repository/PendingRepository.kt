package com.moneyfireworkers.paytrack.domain.repository

import com.moneyfireworkers.paytrack.domain.model.PendingAction
import kotlinx.coroutines.flow.Flow

interface PendingRepository {
    suspend fun create(action: PendingAction): Long
    suspend fun update(action: PendingAction)
    suspend fun getById(id: Long): PendingAction?
    suspend fun getActiveActions(): List<PendingAction>
    fun observeById(id: Long): Flow<PendingAction?>
    fun observeActiveActions(): Flow<List<PendingAction>>
}
