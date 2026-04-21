package com.moneyfireworkers.paytrack.domain.usecase.input

import com.moneyfireworkers.paytrack.core.model.InputType
import com.moneyfireworkers.paytrack.domain.model.PaymentEvent

class CreateFormPaymentEventUseCase {
    operator fun invoke(amountRaw: String, merchantRaw: String, occurredAt: Long, now: Long): PaymentEvent {
        return PaymentEvent(
            inputType = InputType.FORM,
            amountRaw = amountRaw,
            merchantRaw = merchantRaw,
            occurredAt = occurredAt,
            createdAt = now,
            parsedFrom = InputType.FORM,
        )
    }
}
