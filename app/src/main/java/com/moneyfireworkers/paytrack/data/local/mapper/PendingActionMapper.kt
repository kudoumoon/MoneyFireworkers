package com.moneyfireworkers.paytrack.data.local.mapper

import com.moneyfireworkers.paytrack.core.model.PendingStatus
import com.moneyfireworkers.paytrack.data.local.entity.PendingActionEntity
import com.moneyfireworkers.paytrack.domain.model.PendingAction

object PendingActionMapper {
    fun toEntity(model: PendingAction): PendingActionEntity = PendingActionEntity(
        id = model.id,
        paymentEventId = model.paymentEventId,
        ledgerEntryId = model.ledgerEntryId,
        pendingStatus = model.pendingStatus.name,
        reminderShownAt = model.reminderShownAt,
        notificationSentAt = model.notificationSentAt,
        expiresAt = model.expiresAt,
        lastResumeAt = model.lastResumeAt,
    )

    fun fromEntity(entity: PendingActionEntity): PendingAction = PendingAction(
        id = entity.id,
        paymentEventId = entity.paymentEventId,
        ledgerEntryId = entity.ledgerEntryId,
        pendingStatus = PendingStatus.valueOf(entity.pendingStatus),
        reminderShownAt = entity.reminderShownAt,
        notificationSentAt = entity.notificationSentAt,
        expiresAt = entity.expiresAt,
        lastResumeAt = entity.lastResumeAt,
    )
}
