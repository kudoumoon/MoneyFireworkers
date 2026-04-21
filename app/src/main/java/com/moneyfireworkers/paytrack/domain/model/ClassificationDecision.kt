package com.moneyfireworkers.paytrack.domain.model

data class ClassificationDecision(
    val matchedRuleId: Long? = null,
    val categoryId: Long? = null,
    val confidence: Int,
    val decisionReason: String,
    val explanation: String,
    val matchedSignals: Map<String, String> = emptyMap(),
)
