package com.moneyfireworkers.paytrack.data.local.mapper

import com.moneyfireworkers.paytrack.core.model.ClassificationStatus
import com.moneyfireworkers.paytrack.core.model.DedupStatus
import com.moneyfireworkers.paytrack.core.model.EventStatus
import com.moneyfireworkers.paytrack.core.model.InputType
import com.moneyfireworkers.paytrack.core.model.OcrStatus
import com.moneyfireworkers.paytrack.core.model.ParseStatus
import com.moneyfireworkers.paytrack.core.model.SourceType
import com.moneyfireworkers.paytrack.data.local.entity.PaymentEventEntity
import com.moneyfireworkers.paytrack.domain.model.PaymentEvent

object PaymentEventMapper {
    fun toEntity(model: PaymentEvent): PaymentEventEntity = PaymentEventEntity(
        id = model.id,
        sourceType = model.sourceType.name,
        inputType = model.inputType.name,
        rawInputText = model.rawInputText,
        rawInputImageUri = model.rawInputImageUri,
        amountRaw = model.amountRaw,
        merchantRaw = model.merchantRaw,
        occurredAt = model.occurredAt,
        createdAt = model.createdAt,
        eventStatus = model.eventStatus.name,
        parseStatus = model.parseStatus.name,
        classificationStatus = model.classificationStatus.name,
        dedupStatus = model.dedupStatus.name,
        dedupReferenceEntryId = model.dedupReferenceEntryId,
        parsedFrom = model.parsedFrom?.name,
        ocrStatus = model.ocrStatus.name,
        latestErrorCode = model.latestErrorCode,
        latestErrorMessage = model.latestErrorMessage,
    )

    fun fromEntity(entity: PaymentEventEntity): PaymentEvent = PaymentEvent(
        id = entity.id,
        sourceType = SourceType.valueOf(entity.sourceType),
        inputType = InputType.valueOf(entity.inputType),
        rawInputText = entity.rawInputText,
        rawInputImageUri = entity.rawInputImageUri,
        amountRaw = entity.amountRaw,
        merchantRaw = entity.merchantRaw,
        occurredAt = entity.occurredAt,
        createdAt = entity.createdAt,
        eventStatus = EventStatus.valueOf(entity.eventStatus),
        parseStatus = ParseStatus.valueOf(entity.parseStatus),
        classificationStatus = ClassificationStatus.valueOf(entity.classificationStatus),
        dedupStatus = DedupStatus.valueOf(entity.dedupStatus),
        dedupReferenceEntryId = entity.dedupReferenceEntryId,
        parsedFrom = entity.parsedFrom?.let(InputType::valueOf),
        ocrStatus = OcrStatus.valueOf(entity.ocrStatus),
        latestErrorCode = entity.latestErrorCode,
        latestErrorMessage = entity.latestErrorMessage,
    )
}
