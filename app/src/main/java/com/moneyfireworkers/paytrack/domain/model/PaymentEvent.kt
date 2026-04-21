package com.moneyfireworkers.paytrack.domain.model

import com.moneyfireworkers.paytrack.core.model.ClassificationStatus
import com.moneyfireworkers.paytrack.core.model.DedupStatus
import com.moneyfireworkers.paytrack.core.model.EventStatus
import com.moneyfireworkers.paytrack.core.model.InputType
import com.moneyfireworkers.paytrack.core.model.OcrStatus
import com.moneyfireworkers.paytrack.core.model.ParseStatus
import com.moneyfireworkers.paytrack.core.model.SourceType

data class PaymentEvent(
    val id: Long = 0L,
    val sourceType: SourceType = SourceType.MANUAL,
    val inputType: InputType,
    val rawInputText: String? = null,
    val rawInputImageUri: String? = null,
    val amountRaw: String? = null,
    val merchantRaw: String? = null,
    val occurredAt: Long,
    val createdAt: Long,
    val eventStatus: EventStatus = EventStatus.RECEIVED,
    val parseStatus: ParseStatus = ParseStatus.NOT_STARTED,
    val classificationStatus: ClassificationStatus = ClassificationStatus.NOT_STARTED,
    val dedupStatus: DedupStatus = DedupStatus.NOT_STARTED,
    val dedupReferenceEntryId: Long? = null,
    val parsedFrom: InputType? = null,
    val ocrStatus: OcrStatus = OcrStatus.NOT_STARTED,
    val latestErrorCode: String? = null,
    val latestErrorMessage: String? = null,
)
