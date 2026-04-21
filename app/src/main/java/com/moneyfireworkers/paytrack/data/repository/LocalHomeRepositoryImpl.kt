package com.moneyfireworkers.paytrack.data.repository

import com.moneyfireworkers.paytrack.core.model.EntryStatus
import com.moneyfireworkers.paytrack.domain.model.HomeDashboardSnapshot
import com.moneyfireworkers.paytrack.domain.model.HomePendingItem
import com.moneyfireworkers.paytrack.domain.model.HomePendingOverview
import com.moneyfireworkers.paytrack.domain.model.HomeRecentSpending
import com.moneyfireworkers.paytrack.domain.model.HomeSummary
import com.moneyfireworkers.paytrack.domain.model.LedgerEntry
import com.moneyfireworkers.paytrack.domain.model.PendingAction
import com.moneyfireworkers.paytrack.domain.repository.CategoryRepository
import com.moneyfireworkers.paytrack.domain.repository.HomeRepository
import com.moneyfireworkers.paytrack.domain.repository.LedgerRepository
import com.moneyfireworkers.paytrack.domain.repository.PendingRepository
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class LocalHomeRepositoryImpl(
    private val ledgerRepository: LedgerRepository,
    private val pendingRepository: PendingRepository,
    private val categoryRepository: CategoryRepository,
    private val onRefresh: suspend () -> Unit = {},
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) : HomeRepository {
    override fun observeDashboard(): Flow<HomeDashboardSnapshot> {
        return combine(
            ledgerRepository.observeAll(),
            pendingRepository.observeActiveActions(),
            categoryRepository.observeAllEnabled(),
        ) { entries, pendingActions, categories ->
            val categoryNames = categories.associateBy({ it.id }, { it.name })
            val entriesById = entries.associateBy { it.id }
            val confirmedEntries = entries
                .filter { entry -> entry.entryStatus.isFinalized() }
                .sortedByDescending { entry -> entry.occurredAt }
            val pendingItems = pendingActions
                .mapNotNull { action -> action.toPendingItem(entriesById, categoryNames) }
                .sortedByDescending { item -> item.occurredAt }

            HomeDashboardSnapshot(
                summary = HomeSummary(
                    monthlyExpenseInCent = confirmedEntries.sumWithinCurrentMonth(),
                    todayExpenseInCent = confirmedEntries.sumWithinToday(),
                    pendingAmountInCent = pendingItems.sumOf { item -> item.amountInCent },
                ),
                pendingOverview = HomePendingOverview(
                    pendingCount = pendingItems.size,
                    latestPending = pendingItems.firstOrNull(),
                ),
                recentSpendings = confirmedEntries
                    .take(8)
                    .map { entry ->
                        HomeRecentSpending(
                            entryId = entry.id,
                            merchantName = entry.merchantName.ifBlank { "未命名商户" },
                            amountInCent = entry.amountInCent,
                            categoryName = categoryNames[entry.categoryIdFinal ?: entry.categoryIdSuggested]
                                ?: "待分类",
                            occurredAt = entry.occurredAt,
                        )
                    },
                lastUpdatedAt = entries.maxOfOrNull { entry -> entry.updatedAt }
                    ?: System.currentTimeMillis(),
            )
        }
    }

    override suspend fun refreshDashboard() {
        onRefresh()
    }

    private fun PendingAction.toPendingItem(
        entriesById: Map<Long, LedgerEntry>,
        categoryNames: Map<Long, String>,
    ): HomePendingItem? {
        val ledgerEntry = entriesById[ledgerEntryId] ?: return null
        return HomePendingItem(
            pendingActionId = id,
            ledgerEntryId = ledgerEntry.id,
            merchantName = ledgerEntry.merchantName.ifBlank { "待补充商户" },
            amountInCent = ledgerEntry.amountInCent,
            categoryName = categoryNames[ledgerEntry.categoryIdFinal ?: ledgerEntry.categoryIdSuggested],
            occurredAt = ledgerEntry.occurredAt,
        )
    }

    private fun EntryStatus.isFinalized(): Boolean {
        return this == EntryStatus.CONFIRMED_AUTO || this == EntryStatus.CONFIRMED_WITH_EDIT
    }

    private fun List<LedgerEntry>.sumWithinCurrentMonth(): Long {
        val monthStart = Instant.ofEpochMilli(System.currentTimeMillis())
            .atZone(zoneId)
            .toLocalDate()
            .withDayOfMonth(1)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()
        return filter { entry -> entry.occurredAt >= monthStart }
            .sumOf { entry -> entry.amountInCent }
    }

    private fun List<LedgerEntry>.sumWithinToday(): Long {
        val todayStart = Instant.ofEpochMilli(System.currentTimeMillis())
            .atZone(zoneId)
            .toLocalDate()
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()
        return filter { entry -> entry.occurredAt >= todayStart }
            .sumOf { entry -> entry.amountInCent }
    }
}
