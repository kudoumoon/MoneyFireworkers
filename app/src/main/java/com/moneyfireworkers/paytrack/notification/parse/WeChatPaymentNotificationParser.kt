package com.moneyfireworkers.paytrack.notification.parse

import com.moneyfireworkers.paytrack.notification.model.WeChatNotificationPayload
import com.moneyfireworkers.paytrack.notification.model.WeChatParsedNotification
import java.util.Locale

class WeChatPaymentNotificationParser {
    private val amountRegex = Regex("""(?:[¥￥]\s*(\d+(?:[.,]\d{1,2})?)|(\d+(?:[.,]\d{1,2})?)\s*元)""")
    private val merchantRegexes = listOf(
        Regex("""(?:收款方|商户)[:：\s]*([^\n,，:：]{2,24})"""),
        Regex("""(?:向|付款给|支付给)([^\n,，:：]{2,24})"""),
        Regex("""给([^\n,，:：]{2,24})"""),
        Regex("""([^\n,，:：]{2,24})(?:收款|已收款|已到账)"""),
    )
    private val paymentKeywords = listOf("微信支付", "支付", "付款", "收款方", "商户", "已支付", "支付凭证")

    fun parse(payload: WeChatNotificationPayload): WeChatParsedNotification {
        val normalizedText = payload.combinedText
            .replace("\u00A0", " ")
            .replace(Regex("""\s+"""), " ")
            .trim()
        val lowerText = normalizedText.lowercase(Locale.getDefault())
        val isPaymentRelated = paymentKeywords.any { normalizedText.contains(it) } ||
            (normalizedText.contains("¥") || normalizedText.contains("￥")) && lowerText.contains("微信")

        val amountInCent = amountRegex.find(normalizedText)
            ?.destructured
            ?.toList()
            ?.firstOrNull { it.isNotBlank() }
            ?.replace(",", "")
            ?.toBigDecimalOrNull()
            ?.movePointRight(2)
            ?.toLong()

        val merchant = merchantRegexes.firstNotNullOfOrNull { regex ->
            regex.find(normalizedText)?.groupValues?.getOrNull(1)
                ?.trim()
                ?.trim('：', ':')
                ?.takeIf { it.isNotBlank() && !it.contains("微信支付") && !it.contains("人民币") }
        }

        return WeChatParsedNotification(
            isPaymentRelated = isPaymentRelated,
            amountInCent = amountInCent,
            merchantName = merchant,
            occurredAt = payload.postedAt,
            normalizedText = normalizedText,
        )
    }
}
