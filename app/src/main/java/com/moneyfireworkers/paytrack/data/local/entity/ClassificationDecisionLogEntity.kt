package com.moneyfireworkers.paytrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "classification_decision_logs")
data class ClassificationDecisionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val paymentEventId: Long,
    val ledgerEntryId: Long,
    val matchedRuleId: Long? = null,
    val matchedSignalsJson: String,
    val decisionReason: String,
    val confidence: Int,
    val createdAt: Long,
)
