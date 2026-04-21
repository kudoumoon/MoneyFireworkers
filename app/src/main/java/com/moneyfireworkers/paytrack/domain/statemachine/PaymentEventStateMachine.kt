package com.moneyfireworkers.paytrack.domain.statemachine

import com.moneyfireworkers.paytrack.core.model.EventStatus

class PaymentEventStateMachine {
    fun canTransition(from: EventStatus, to: EventStatus): Boolean {
        return when (from) {
            EventStatus.RECEIVED -> to in setOf(EventStatus.PARSED, EventStatus.PARSE_FAILED, EventStatus.ERROR)
            EventStatus.PARSED -> to in setOf(EventStatus.DEDUP_CHECKED, EventStatus.DUPLICATE_REJECTED, EventStatus.ERROR)
            EventStatus.PARSE_FAILED -> to in setOf(EventStatus.CANCELLED, EventStatus.ERROR)
            EventStatus.DEDUP_CHECKED -> to in setOf(EventStatus.CLASSIFIED, EventStatus.CLASSIFY_FALLBACK, EventStatus.ERROR)
            EventStatus.DUPLICATE_REJECTED -> to == EventStatus.CANCELLED
            EventStatus.CLASSIFIED, EventStatus.CLASSIFY_FALLBACK -> to in setOf(EventStatus.LEDGER_DRAFT_CREATED, EventStatus.ERROR)
            EventStatus.LEDGER_DRAFT_CREATED -> to in setOf(EventStatus.PENDING_USER_ACTION, EventStatus.ERROR)
            EventStatus.PENDING_USER_ACTION -> to in setOf(EventStatus.FINALIZED, EventStatus.CANCELLED, EventStatus.ERROR)
            EventStatus.FINALIZED, EventStatus.CANCELLED, EventStatus.ERROR -> false
        }
    }
}
