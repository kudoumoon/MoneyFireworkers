package com.moneyfireworkers.paytrack.domain.usecase.pending

import com.moneyfireworkers.paytrack.domain.model.PendingAction

class CreatePendingActionUseCase {
    operator fun invoke(paymentEventId: Long, ledgerEntryId: Long): PendingAction {
        return PendingAction(
            paymentEventId = paymentEventId,
            ledgerEntryId = ledgerEntryId,
        )
    }
}
