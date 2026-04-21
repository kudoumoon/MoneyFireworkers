package com.moneyfireworkers.paytrack.domain.usecase.process

import com.moneyfireworkers.paytrack.core.model.ClassificationStatus
import com.moneyfireworkers.paytrack.core.model.EventStatus
import com.moneyfireworkers.paytrack.core.model.ParseStatus
import com.moneyfireworkers.paytrack.domain.model.ClassificationDecision
import com.moneyfireworkers.paytrack.domain.model.ParsedPayment
import com.moneyfireworkers.paytrack.domain.model.ProcessPaymentResult
import com.moneyfireworkers.paytrack.domain.repository.PaymentRepository
import com.moneyfireworkers.paytrack.domain.usecase.input.CreateImagePaymentEventUseCase

class ProcessImagePaymentUseCase(
    private val paymentRepository: PaymentRepository,
    private val createImagePaymentEventUseCase: CreateImagePaymentEventUseCase = CreateImagePaymentEventUseCase(),
) {
    suspend operator fun invoke(imageUri: String, now: Long): ProcessPaymentResult {
        val draftEvent = createImagePaymentEventUseCase(imageUri, now)
        val eventId = paymentRepository.create(draftEvent)
        val event = draftEvent.copy(
            id = eventId,
            eventStatus = EventStatus.PENDING_USER_ACTION,
            parseStatus = ParseStatus.NOT_STARTED,
            classificationStatus = ClassificationStatus.NOT_STARTED,
        )
        paymentRepository.update(event)

        return ProcessPaymentResult(
            paymentEvent = event,
            parsedPayment = ParsedPayment(occurredAt = now),
            dedupStatus = com.moneyfireworkers.paytrack.core.model.DedupStatus.NOT_STARTED,
            classificationDecision = ClassificationDecision(
                categoryId = null,
                confidence = 0,
                decisionReason = "IMAGE_PENDING_MANUAL_CONFIRM",
                explanation = "Image input is stored first and waits for manual confirmation in v1.",
            ),
            ledgerEntry = null,
            pendingAction = null,
        )
    }
}
