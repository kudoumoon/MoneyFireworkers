package com.moneyfireworkers.paytrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_recognition_logs")
data class NotificationRecognitionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
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
