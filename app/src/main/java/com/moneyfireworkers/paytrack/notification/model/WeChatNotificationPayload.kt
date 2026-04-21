package com.moneyfireworkers.paytrack.notification.model

data class WeChatNotificationPayload(
    val packageName: String,
    val title: String? = null,
    val text: String? = null,
    val subText: String? = null,
    val bigText: String? = null,
    val postedAt: Long,
) {
    val combinedText: String
        get() = listOfNotNull(title, text, subText, bigText)
            .joinToString(separator = "\n")
            .trim()
}
