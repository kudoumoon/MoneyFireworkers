package com.moneyfireworkers.paytrack.dedup.evaluator

import com.moneyfireworkers.paytrack.core.model.DedupStatus
import com.moneyfireworkers.paytrack.domain.model.LedgerEntry
import com.moneyfireworkers.paytrack.domain.model.ParsedPayment
import kotlin.math.abs

class DuplicateEvaluator {
    fun evaluate(parsedPayment: ParsedPayment, existingEntries: List<LedgerEntry>): DedupStatus {
        val amount = parsedPayment.amountInCent ?: return DedupStatus.NONE
        val occurredAt = parsedPayment.occurredAt ?: return DedupStatus.NONE
        val merchant = parsedPayment.merchantName.orEmpty()

        val exactMatch = existingEntries.any {
            it.amountInCent == amount &&
                it.merchantName == merchant &&
                abs(it.occurredAt - occurredAt) <= TWO_MINUTES_MS
        }

        if (exactMatch) return DedupStatus.CONFIRMED_DUPLICATE

        val suspected = existingEntries.any {
            it.amountInCent == amount && abs(it.occurredAt - occurredAt) <= FIVE_MINUTES_MS
        }

        return if (suspected) DedupStatus.SUSPECTED else DedupStatus.NONE
    }

    private companion object {
        const val TWO_MINUTES_MS = 2 * 60 * 1000L
        const val FIVE_MINUTES_MS = 5 * 60 * 1000L
    }
}
