package com.moneyfireworkers.paytrack.domain.usecase.parse

import com.moneyfireworkers.paytrack.domain.model.ParsedPayment

class ValidateFormPaymentUseCase {
    operator fun invoke(amountRaw: String, merchantRaw: String, occurredAt: Long): ParsedPayment {
        require(amountRaw.isNotBlank()) { "Amount is required." }
        require(merchantRaw.isNotBlank()) { "Merchant is required." }

        val amountInCent = amountRaw.toBigDecimal().multiply("100".toBigDecimal()).toLong()
        require(amountInCent > 0) { "Amount must be greater than zero." }

        return ParsedPayment(
            amountInCent = amountInCent,
            merchantName = merchantRaw.trim(),
            occurredAt = occurredAt,
        )
    }
}
