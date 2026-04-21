package com.moneyfireworkers.paytrack.domain.usecase.notification

import android.util.Log
import com.moneyfireworkers.paytrack.core.model.ClassificationStatus
import com.moneyfireworkers.paytrack.core.model.DedupStatus
import com.moneyfireworkers.paytrack.core.model.EntryStatus
import com.moneyfireworkers.paytrack.core.model.EventStatus
import com.moneyfireworkers.paytrack.core.model.InputType
import com.moneyfireworkers.paytrack.core.model.ParseStatus
import com.moneyfireworkers.paytrack.core.model.SourceType
import com.moneyfireworkers.paytrack.domain.model.NotificationCandidateCard
import com.moneyfireworkers.paytrack.domain.model.NotificationRecognitionLog
import com.moneyfireworkers.paytrack.domain.model.ParsedPayment
import com.moneyfireworkers.paytrack.domain.model.PaymentEvent
import com.moneyfireworkers.paytrack.domain.repository.ClassificationRepository
import com.moneyfireworkers.paytrack.domain.repository.LedgerRepository
import com.moneyfireworkers.paytrack.domain.repository.NotificationRecognitionLogRepository
import com.moneyfireworkers.paytrack.domain.repository.PaymentRepository
import com.moneyfireworkers.paytrack.domain.repository.PendingRepository
import com.moneyfireworkers.paytrack.domain.usecase.classify.ClassifyPaymentUseCase
import com.moneyfireworkers.paytrack.domain.usecase.dedup.CheckDuplicatePaymentUseCase
import com.moneyfireworkers.paytrack.domain.usecase.ledger.CreateDraftLedgerEntryUseCase
import com.moneyfireworkers.paytrack.domain.usecase.pending.CreatePendingActionUseCase
import com.moneyfireworkers.paytrack.notification.model.WeChatNotificationPayload
import com.moneyfireworkers.paytrack.notification.parse.WeChatPaymentNotificationParser

