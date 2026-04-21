package com.moneyfireworkers.paytrack.domain.usecase.expense

import com.moneyfireworkers.paytrack.core.model.ClassificationStatus
import com.moneyfireworkers.paytrack.core.model.EntryStatus
import com.moneyfireworkers.paytrack.core.model.EventStatus
import com.moneyfireworkers.paytrack.core.model.ParseStatus
import com.moneyfireworkers.paytrack.core.model.UserActionType
import com.moneyfireworkers.paytrack.domain.model.LedgerEntry
import com.moneyfireworkers.paytrack.domain.model.SpendingEmotion
import com.moneyfireworkers.paytrack.domain.repository.CategoryRepository
import com.moneyfireworkers.paytrack.domain.repository.LedgerRepository
import com.moneyfireworkers.paytrack.domain.repository.PaymentRepository
import com.moneyfireworkers.paytrack.domain.usecase.input.CreateFormPaymentEventUseCase

class RecordExpenseUseCase(
    private val paymentRepository: PaymentRepository,
    private val ledgerRepository: LedgerRepository,
    private val categoryRepository: CategoryRepository,
    private val createFormPaymentEventUseCase: CreateFormPaymentEventUseCase = CreateFormPaymentEventUseCase(),
) {
    suspend operator fun invoke(
        amountInCent: Long,
        categoryId: Long,
        emotion: SpendingEmotion,
        note: String?,
        occurredAt: Long,
        now: Long,
    ): Long {
        require(amountInCent > 0) { "金额必须大于 0" }

        val category = requireNotNull(categoryRepository.getById(categoryId)) {
            "所选分类不存在"
        }
        val merchantName = note?.trim().takeUnless { it.isNullOrBlank() } ?: category.name

        val paymentEvent = createFormPaymentEventUseCase(
            amountRaw = amountInCent.toString(),
            merchantRaw = merchantName,
            occurredAt = occurredAt,
            now = now,
        ).copy(
            eventStatus = EventStatus.FINALIZED,
            parseStatus = ParseStatus.USER_COMPLETED,
            classificationStatus = ClassificationStatus.FALLBACK,
        )
        val paymentEventId = paymentRepository.create(paymentEvent)

        val ledgerEntry = LedgerEntry(
            paymentEventId = paymentEventId,
            amountInCent = amountInCent,
            merchantName = merchantName,
            categoryIdSuggested = categoryId,
            categoryIdFinal = categoryId,
            classificationConfidence = 100,
            classificationExplanationSnapshot = "用户手动选择分类并直接记账。",
            note = note?.trim().takeUnless { it.isNullOrEmpty() },
            emotion = emotion,
            occurredAt = occurredAt,
            entryStatus = EntryStatus.CONFIRMED_WITH_EDIT,
            userActionType = UserActionType.CONFIRM_WITH_EDIT,
            confirmedAt = now,
            createdAt = now,
            updatedAt = now,
        )
        return ledgerRepository.create(ledgerEntry)
    }
}
