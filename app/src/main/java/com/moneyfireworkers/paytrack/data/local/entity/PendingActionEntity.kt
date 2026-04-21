package com.moneyfireworkers.paytrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_actions")
data class PendingActionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val paymentEventId: Long,
    val ledgerEntryId: Long,
    val pendingStatus: String,
    val reminderShownAt: Long? = null,
    val notificationSentAt: Long? = null,
    val expiresAt: Long? = null,
    val lastResumeAt: Long? = null,
)
