package com.moneyfireworkers.paytrack.domain.statemachine

import com.moneyfireworkers.paytrack.core.model.PendingStatus

class PendingActionStateMachine {
    fun canTransition(from: PendingStatus, to: PendingStatus): Boolean {
        return when (from) {
            PendingStatus.ACTIVE -> to in setOf(PendingStatus.SHOWN_IN_APP, PendingStatus.NOTIFIED, PendingStatus.RESOLVED, PendingStatus.EXPIRED)
            PendingStatus.SHOWN_IN_APP -> to in setOf(PendingStatus.RESUMED, PendingStatus.RESOLVED, PendingStatus.EXPIRED)
            PendingStatus.NOTIFIED -> to in setOf(PendingStatus.RESUMED, PendingStatus.RESOLVED, PendingStatus.EXPIRED)
            PendingStatus.RESUMED -> to in setOf(PendingStatus.RESOLVED, PendingStatus.EXPIRED)
            PendingStatus.RESOLVED, PendingStatus.EXPIRED -> false
        }
    }
}
