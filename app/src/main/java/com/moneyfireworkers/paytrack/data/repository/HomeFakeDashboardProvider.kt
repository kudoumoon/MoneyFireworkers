package com.moneyfireworkers.paytrack.data.repository

import com.moneyfireworkers.paytrack.domain.model.HomeDashboardSnapshot
import com.moneyfireworkers.paytrack.domain.model.HomePendingItem
import com.moneyfireworkers.paytrack.domain.model.HomePendingOverview
import com.moneyfireworkers.paytrack.domain.model.HomeRecentSpending
import com.moneyfireworkers.paytrack.domain.model.HomeSummary
import com.moneyfireworkers.paytrack.domain.model.SpendingEmotion

class HomeFakeDashboardProvider(
    private val nowProvider: () -> Long = { System.currentTimeMillis() },
) {
    fun createSnapshot(scenario: HomeFakeScenario): HomeDashboardSnapshot {
        val now = nowProvider()
        return when (scenario) {
            HomeFakeScenario.POPULATED -> createPopulatedSnapshot(now)
            HomeFakeScenario.EMPTY -> createEmptySnapshot(now)
        }
    }

    private fun createPopulatedSnapshot(now: Long): HomeDashboardSnapshot {
        val recentSpendings = listOf(
            HomeRecentSpending(
                entryId = 301L,
                merchantName = "Luckin Coffee",
                amountInCent = 1800L,
                categoryName = "Coffee & Tea",
                occurredAt = now - 18L * 60L * 1000L,
                emotion = SpendingEmotion.HAPPY,
            ),
            HomeRecentSpending(
                entryId = 302L,
                merchantName = "Metro Line 2",
                amountInCent = 500L,
                categoryName = "Transport",
                occurredAt = now - 2L * 60L * 60L * 1000L,
                emotion = SpendingEmotion.NEUTRAL,
            ),
            HomeRecentSpending(
                entryId = 303L,
                merchantName = "Hema Fresh",
                amountInCent = 8600L,
                categoryName = "Groceries",
                occurredAt = now - 5L * 60L * 60L * 1000L,
                emotion = SpendingEmotion.HAPPY,
            ),
            HomeRecentSpending(
                entryId = 304L,
                merchantName = "Ride Hailing",
                amountInCent = 4200L,
                categoryName = "Transport",
                occurredAt = now - 26L * 60L * 60L * 1000L,
                emotion = SpendingEmotion.REGRETFUL,
            ),
            HomeRecentSpending(
                entryId = 305L,
                merchantName = "Late Night Snack",
                amountInCent = 3600L,
                categoryName = "Dining",
                occurredAt = now - 31L * 60L * 60L * 1000L,
                emotion = SpendingEmotion.REGRETFUL,
            ),
        )

        return HomeDashboardSnapshot(
            summary = HomeSummary(
                monthlyExpenseInCent = 126_580L,
                todayExpenseInCent = 5_800L,
                pendingAmountInCent = 1_800L,
            ),
            pendingOverview = HomePendingOverview(
                pendingCount = 2,
                latestPending = HomePendingItem(
                    pendingActionId = 9001L,
                    ledgerEntryId = 8001L,
                    merchantName = "Luckin Coffee",
                    amountInCent = 1800L,
                    categoryName = "Coffee & Tea",
                    occurredAt = now - 8L * 60L * 1000L,
                ),
            ),
            recentSpendings = recentSpendings,
            lastUpdatedAt = now,
        )
    }

    private fun createEmptySnapshot(now: Long): HomeDashboardSnapshot {
        return HomeDashboardSnapshot(
            summary = HomeSummary(
                monthlyExpenseInCent = 0L,
                todayExpenseInCent = 0L,
                pendingAmountInCent = 0L,
            ),
            pendingOverview = HomePendingOverview(pendingCount = 0),
            recentSpendings = emptyList(),
            lastUpdatedAt = now,
        )
    }
}
