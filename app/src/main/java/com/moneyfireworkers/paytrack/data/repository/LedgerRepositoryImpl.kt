package com.moneyfireworkers.paytrack.data.repository

import com.moneyfireworkers.paytrack.data.local.dao.LedgerEntryDao
import com.moneyfireworkers.paytrack.data.local.mapper.LedgerEntryMapper
import com.moneyfireworkers.paytrack.domain.model.LedgerEntry
import com.moneyfireworkers.paytrack.domain.repository.LedgerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LedgerRepositoryImpl(
    private val ledgerEntryDao: LedgerEntryDao,
) : LedgerRepository {
    override suspend fun create(entry: LedgerEntry): Long {
        return ledgerEntryDao.insert(LedgerEntryMapper.toEntity(entry))
    }

    override suspend fun update(entry: LedgerEntry) {
        ledgerEntryDao.update(LedgerEntryMapper.toEntity(entry))
    }

    override suspend fun getById(id: Long): LedgerEntry? {
        return ledgerEntryDao.getById(id)?.let(LedgerEntryMapper::fromEntity)
    }

    override fun observeById(id: Long): Flow<LedgerEntry?> {
        return ledgerEntryDao.observeById(id)
            .map { entity -> entity?.let(LedgerEntryMapper::fromEntity) }
    }

    override fun observeAll(): Flow<List<LedgerEntry>> {
        return ledgerEntryDao.observeAll()
            .map { entries -> entries.map(LedgerEntryMapper::fromEntity) }
    }

    override suspend fun getRecent(limit: Int): List<LedgerEntry> {
        return ledgerEntryDao.getRecent(limit).map(LedgerEntryMapper::fromEntity)
    }

    override fun observeRecent(limit: Int): Flow<List<LedgerEntry>> {
        return ledgerEntryDao.observeRecent(limit)
            .map { entries -> entries.map(LedgerEntryMapper::fromEntity) }
    }
}
