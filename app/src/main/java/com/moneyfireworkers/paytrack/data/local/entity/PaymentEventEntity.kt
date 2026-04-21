package com.moneyfireworkers.paytrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_events")
data class PaymentEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val sourceType: String,
    val inputType: String,
    val rawInputText: String? = null,
    val rawInputImageUri: String? = null,
    val amountRaw: String? = null,
    val merchantRaw: String? = null,
    val occurredAt: Long,
    val createdAt: Long,
    val eventStatus: String,
    val parseStatus: String,
    val classificationStatus: String,
    val dedupStatus: String,
    val dedupReferenceEntryId: Long? = null,
    val parsedFrom: String? = null,
    val ocrStatus: String,
    val latestErrorCode: String? = null,
    val latestErrorMessage: String? = null,
)
