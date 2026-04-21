package com.moneyfireworkers.paytrack.domain.repository

import com.moneyfireworkers.paytrack.domain.model.PaymentEvent
import kotlinx.coroutines.flow.Flow

interface PaymentRepository {
    suspend fun create(event: PaymentEvent): Long
    suspend fun update(event: PaymentEvent)
    suspend fun getById(id: Long): PaymentEvent?
    fun observeById(id: Long): Flow<PaymentEvent?>
}
