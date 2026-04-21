package com.moneyfireworkers.paytrack.domain.usecase.pending

import com.moneyfireworkers.paytrack.core.model.PendingStatus
import com.moneyfireworkers.paytrack.domain.model.PendingAction

class ResolvePendingActionUseCase {
    operator fun invoke(action: PendingAction, now: Long): PendingAction {
        return action.copy(
            pendingStatus = PendingStatus.RESOLVED,
            lastResumeAt = now,
        )
    }
}
