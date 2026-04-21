package com.moneyfireworkers.paytrack.domain.model

import com.moneyfireworkers.paytrack.core.model.MatchType

data class ClassificationRule(
    val id: Long = 0L,
    val name: String,
    val merchantKeyword: String,
    val merchantNormalizedKeyword: String,
    val matchType: MatchType,
    val amountMinInCent: Long? = null,
    val amountMaxInCent: Long? = null,
    val targetCategoryId: Long,
    val priority: Int = 0,
    val explanationTemplate: String,
    val isEnabled: Boolean = true,
    val version: Int = 1,
    val createdAt: Long,
    val updatedAt: Long,
)
