package com.moneyfireworkers.paytrack.domain.usecase.ledger

import com.moneyfireworkers.paytrack.core.model.EntryStatus
import com.moneyfireworkers.paytrack.core.model.UserActionType
import com.moneyfireworkers.paytrack.domain.model.LedgerEntry

class ConfirmLedgerEntryUseCase {
    operator fun invoke(entry: LedgerEntry, now: Long): LedgerEntry {
        return entry.copy(
            entryStatus = EntryStatus.CONFIRMED_AUTO,
            userActionType = UserActionType.AUTO_CONFIRM,
            confirmedAt = now,
            updatedAt = now,
        )
    }
}
