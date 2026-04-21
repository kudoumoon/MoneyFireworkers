package com.moneyfireworkers.paytrack.feature.home

import com.moneyfireworkers.paytrack.domain.model.HomeDashboardSnapshot
import com.moneyfireworkers.paytrack.domain.model.HomePendingItem
import com.moneyfireworkers.paytrack.domain.model.HomePendingOverview
import com.moneyfireworkers.paytrack.domain.model.HomeRecentSpending
import com.moneyfireworkers.paytrack.domain.model.HomeSummary
import com.moneyfireworkers.paytrack.domain.model.SpendingEmotion
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeUiStateMapperTest {
    private val mapper = HomeUiStateMapper(
        zoneId = ZoneId.of("Asia/Shanghai"),
    )

    @Test
    fun `map returns empty state when snapshot has no pending and no records`() {
        val snapshot = HomeDashboardSnapshot(
            summary = HomeSummary(
                monthlyExpenseInCent = 0L,
                todayExpenseInCent = 0L,
                pendingAmountInCent = 0L,
            ),
            pendingOverview = HomePendingOverview(pendingCount = 0),
            recentSpendings = emptyList(),
            lastUpdatedAt = 1_713_618_000_000L,
        )

        val uiState = mapper.map(snapshot)

        assertEquals(HomeScreenState.EMPTY, uiState.screenState)
        assertEquals("¥0.00", uiState.summary.monthlyExpenseLabel)
        assertFalse(uiState.pendingSection.hasPending)
        assertTrue(uiState.recentSpendings.isEmpty())
        assertFalse(uiState.primaryActions.canProcessPending)
    }

    @Test
    fun `map returns content state and optional emotion when snapshot has data`() {
        val snapshot = HomeDashboardSnapshot(
            summary = HomeSummary(
                monthlyExpenseInCent = 126_580L,
                todayExpenseInCent = 5_800L,
                pendingAmountInCent = 1_800L,
            ),
            pendingOverview = HomePendingOverview(
                pendingCount = 1,
                latestPending = HomePendingItem(
                    pendingActionId = 11L,
                    ledgerEntryId = 22L,
                    merchantName = "瑞幸咖啡",
                    amountInCent = 1_800L,
                    categoryName = "咖啡茶饮",
                    occurredAt = 1_713_618_000_000L,
                ),
            ),
            recentSpendings = listOf(
                HomeRecentSpending(
                    entryId = 99L,
                    merchantName = "瑞幸咖啡",
                    amountInCent = 1_800L,
                    categoryName = "咖啡茶饮",
                    occurredAt = 1_713_618_000_000L,
                    emotion = SpendingEmotion.HAPPY,
                ),
                HomeRecentSpending(
                    entryId = 100L,
                    merchantName = "地铁",
                    amountInCent = 500L,
                    categoryName = "交通",
                    occurredAt = 1_713_618_300_000L,
                    emotion = null,
                ),
            ),
            lastUpdatedAt = 1_713_618_600_000L,
        )

        val uiState = mapper.map(snapshot)

        assertEquals(HomeScreenState.CONTENT, uiState.screenState)
        assertEquals("1", uiState.pendingSection.pendingCountLabel)
        assertTrue(uiState.pendingSection.hasPending)
        assertEquals("瑞幸咖啡", uiState.pendingSection.latestPending?.merchantName)
        assertTrue(uiState.primaryActions.canProcessPending)
        assertEquals(2, uiState.recentSpendings.size)
        assertEquals(SpendingEmotion.HAPPY, uiState.recentSpendings.first().emotion)
        assertNull(uiState.recentSpendings.last().emotionLabelToken)
    }
}
