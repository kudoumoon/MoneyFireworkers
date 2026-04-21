package com.moneyfireworkers.paytrack.domain.usecase.pending

import com.moneyfireworkers.paytrack.core.model.EventStatus
import com.moneyfireworkers.paytrack.domain.model.LedgerEntry
import com.moneyfireworkers.paytrack.domain.repository.LedgerRepository
import com.moneyfireworkers.paytrack.domain.repository.PaymentRepository
import com.moneyfireworkers.paytrack.domain.repository.PendingRepository
import com.moneyfireworkers.paytrack.domain.usecase.ledger.ConfirmLedgerEntryWithEditUseCase

class ConfirmPendingEntryWithEditUseCase(
    private val ledgerRepository: LedgerRepository,
    private val pendingRepository: PendingRepository,
    private val paymentRepository: PaymentRepository,
    private val confirmLedgerEntryWithEditUseCase: ConfirmLedgerEntryWithEditUseCase = ConfirmLedgerEntryWithEditUseCase(),
    private val resolvePendingActionUseCase: ResolvePendingActionUseCase = ResolvePendingActionUseCase(),
) {
    suspend operator fun invoke(
        pendingActionId: Long,
        ledgerEntryId: Long,
        finalCategoryId: Long,
        now: Long,
    ): LedgerEntry {
        val pendingAction = requireNotNull(pendingRepository.getById(pendingActionId)) {
            "Pending action $pendingActionId was not found."
        }
        val ledgerEntry = requireNotNull(ledgerRepository.getById(ledgerEntryId)) {
            "Ledger entry $ledgerEntryId was not found."
        }
        val confirmedEntry = confirmLedgerEntryWithEditUseCase(
            entry = ledgerEntry,
            finalCategoryId = finalCategoryId,
            note = ledgerEntry.note,
            now = now,
        )
        ledgerRepository.update(confirmedEntry)
        pendingRepository.update(resolvePendingActionUseCase(pendingAction, now))
        paymentRepository.getById(pendingAction.paymentEventId)?.let { event ->
            paymentRepository.update(event.copy(eventStatus = EventStatus.FINALIZED))
        }
        return confirmedEntry
    }
}
