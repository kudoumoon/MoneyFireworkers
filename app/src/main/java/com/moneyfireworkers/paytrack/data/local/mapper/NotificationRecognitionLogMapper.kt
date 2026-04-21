package com.moneyfireworkers.paytrack.data.local.mapper

import com.moneyfireworkers.paytrack.data.local.entity.NotificationRecognitionLogEntity
import com.moneyfireworkers.paytrack.domain.model.NotificationRecognitionLog

object NotificationRecognitionLogMapper {
    fun toEntity(model: NotificationRecognitionLog): NotificationRecognitionLogEntity = NotificationRecognitionLogEntity(
        id = model.id,
        packageName = model.packageName,
        title = model.title,
        contentText = model.contentText,
        amountInCent = model.amountInCent,
        merchantName = model.merchantName,
        occurredAt = model.occurredAt,
        recognitionStatus = model.recognitionStatus,
        paymentEventId = model.paymentEventId,
        ledgerEntryId = model.ledgerEntryId,
        pendingActionId = model.pendingActionId,
        rawPayload = model.rawPayload,
        failureReason = model.failureReason,
        createdAt = model.createdAt,
    )

    fun fromEntity(entity: NotificationRecognitionLogEntity): NotificationRecognitionLog = NotificationRecognitionLog(
        id = entity.id,
        packageName = entity.packageName,
        title = entity.title,
        contentText = entity.contentText,
        amountInCent = entity.amountInCent,
        merchantName = entity.merchantName,
        occurredAt = entity.occurredAt,
        recognitionStatus = entity.recognitionStatus,
        paymentEventId = entity.paymentEventId,
        ledgerEntryId = entity.ledgerEntryId,
        pendingActionId = entity.pendingActionId,
        rawPayload = entity.rawPayload,
        failureReason = entity.failureReason,
        createdAt = entity.createdAt,
    )
}
