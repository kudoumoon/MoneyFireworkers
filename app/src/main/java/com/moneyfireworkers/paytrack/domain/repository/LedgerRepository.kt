package com.moneyfireworkers.paytrack.domain.repository

import com.moneyfireworkers.paytrack.domain.model.LedgerEntry
import kotlinx.coroutines.flow.Flow

interface LedgerRepository {
    suspend fun create(entry: LedgerEntry): Long
    suspend fun update(entry: LedgerEntry)
    suspend fun getById(id: Long): LedgerEntry?
    fun observeById(id: Long): Flow<LedgerEntry?>
    fun observeAll(): Flow<List<LedgerEntry>>
    suspend fun getRecent(limit: Int): List<LedgerEntry>
    fun observeRecent(limit: Int): Flow<List<LedgerEntry>>
}
