package com.moneyfireworkers.paytrack.domain.usecase.parse

import com.moneyfireworkers.paytrack.domain.model.ParsedPayment

class ParseTextPaymentUseCase {
    operator fun invoke(rawText: String, fallbackTime: Long): ParsedPayment {
        val amountRegex = Regex("""(\d+(?:\.\d{1,2})?)""")
        val amountMatch = amountRegex.find(rawText)?.groupValues?.get(1)
        val amountInCent = amountMatch
            ?.toBigDecimalOrNull()
            ?.multiply("100".toBigDecimal())
            ?.toLong()

        val merchant = rawText
            .replace(amountRegex, "")
            .replace("元", "")
            .replace("块", "")
            .replace("支付", "")
            .replace("付款", "")
            .replace("消费", "")
            .trim()
            .ifBlank { null }

        return ParsedPayment(
            amountInCent = amountInCent,
            merchantName = merchant,
            occurredAt = fallbackTime,
        )
    }
}
