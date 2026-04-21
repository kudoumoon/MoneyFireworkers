package com.moneyfireworkers.paytrack.domain.model

data class NotificationCandidateCard(
    val pendingActionId: Long? = null,
    val ledgerEntryId: Long? = null,
    val amountInCent: Long? = null,
    val merchantName: String? = null,
    val suggestedCategoryId: Long? = null,
    val suggestedCategoryName: String? = null,
    val explanation: String? = null,
    val rawText: String,
    val sourcePackage: String,
    val occurredAt: Long,
    val manualFallbackRequired: Boolean = false,
)
