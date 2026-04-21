package com.moneyfireworkers.paytrack.domain.model

import com.moneyfireworkers.paytrack.core.model.PendingStatus

data class PendingAction(
    val id: Long = 0L,
    val paymentEventId: Long,
    val ledgerEntryId: Long,
    val pendingStatus: PendingStatus = PendingStatus.ACTIVE,
    val reminderShownAt: Long? = null,
    val notificationSentAt: Long? = null,
    val expiresAt: Long? = null,
    val lastResumeAt: Long? = null,
)
