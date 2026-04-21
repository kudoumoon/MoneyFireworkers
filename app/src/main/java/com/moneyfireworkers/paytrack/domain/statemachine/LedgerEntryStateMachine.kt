package com.moneyfireworkers.paytrack.domain.statemachine

import com.moneyfireworkers.paytrack.core.model.EntryStatus

class LedgerEntryStateMachine {
    fun canTransition(from: EntryStatus, to: EntryStatus): Boolean {
        return when (from) {
            EntryStatus.DRAFT -> to == EntryStatus.PENDING_CONFIRMATION
            EntryStatus.PENDING_CONFIRMATION -> to in setOf(
                EntryStatus.CONFIRMED_AUTO,
                EntryStatus.CONFIRMED_WITH_EDIT,
                EntryStatus.CANCELLED,
            )
            EntryStatus.CONFIRMED_AUTO, EntryStatus.CONFIRMED_WITH_EDIT, EntryStatus.CANCELLED -> false
        }
    }
}
