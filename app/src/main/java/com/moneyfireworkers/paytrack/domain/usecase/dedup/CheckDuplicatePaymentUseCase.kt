package com.moneyfireworkers.paytrack.domain.usecase.dedup

import com.moneyfireworkers.paytrack.core.model.DedupStatus
import com.moneyfireworkers.paytrack.dedup.evaluator.DuplicateEvaluator
import com.moneyfireworkers.paytrack.domain.model.LedgerEntry
import com.moneyfireworkers.paytrack.domain.model.ParsedPayment

class CheckDuplicatePaymentUseCase(
    private val evaluator: DuplicateEvaluator = DuplicateEvaluator(),
) {
    operator fun invoke(parsedPayment: ParsedPayment, existingEntries: List<LedgerEntry>): DedupStatus {
        return evaluator.evaluate(parsedPayment, existingEntries)
    }
}
