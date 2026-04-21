package com.moneyfireworkers.paytrack.domain.usecase.process

import com.moneyfireworkers.paytrack.core.model.ClassificationStatus
import com.moneyfireworkers.paytrack.core.model.DedupStatus
import com.moneyfireworkers.paytrack.core.model.EntryStatus
import com.moneyfireworkers.paytrack.core.model.EventStatus
import com.moneyfireworkers.paytrack.core.model.ParseStatus
import com.moneyfireworkers.paytrack.domain.model.ProcessPaymentResult
import com.moneyfireworkers.paytrack.domain.repository.ClassificationRepository
import com.moneyfireworkers.paytrack.domain.repository.LedgerRepository
import com.moneyfireworkers.paytrack.domain.repository.PaymentRepository
import com.moneyfireworkers.paytrack.domain.repository.PendingRepository
import com.moneyfireworkers.paytrack.domain.usecase.classify.ClassifyPaymentUseCase
import com.moneyfireworkers.paytrack.domain.usecase.dedup.CheckDuplicatePaymentUseCase
import com.moneyfireworkers.paytrack.domain.usecase.input.CreateTextPaymentEventUseCase
import com.moneyfireworkers.paytrack.domain.usecase.ledger.CreateDraftLedgerEntryUseCase
import com.moneyfireworkers.paytrack.domain.usecase.parse.ParseTextPaymentUseCase
import com.moneyfireworkers.paytrack.domain.usecase.pending.CreatePendingActionUseCase

class ProcessTextPaymentUseCase(
    private val paymentRepository: PaymentRepository,
    private val ledgerRepository: LedgerRepository,
    private val pendingRepository: PendingRepository,
    private val classificationRepository: ClassificationRepository,
    private val createTextPaymentEventUseCase: CreateTextPaymentEventUseCase = CreateTextPaymentEventUseCase(),
    private val parseTextPaymentUseCase: ParseTextPaymentUseCase = ParseTextPaymentUseCase(),
    private val checkDuplicatePaymentUseCase: CheckDuplicatePaymentUseCase = CheckDuplicatePaymentUseCase(),
    private val classifyPaymentUseCase: ClassifyPaymentUseCase = ClassifyPaymentUseCase(),
    private val createDraftLedgerEntryUseCase: CreateDraftLedgerEntryUseCase = CreateDraftLedgerEntryUseCase(),
    private val createPendingActionUseCase: CreatePendingActionUseCase = CreatePendingActionUseCase(),
) {
    suspend operator fun invoke(rawText: String, now: Long): ProcessPaymentResult {
        val draftEvent = createTextPaymentEventUseCase(rawText, now)
        val eventId = paymentRepository.create(draftEvent)

        val parsed = parseTextPaymentUseCase(rawText, now)
        val parsedEvent = draftEvent.copy(
            id = eventId,
            eventStatus = if (parsed.amountInCent != null || parsed.merchantName != null) {
                EventStatus.PARSED
            } else {
                EventStatus.PARSE_FAILED
            },
            parseStatus = if (parsed.amountInCent != null || parsed.merchantName != null) {
                ParseStatus.SUCCESS
            } else {
                ParseStatus.FAILED
            },
        )
        paymentRepository.update(parsedEvent)

        val recentEntries = ledgerRepository.getRecent(limit = 20)
        val dedupStatus = checkDuplicatePaymentUseCase(parsed, recentEntries)

        if (dedupStatus == DedupStatus.CONFIRMED_DUPLICATE) {
            val duplicateEvent = parsedEvent.copy(
                eventStatus = EventStatus.DUPLICATE_REJECTED,
                dedupStatus = dedupStatus,
            )
            paymentRepository.update(duplicateEvent)
            return ProcessPaymentResult(
                paymentEvent = duplicateEvent,
                parsedPayment = parsed,
                dedupStatus = dedupStatus,
                classificationDecision = classifyPaymentUseCase(parsed, emptyList()),
                ledgerEntry = null,
                pendingAction = null,
            )
        }

        val rules = classificationRepository.getAllEnabledRules()
        val decision = classifyPaymentUseCase(parsed, rules)
        val classifiedEvent = parsedEvent.copy(
            eventStatus = if (decision.categoryId != null) EventStatus.CLASSIFIED else EventStatus.CLASSIFY_FALLBACK,
            classificationStatus = if (decision.categoryId != null) {
                ClassificationStatus.RULE_MATCHED
            } else {
                ClassificationStatus.FALLBACK
            },
            dedupStatus = dedupStatus,
        )
        paymentRepository.update(classifiedEvent)

        val draftEntry = createDraftLedgerEntryUseCase(
            paymentEventId = eventId,
            parsedPayment = parsed,
            decision = decision,
            now = now,
        )
        val ledgerId = ledgerRepository.create(draftEntry)
        val pendingEntry = draftEntry.copy(
            id = ledgerId,
            entryStatus = EntryStatus.PENDING_CONFIRMATION,
            updatedAt = now,
        )
        ledgerRepository.update(pendingEntry)

        val pending = createPendingActionUseCase(paymentEventId = eventId, ledgerEntryId = ledgerId)
        val pendingId = pendingRepository.create(pending)
        val persistedPending = pending.copy(id = pendingId)

        val finalEvent = classifiedEvent.copy(eventStatus = EventStatus.PENDING_USER_ACTION)
        paymentRepository.update(finalEvent)

        return ProcessPaymentResult(
            paymentEvent = finalEvent,
            parsedPayment = parsed,
            dedupStatus = dedupStatus,
            classificationDecision = decision,
            ledgerEntry = pendingEntry,
            pendingAction = persistedPending,
        )
    }
}
