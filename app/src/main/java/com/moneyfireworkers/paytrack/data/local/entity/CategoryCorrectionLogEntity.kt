package com.moneyfireworkers.paytrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_correction_logs")
data class CategoryCorrectionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val ledgerEntryId: Long,
    val merchantName: String,
    val fromCategoryId: Long,
    val toCategoryId: Long,
    val reasonOptional: String? = null,
    val createdAt: Long,
)
