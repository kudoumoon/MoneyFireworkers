package com.moneyfireworkers.paytrack.domain.usecase.input

import com.moneyfireworkers.paytrack.core.model.InputType
import com.moneyfireworkers.paytrack.core.model.OcrStatus
import com.moneyfireworkers.paytrack.domain.model.PaymentEvent

class CreateImagePaymentEventUseCase {
    operator fun invoke(imageUri: String, now: Long): PaymentEvent {
        return PaymentEvent(
            inputType = InputType.IMAGE,
            rawInputImageUri = imageUri,
            occurredAt = now,
            createdAt = now,
            parsedFrom = InputType.IMAGE,
            ocrStatus = OcrStatus.NOT_STARTED,
        )
    }
}
