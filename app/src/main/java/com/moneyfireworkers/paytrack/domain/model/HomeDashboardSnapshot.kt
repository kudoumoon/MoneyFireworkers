package com.moneyfireworkers.paytrack.domain.model

data class HomeDashboardSnapshot(
    val summary: HomeSummary,
    val pendingOverview: HomePendingOverview,
    val recentSpendings: List<HomeRecentSpending>,
    val lastUpdatedAt: Long,
) {
    val isEmpty: Boolean
        get() = pendingOverview.pendingCount == 0 && recentSpendings.isEmpty()
}

data class HomeSummary(
    val monthlyExpenseInCent: Long,
    val todayExpenseInCent: Long,
    val pendingAmountInCent: Long,
)

data class HomePendingOverview(
    val pendingCount: Int,
    val latestPending: HomePendingItem? = null,
)

data class HomePendingItem(
    val pendingActionId: Long,
    val ledgerEntryId: Long,
    val merchantName: String,
    val amountInCent: Long,
    val categoryName: String?,
    val occurredAt: Long,
)

data class HomeRecentSpending(
    val entryId: Long,
    val merchantName: String,
    val amountInCent: Long,
    val categoryName: String,
    val occurredAt: Long,
    val emotion: SpendingEmotion? = null,
)
