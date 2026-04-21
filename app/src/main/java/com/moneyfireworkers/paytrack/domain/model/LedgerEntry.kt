package com.moneyfireworkers.paytrack.domain.model

import com.moneyfireworkers.paytrack.core.model.EntryStatus
import com.moneyfireworkers.paytrack.core.model.UserActionType

data class LedgerEntry(
    val id: Long = 0L,
    val paymentEventId: Long,
    val amountInCent: Long,
    val merchantName: String,
    val categoryIdSuggested: Long? = null,
    val categoryIdFinal: Long? = null,
    val classificationConfidence: Int = 0,
    val classificationExplanationSnapshot: String? = null,
    val note: String? = null,
    val emotion: SpendingEmotion? = null,
    val occurredAt: Long,
    val entryStatus: EntryStatus = EntryStatus.DRAFT,
    val userActionType: UserActionType = UserActionType.NONE,
    val confirmedAt: Long? = null,
    val createdAt: Long,
    val updatedAt: Long,
)
