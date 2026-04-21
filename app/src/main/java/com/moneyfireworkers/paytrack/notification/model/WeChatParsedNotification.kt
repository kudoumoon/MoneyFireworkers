package com.moneyfireworkers.paytrack.notification.model

data class WeChatParsedNotification(
    val isPaymentRelated: Boolean,
    val amountInCent: Long? = null,
    val merchantName: String? = null,
    val occurredAt: Long,
    val normalizedText: String,
)
