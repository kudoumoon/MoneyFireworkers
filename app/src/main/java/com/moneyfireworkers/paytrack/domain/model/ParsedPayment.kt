package com.moneyfireworkers.paytrack.domain.model

data class ParsedPayment(
    val amountInCent: Long? = null,
    val merchantName: String? = null,
    val occurredAt: Long? = null,
    val note: String? = null,
)