class ProcessWeChatNotificationUseCase(
    private val paymentRepository: PaymentRepository,
    private val ledgerRepository: LedgerRepository,
    private val pendingRepository: PendingRepository,
    private val classificationRepository: ClassificationRepository,
    private val logRepository: NotificationRecognitionLogRepository,
    private val parser: WeChatPaymentNotificationParser = WeChatPaymentNotificationParser(),
    private val classifyPaymentUseCase: ClassifyPaymentUseCase = ClassifyPaymentUseCase(),
    private val checkDuplicatePaymentUseCase: CheckDuplicatePaymentUseCase = CheckDuplicatePaymentUseCase(),
    private val createDraftLedgerEntryUseCase: CreateDraftLedgerEntryUseCase = CreateDraftLedgerEntryUseCase(),
    private val createPendingActionUseCase: CreatePendingActionUseCase = CreatePendingActionUseCase(),
) {
    suspend operator fun invoke(payload: WeChatNotificationPayload): NotificationCandidateCard? {
        val now = System.currentTimeMillis()
        val parsedNotification = parser.parse(payload)
        Log.d(TAG, "Parsed notification: payment=${parsedNotification.isPaymentRelated}, amount=${parsedNotification.amountInCent}, merchant=${parsedNotification.merchantName}")

        if (!parsedNotification.isPaymentRelated) {
            logRepository.create(
                NotificationRecognitionLog(
                    packageName = payload.packageName,
                    title = payload.title,
                    contentText = parsedNotification.normalizedText,
                    recognitionStatus = "IGNORED_NOT_PAYMENT",
                    rawPayload = buildRawPayload(payload),
                    createdAt = now,
                ),
            )
            return null
        }

        if (parsedNotification.amountInCent == null || parsedNotification.merchantName.isNullOrBlank()) {
            logRepository.create(
                NotificationRecognitionLog(
                    packageName = payload.packageName,
                    title = payload.title,
                    contentText = parsedNotification.normalizedText,
                    amountInCent = parsedNotification.amountInCent,
                    merchantName = parsedNotification.merchantName,
                    occurredAt = parsedNotification.occurredAt,
                    recognitionStatus = "PARSE_FAILED_MANUAL_FALLBACK",
                    rawPayload = buildRawPayload(payload),
                    failureReason = "Amount or merchant name could not be confidently extracted.",
                    createdAt = now,
                ),
            )
            return NotificationCandidateCard(
                amountInCent = parsedNotification.amountInCent,
                merchantName = parsedNotification.merchantName,
                explanation = "没有完整识别出金额或商户，建议转为手动补录。",
                rawText = parsedNotification.normalizedText,
                sourcePackage = payload.packageName,
                occurredAt = parsedNotification.occurredAt,
                manualFallbackRequired = true,
            )
        }

        val parsedPayment = ParsedPayment(
            amountInCent = parsedNotification.amountInCent,
            merchantName = parsedNotification.merchantName,
            occurredAt = parsedNotification.occurredAt,
            note = payload.title,
        )
        val duplicateStatus = checkDuplicatePaymentUseCase(parsedPayment, ledgerRepository.getRecent(limit = 20))
        if (duplicateStatus == DedupStatus.CONFIRMED_DUPLICATE) {
            logRepository.create(
                NotificationRecognitionLog(
                    packageName = payload.packageName,
                    title = payload.title,
                    contentText = parsedNotification.normalizedText,
                    amountInCent = parsedNotification.amountInCent,
                    merchantName = parsedNotification.merchantName,
                    occurredAt = parsedNotification.occurredAt,
                    recognitionStatus = "DUPLICATE_REJECTED",
                    rawPayload = buildRawPayload(payload),
                    failureReason = "Matched duplicate policy against recent expenses.",
                    createdAt = now,
                ),
            )
            return null
        }

        val draftEvent = PaymentEvent(
            sourceType = SourceType.WECHAT_NOTIFICATION,
            inputType = InputType.TEXT,
            rawInputText = parsedNotification.normalizedText,
            amountRaw = parsedNotification.amountInCent.toString(),
            merchantRaw = parsedNotification.merchantName,
            occurredAt = parsedNotification.occurredAt,
            createdAt = now,
            eventStatus = EventStatus.PARSED,
            parseStatus = ParseStatus.SUCCESS,
            dedupStatus = duplicateStatus,
            parsedFrom = InputType.TEXT,
        )
        val eventId = paymentRepository.create(draftEvent)

        val rules = classificationRepository.getAllEnabledRules()
        val decision = classifyPaymentUseCase(parsedPayment, rules)
        paymentRepository.update(
            draftEvent.copy(
                id = eventId,
                eventStatus = if (decision.categoryId != null) EventStatus.CLASSIFIED else EventStatus.CLASSIFY_FALLBACK,
                classificationStatus = if (decision.categoryId != null) ClassificationStatus.RULE_MATCHED else ClassificationStatus.FALLBACK,
            ),
        )

        val draftEntry = createDraftLedgerEntryUseCase(
            paymentEventId = eventId,
            parsedPayment = parsedPayment,
            decision = decision,
            now = now,
        )
        val ledgerEntryId = ledgerRepository.create(draftEntry)
        val pendingEntry = draftEntry.copy(
            id = ledgerEntryId,
            entryStatus = EntryStatus.PENDING_CONFIRMATION,
            updatedAt = now,
        )
        ledgerRepository.update(pendingEntry)

        val pendingAction = createPendingActionUseCase(paymentEventId = eventId, ledgerEntryId = ledgerEntryId)
        val pendingActionId = pendingRepository.create(pendingAction)
        paymentRepository.update(
            draftEvent.copy(
                id = eventId,
                eventStatus = EventStatus.PENDING_USER_ACTION,
                classificationStatus = if (decision.categoryId != null) ClassificationStatus.RULE_MATCHED else ClassificationStatus.FALLBACK,
                dedupStatus = duplicateStatus,
            ),
        )

        logRepository.create(
            NotificationRecognitionLog(
                packageName = payload.packageName,
                title = payload.title,
                contentText = parsedNotification.normalizedText,
                amountInCent = parsedNotification.amountInCent,
                merchantName = parsedNotification.merchantName,
                occurredAt = parsedNotification.occurredAt,
                recognitionStatus = "CANDIDATE_CREATED",
                paymentEventId = eventId,
                ledgerEntryId = ledgerEntryId,
                pendingActionId = pendingActionId,
                rawPayload = buildRawPayload(payload),
                createdAt = now,
            ),
        )

        return NotificationCandidateCard(
            pendingActionId = pendingActionId,
            ledgerEntryId = ledgerEntryId,
            amountInCent = parsedNotification.amountInCent,
            merchantName = parsedNotification.merchantName,
            suggestedCategoryId = decision.categoryId,
            explanation = decision.explanation,
            rawText = parsedNotification.normalizedText,
            sourcePackage = payload.packageName,
            occurredAt = parsedNotification.occurredAt,
            manualFallbackRequired = false,
        )
    }

    private fun buildRawPayload(payload: WeChatNotificationPayload): String {
        return listOf(
            "package=${payload.packageName}",
            "title=${payload.title.orEmpty()}",
            "text=${payload.text.orEmpty()}",
            "subText=${payload.subText.orEmpty()}",
            "bigText=${payload.bigText.orEmpty()}",
            "postedAt=${payload.postedAt}",
        ).joinToString(separator = "\n")
    }

    private companion object {
        const val TAG = "MFW-WeChatFlow"
    }
}
