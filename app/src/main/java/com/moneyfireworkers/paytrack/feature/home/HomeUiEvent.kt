package com.moneyfireworkers.paytrack.feature.home

sealed interface HomeUiEvent {
    data object NavigateToAddPayment : HomeUiEvent

    data object NavigateToRecords : HomeUiEvent

    data class NavigateToPending(
        val pendingActionId: Long,
        val ledgerEntryId: Long,
    ) : HomeUiEvent

    data class NavigateToEntryDetail(val entryId: Long) : HomeUiEvent
}
