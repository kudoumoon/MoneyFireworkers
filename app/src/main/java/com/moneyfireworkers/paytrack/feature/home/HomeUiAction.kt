package com.moneyfireworkers.paytrack.feature.home

sealed interface HomeUiAction {
    data object Refresh : HomeUiAction

    data object AddPaymentClicked : HomeUiAction

    data object ViewAllClicked : HomeUiAction

    data object PendingClicked : HomeUiAction

    data class RecentSpendingClicked(val entryId: Long) : HomeUiAction
}
