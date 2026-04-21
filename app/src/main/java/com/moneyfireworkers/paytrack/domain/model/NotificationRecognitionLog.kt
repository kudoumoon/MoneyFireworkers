package com.moneyfireworkers.paytrack.domain.model

data class NotificationRecognitionLog(
    val id: Long = 0L,
    val packageName: String,
    val title: String? = null,
    val contentText: String,
    val amountInCent: Long? = null,
    val merchantName: String? = null,
    val occurredAt: Long? = null,
    val recognitionStatus: String,
    val paymentEventId: Long? = null,
    val ledgerEntryId: Long? = null,
    val pendingActionId: Long? = null,
    val rawPayload: String,
    val failureReason: String? = null,
    val createdAt: Long,
)
