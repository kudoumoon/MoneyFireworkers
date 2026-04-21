package com.moneyfireworkers.paytrack.data.repository

import com.moneyfireworkers.paytrack.data.local.dao.PaymentEventDao
import com.moneyfireworkers.paytrack.data.local.mapper.PaymentEventMapper
import com.moneyfireworkers.paytrack.domain.model.PaymentEvent
import com.moneyfireworkers.paytrack.domain.repository.PaymentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PaymentRepositoryImpl(
    private val paymentEventDao: PaymentEventDao,
) : PaymentRepository {
    override suspend fun create(event: PaymentEvent): Long {
        return paymentEventDao.insert(PaymentEventMapper.toEntity(event))
    }

    override suspend fun update(event: PaymentEvent) {
        paymentEventDao.update(PaymentEventMapper.toEntity(event))
    }

    override suspend fun getById(id: Long): PaymentEvent? {
        return paymentEventDao.getById(id)?.let(PaymentEventMapper::fromEntity)
    }

    override fun observeById(id: Long): Flow<PaymentEvent?> {
        return paymentEventDao.observeById(id)
            .map { entity -> entity?.let(PaymentEventMapper::fromEntity) }
    }
}
