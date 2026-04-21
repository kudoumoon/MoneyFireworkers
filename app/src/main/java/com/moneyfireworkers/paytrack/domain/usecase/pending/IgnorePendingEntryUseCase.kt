package com.moneyfireworkers.paytrack.domain.usecase.pending

import com.moneyfireworkers.paytrack.core.model.EntryStatus
import com.moneyfireworkers.paytrack.core.model.EventStatus
import com.moneyfireworkers.paytrack.core.model.PendingStatus
import com.moneyfireworkers.paytrack.domain.repository.LedgerRepository
import com.moneyfireworkers.paytrack.domain.repository.PaymentRepository
import com.moneyfireworkers.paytrack.domain.repository.PendingRepository

class IgnorePendingEntryUseCase(
    private val ledgerRepository: LedgerRepository,
    private val pendingRepository: PendingRepository,
    private val paymentRepository: PaymentRepository,
) {
    suspend operator fun invoke(
        pendingActionId: Long,
        ledgerEntryId: Long,
        now: Long,
    ) {
        val pendingAction = requireNotNull(pendingRepository.getById(pendingActionId)) {
            "Pending action $pendingActionId was not found."
        }
        val ledgerEntry = requireNotNull(ledgerRepository.getById(ledgerEntryId)) {
            "Ledger entry $ledgerEntryId was not found."
        }
        ledgerRepository.update(
            ledgerEntry.copy(
                entryStatus = EntryStatus.CANCELLED,
                updatedAt = now,
            ),
        )
        pendingRepository.update(
            pendingAction.copy(
                pendingStatus = PendingStatus.EXPIRED,
                lastResumeAt = now,
            ),
        )
        paymentRepository.getById(pendingAction.paymentEventId)?.let { event ->
            paymentRepository.update(
                event.copy(
                    eventStatus = EventStatus.CANCELLED,
                    latestErrorCode = "IGNORED_BY_USER",
                    latestErrorMessage = "Candidate was ignored from the notification confirmation card.",
                ),
            )
        }
    }
}
