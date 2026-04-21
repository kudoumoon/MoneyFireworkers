package com.moneyfireworkers.paytrack.domain.model

import com.moneyfireworkers.paytrack.core.model.DedupStatus

data class ProcessPaymentResult(
    val paymentEvent: PaymentEvent,
    val parsedPayment: ParsedPayment,
    val dedupStatus: DedupStatus,
    val classificationDecision: ClassificationDecision,
    val ledgerEntry: LedgerEntry?,
    val pendingAction: PendingAction?,
)
