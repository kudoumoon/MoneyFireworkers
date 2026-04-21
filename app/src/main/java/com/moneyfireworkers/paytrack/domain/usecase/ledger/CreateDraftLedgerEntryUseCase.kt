package com.moneyfireworkers.paytrack.domain.usecase.ledger

import com.moneyfireworkers.paytrack.domain.model.ClassificationDecision
import com.moneyfireworkers.paytrack.domain.model.LedgerEntry
import com.moneyfireworkers.paytrack.domain.model.ParsedPayment

class CreateDraftLedgerEntryUseCase {
    operator fun invoke(
        paymentEventId: Long,
        parsedPayment: ParsedPayment,
        decision: ClassificationDecision,
        now: Long,
    ): LedgerEntry {
        return LedgerEntry(
            paymentEventId = paymentEventId,
            amountInCent = parsedPayment.amountInCent ?: 0L,
            merchantName = parsedPayment.merchantName.orEmpty(),
            categoryIdSuggested = decision.categoryId,
            categoryIdFinal = decision.categoryId,
            classificationConfidence = decision.confidence,
            classificationExplanationSnapshot = decision.explanation,
            note = parsedPayment.note,
            occurredAt = parsedPayment.occurredAt ?: now,
            createdAt = now,
            updatedAt = now,
        )
    }
}
