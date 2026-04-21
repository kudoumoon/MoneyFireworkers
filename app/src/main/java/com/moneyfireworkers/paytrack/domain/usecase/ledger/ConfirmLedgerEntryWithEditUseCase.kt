package com.moneyfireworkers.paytrack.domain.usecase.ledger

import com.moneyfireworkers.paytrack.core.model.EntryStatus
import com.moneyfireworkers.paytrack.core.model.UserActionType
import com.moneyfireworkers.paytrack.domain.model.LedgerEntry

class ConfirmLedgerEntryWithEditUseCase {
    operator fun invoke(entry: LedgerEntry, finalCategoryId: Long, note: String?, now: Long): LedgerEntry {
        return entry.copy(
            categoryIdFinal = finalCategoryId,
            note = note ?: entry.note,
            entryStatus = EntryStatus.CONFIRMED_WITH_EDIT,
            userActionType = UserActionType.CONFIRM_WITH_EDIT,
            confirmedAt = now,
            updatedAt = now,
        )
    }
}
