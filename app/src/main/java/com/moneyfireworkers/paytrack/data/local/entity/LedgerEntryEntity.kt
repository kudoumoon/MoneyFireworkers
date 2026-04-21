package com.moneyfireworkers.paytrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ledger_entries")
data class LedgerEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val paymentEventId: Long,
    val amountInCent: Long,
    val merchantName: String,
    val categoryIdSuggested: Long? = null,
    val categoryIdFinal: Long? = null,
    val classificationConfidence: Int,
    val classificationExplanationSnapshot: String? = null,
    val note: String? = null,
    val emotion: String? = null,
    val occurredAt: Long,
    val entryStatus: String,
    val userActionType: String,
    val confirmedAt: Long? = null,
    val createdAt: Long,
    val updatedAt: Long,
)
