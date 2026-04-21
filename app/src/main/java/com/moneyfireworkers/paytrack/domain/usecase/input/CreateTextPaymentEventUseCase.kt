package com.moneyfireworkers.paytrack.domain.usecase.input

import com.moneyfireworkers.paytrack.core.model.InputType
import com.moneyfireworkers.paytrack.domain.model.PaymentEvent

class CreateTextPaymentEventUseCase {
    operator fun invoke(text: String, now: Long): PaymentEvent {
        return PaymentEvent(
            inputType = InputType.TEXT,
            rawInputText = text,
            occurredAt = now,
            createdAt = now,
            parsedFrom = InputType.TEXT,
        )
    }
}
