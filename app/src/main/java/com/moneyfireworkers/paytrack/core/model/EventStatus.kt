package com.moneyfireworkers.paytrack.core.model

enum class EventStatus {
    RECEIVED,
    PARSED,
    PARSE_FAILED,
    DEDUP_CHECKED,
    DUPLICATE_REJECTED,
    CLASSIFIED,
    CLASSIFY_FALLBACK,
    LEDGER_DRAFT_CREATED,
    PENDING_USER_ACTION,
    FINALIZED,
    CANCELLED,
    ERROR,
}
