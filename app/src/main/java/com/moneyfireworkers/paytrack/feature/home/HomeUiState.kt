package com.moneyfireworkers.paytrack.feature.home

import com.moneyfireworkers.paytrack.domain.model.EmotionAmountColorToken
import com.moneyfireworkers.paytrack.domain.model.SpendingEmotion

enum class HomeScreenState {
    LOADING,
    EMPTY,
    CONTENT,
}

data class HomeUiState(
    val screenState: HomeScreenState = HomeScreenState.LOADING,
    val summary: HomeSummaryUiModel = HomeSummaryUiModel.empty(),
    val pendingSection: HomePendingSectionUiModel = HomePendingSectionUiModel.empty(),
    val recentSpendings: List<HomeRecentSpendingUiModel> = emptyList(),
    val primaryActions: HomePrimaryActionsUiModel = HomePrimaryActionsUiModel(),
    val lastUpdatedLabel: String? = null,
    val isRefreshing: Boolean = false,
    val refreshErrorMessage: String? = null,
)

data class HomeSummaryUiModel(
    val monthlyExpenseInCent: Long,
    val monthlyExpenseLabel: String,
    val todayExpenseInCent: Long,
    val todayExpenseLabel: String,
    val pendingAmountInCent: Long,
    val pendingAmountLabel: String,
) {
    companion object {
        fun empty(): HomeSummaryUiModel {
            return HomeSummaryUiModel(
                monthlyExpenseInCent = 0L,
                monthlyExpenseLabel = "",
                todayExpenseInCent = 0L,
                todayExpenseLabel = "",
                pendingAmountInCent = 0L,
                pendingAmountLabel = "",
            )
        }
    }
}

data class HomePendingSectionUiModel(
    val pendingCount: Int,
    val pendingCountLabel: String,
    val latestPending: HomePendingItemUiModel?,
) {
    val hasPending: Boolean
        get() = pendingCount > 0 && latestPending != null

    companion object {
        fun empty(): HomePendingSectionUiModel {
            return HomePendingSectionUiModel(
                pendingCount = 0,
                pendingCountLabel = "0",
                latestPending = null,
            )
        }
    }
}

data class HomePendingItemUiModel(
    val pendingActionId: Long,
    val ledgerEntryId: Long,
    val merchantName: String,
    val amountInCent: Long,
    val amountLabel: String,
    val categoryName: String?,
    val timeLabel: String,
)

data class HomeRecentSpendingUiModel(
    val entryId: Long,
    val merchantName: String,
    val amountInCent: Long,
    val amountLabel: String,
    val categoryName: String,
    val timeLabel: String,
    val emotion: SpendingEmotion? = null,
    val emotionLabelToken: String? = null,
    val amountColorToken: EmotionAmountColorToken? = null,
)

data class HomePrimaryActionsUiModel(
    val canAddPayment: Boolean = true,
    val canViewAll: Boolean = true,
    val canProcessPending: Boolean = false,
)
