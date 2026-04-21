package com.moneyfireworkers.paytrack.data.local.mapper

import com.moneyfireworkers.paytrack.core.model.EntryStatus
import com.moneyfireworkers.paytrack.core.model.UserActionType
import com.moneyfireworkers.paytrack.data.local.entity.LedgerEntryEntity
import com.moneyfireworkers.paytrack.domain.model.LedgerEntry
import com.moneyfireworkers.paytrack.domain.model.SpendingEmotion

object LedgerEntryMapper {
    fun toEntity(model: LedgerEntry): LedgerEntryEntity = LedgerEntryEntity(
        id = model.id,
        paymentEventId = model.paymentEventId,
        amountInCent = model.amountInCent,
        merchantName = model.merchantName,
        categoryIdSuggested = model.categoryIdSuggested,
        categoryIdFinal = model.categoryIdFinal,
        classificationConfidence = model.classificationConfidence,
        classificationExplanationSnapshot = model.classificationExplanationSnapshot,
        note = model.note,
        emotion = model.emotion?.name,
        occurredAt = model.occurredAt,
        entryStatus = model.entryStatus.name,
        userActionType = model.userActionType.name,
        confirmedAt = model.confirmedAt,
        createdAt = model.createdAt,
        updatedAt = model.updatedAt,
    )

    fun fromEntity(entity: LedgerEntryEntity): LedgerEntry = LedgerEntry(
        id = entity.id,
        paymentEventId = entity.paymentEventId,
        amountInCent = entity.amountInCent,
        merchantName = entity.merchantName,
        categoryIdSuggested = entity.categoryIdSuggested,
        categoryIdFinal = entity.categoryIdFinal,
        classificationConfidence = entity.classificationConfidence,
        classificationExplanationSnapshot = entity.classificationExplanationSnapshot,
        note = entity.note,
        emotion = entity.emotion?.let(SpendingEmotion::valueOf),
        occurredAt = entity.occurredAt,
        entryStatus = EntryStatus.valueOf(entity.entryStatus),
        userActionType = UserActionType.valueOf(entity.userActionType),
        confirmedAt = entity.confirmedAt,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt,
    )
}
