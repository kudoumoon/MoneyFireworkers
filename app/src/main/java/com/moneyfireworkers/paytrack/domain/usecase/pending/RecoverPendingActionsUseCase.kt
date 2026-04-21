package com.moneyfireworkers.paytrack.domain.usecase.pending

import com.moneyfireworkers.paytrack.domain.model.PendingAction
import com.moneyfireworkers.paytrack.domain.repository.PendingRepository

class RecoverPendingActionsUseCase(
    private val pendingRepository: PendingRepository,
) {
    suspend operator fun invoke(): List<PendingAction> {
        return pendingRepository.getActiveActions()
            .sortedByDescending { it.id }
    }
}
