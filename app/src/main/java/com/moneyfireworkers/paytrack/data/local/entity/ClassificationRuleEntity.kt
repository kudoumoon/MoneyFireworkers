package com.moneyfireworkers.paytrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "classification_rules")
data class ClassificationRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val merchantKeyword: String,
    val merchantNormalizedKeyword: String,
    val matchType: String,
    val amountMinInCent: Long? = null,
    val amountMaxInCent: Long? = null,
    val targetCategoryId: Long,
    val priority: Int,
    val explanationTemplate: String,
    val isEnabled: Boolean,
    val version: Int,
    val createdAt: Long,
    val updatedAt: Long,
)
