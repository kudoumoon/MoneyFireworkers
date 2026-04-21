package com.moneyfireworkers.paytrack.app

sealed class AppDestination(val route: String) {
    data object Home : AppDestination("home")
    data object AddPayment : AppDestination("add_payment")
    data object Records : AppDestination("records")
    data object EntryDetail : AppDestination("entry_detail/{entryId}") {
        fun createRoute(entryId: Long): String = "entry_detail/$entryId"
    }

    data object PendingDetail : AppDestination("pending_detail/{pendingActionId}/{ledgerEntryId}") {
        fun createRoute(pendingActionId: Long, ledgerEntryId: Long): String {
            return "pending_detail/$pendingActionId/$ledgerEntryId"
        }
    }
}
