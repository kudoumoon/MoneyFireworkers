package com.moneyfireworkers.paytrack.feature.home

import com.moneyfireworkers.paytrack.domain.model.HomeDashboardSnapshot
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class HomeUiStateMapper(
    private val zoneId: ZoneId = ZoneId.systemDefault(),
    private val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale.SIMPLIFIED_CHINESE),
) {
    private val itemTimeFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm")

    fun map(snapshot: HomeDashboardSnapshot): HomeUiState {
        return HomeUiState(
            screenState = if (snapshot.isEmpty) HomeScreenState.EMPTY else HomeScreenState.CONTENT,
            summary = HomeSummaryUiModel(
                monthlyExpenseInCent = snapshot.summary.monthlyExpenseInCent,
                monthlyExpenseLabel = formatAmount(snapshot.summary.monthlyExpenseInCent),
                todayExpenseInCent = snapshot.summary.todayExpenseInCent,
                todayExpenseLabel = formatAmount(snapshot.summary.todayExpenseInCent),
                pendingAmountInCent = snapshot.summary.pendingAmountInCent,
                pendingAmountLabel = formatAmount(snapshot.summary.pendingAmountInCent),
            ),
            pendingSection = HomePendingSectionUiModel(
                pendingCount = snapshot.pendingOverview.pendingCount,
                pendingCountLabel = snapshot.pendingOverview.pendingCount.toString(),
                latestPending = snapshot.pendingOverview.latestPending?.let { pending ->
                    HomePendingItemUiModel(
                        pendingActionId = pending.pendingActionId,
                        ledgerEntryId = pending.ledgerEntryId,
                        merchantName = pending.merchantName,
                        amountInCent = pending.amountInCent,
                        amountLabel = formatAmount(pending.amountInCent),
                        categoryName = pending.categoryName,
                        timeLabel = formatTimestamp(pending.occurredAt),
                    )
                },
            ),
            recentSpendings = snapshot.recentSpendings.map { spending ->
                HomeRecentSpendingUiModel(
                    entryId = spending.entryId,
                    merchantName = spending.merchantName,
                    amountInCent = spending.amountInCent,
                    amountLabel = formatAmount(spending.amountInCent),
                    categoryName = spending.categoryName,
                    timeLabel = formatTimestamp(spending.occurredAt),
                    emotion = spending.emotion,
                    emotionLabelToken = spending.emotion?.label,
                    amountColorToken = spending.emotion?.amountColorToken,
                )
            },
            primaryActions = HomePrimaryActionsUiModel(
                canAddPayment = true,
                canViewAll = true,
                canProcessPending = snapshot.pendingOverview.pendingCount > 0,
            ),
            lastUpdatedLabel = formatTimestamp(snapshot.lastUpdatedAt),
            isRefreshing = false,
        )
    }

    private fun formatAmount(amountInCent: Long): String {
        return currencyFormatter.format(amountInCent / 100.0)
    }

    private fun formatTimestamp(timestamp: Long): String {
        return Instant.ofEpochMilli(timestamp)
            .atZone(zoneId)
            .format(itemTimeFormatter)
    }
}
