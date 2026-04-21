package com.moneyfireworkers.paytrack.feature.home

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneyfireworkers.paytrack.core.ui.theme.EmotionHappy
import com.moneyfireworkers.paytrack.core.ui.theme.EmotionImpulsive
import com.moneyfireworkers.paytrack.core.ui.theme.EmotionNeutral
import com.moneyfireworkers.paytrack.core.ui.theme.EmotionRegretful
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyBackground
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyDivider
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyPendingBorder
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyPendingTint
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyPrimary
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyPrimaryDeep
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyPrimarySoft
import com.moneyfireworkers.paytrack.core.ui.theme.MoneySurface
import com.moneyfireworkers.paytrack.core.ui.theme.MoneySurfaceSoft
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyTextPrimary
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyTextSecondary
import com.moneyfireworkers.paytrack.domain.model.EmotionAmountColorToken
import com.moneyfireworkers.paytrack.domain.model.SpendingEmotion

private val HomeCardShape = RoundedCornerShape(12.dp)

private const val LabelHome = "\u9996\u9875"
private const val LabelHomeHint =
    "\u4ED8\u6B3E\u540E\uFF0C\u9996\u9875\u4F1A\u5E2E\u4F60\u628A\u8FD9\u7B14\u94B1\u6536\u597D"
private const val LabelUpdatedAt = "\u66F4\u65B0\u4E8E"
private const val LabelRefresh = "\u5237\u65B0"
private const val LabelRefreshing = "\u5237\u65B0\u4E2D"
private const val LabelRefreshFailed = "\u5237\u65B0\u5931\u8D25"
private const val LabelSummaryHint =
    "\u672C\u6708\u652F\u51FA\u3001\u4ECA\u65E5\u652F\u51FA\u548C\u5F85\u786E\u8BA4\u91D1\u989D\u90FD\u4F1A\u5728\u8FD9\u91CC\u540C\u6B65\u66F4\u65B0\u3002"
private const val LabelMonthlyExpense = "\u672C\u6708\u652F\u51FA"
private const val LabelTodayExpense = "\u4ECA\u65E5\u652F\u51FA"
private const val LabelPendingAmount = "\u5F85\u786E\u8BA4\u91D1\u989D"
private const val LabelPending = "\u5F85\u786E\u8BA4"
private const val LabelNoPending =
    "\u5F53\u524D\u6CA1\u6709\u672A\u5904\u7406\u7684\u4ED8\u6B3E\u63D0\u9192"
private const val LabelPendingAction = "\u7ACB\u5373\u5904\u7406"
private const val LabelPendingCalm =
    "\u6CA1\u6709\u65B0\u7684\u5F85\u786E\u8BA4\u8D26\u5355\uFF0C\u521A\u5B8C\u6210\u7684\u6D88\u8D39\u4F1A\u5728\u8FD9\u91CC\u63D0\u9192\u4F60\u590D\u6838\u3002"
private const val LabelAddPayment = "\u6DFB\u52A0\u4ED8\u6B3E"
private const val LabelViewAll = "\u67E5\u770B\u5168\u90E8"
private const val LabelOpenRecords = "\u8FDB\u5165\u5B8C\u6574\u8D26\u5355"
private const val LabelProcessPending = "\u5904\u7406\u5F85\u786E\u8BA4"
private const val LabelProcessPendingHint =
    "\u7EE7\u7EED\u5B8C\u6210\u672A\u5904\u7406\u9879"
private const val LabelNoPendingShort = "\u5F53\u524D\u6CA1\u6709\u5F85\u786E\u8BA4"
private const val LabelRecentSpendings = "\u6700\u8FD1\u6D88\u8D39"
private const val LabelRecentSpendingsHint =
    "\u8FD9\u91CC\u5C55\u793A\u5DF2\u7ECF\u786E\u8BA4\u5165\u8D26\u7684\u6700\u65B0\u8BB0\u5F55"
private const val LabelRecentSpendingsLoadingHint =
    "\u786E\u8BA4\u5B8C\u6210\u540E\uFF0C\u6700\u65B0\u8BB0\u8D26\u4F1A\u51FA\u73B0\u5728\u8FD9\u91CC"
private const val LabelViewDetail = "\u67E5\u770B\u8BE6\u60C5"
private const val LabelEmptyTitle = "\u8FD8\u6CA1\u6709\u6D88\u8D39\u8BB0\u5F55"
private const val LabelEmptyHint =
    "\u6DFB\u52A0\u7B2C\u4E00\u7B14\u4ED8\u6B3E\u4E4B\u540E\uFF0C\u8FD9\u91CC\u4F1A\u5F00\u59CB\u5C55\u793A\u5F85\u786E\u8BA4\u63D0\u9192\u548C\u6700\u8FD1\u8D26\u5355\u3002"
private const val LabelAddFirstPayment = "\u6DFB\u52A0\u4E00\u7B14\u4ED8\u6B3E"
private const val LabelEmotionHappy = "\u5F00\u5FC3"
private const val LabelEmotionNeutral = "\u4E00\u822C"
private const val LabelEmotionRegretful = "\u540E\u6094"
private const val LabelEmotionImpulsive = "\u51B2\u52A8"

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onAction: (HomeUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MoneyBackground,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .safeDrawingPadding(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                HomeHeader(
                    lastUpdatedLabel = uiState.lastUpdatedLabel,
                    isRefreshing = uiState.isRefreshing,
                    refreshErrorMessage = uiState.refreshErrorMessage,
                    onRefresh = { onAction(HomeUiAction.Refresh) },
                )
            }

            when (uiState.screenState) {
                HomeScreenState.LOADING -> {
                    item { HomeSummaryLoadingCard() }
                    item { HomePendingLoadingCard() }
                    item { HomeActionLoadingSection() }
                    item {
                        SectionTitle(
                            title = LabelRecentSpendings,
                            subtitle = LabelRecentSpendingsLoadingHint,
                        )
                    }
                    items(3) {
                        SpendingLoadingCard()
                    }
                }

                HomeScreenState.EMPTY -> {
                    item {
                        HomeSummaryCard(summary = uiState.summary)
                    }
                    item {
                        PendingCard(
                            pendingSection = uiState.pendingSection,
                            onPendingClick = { onAction(HomeUiAction.PendingClicked) },
                        )
                    }
                    item {
                        ActionPanel(
                            primaryActions = uiState.primaryActions,
                            onAction = onAction,
                        )
                    }
                    item {
                        EmptyStateCard(
                            onAddPayment = { onAction(HomeUiAction.AddPaymentClicked) },
                        )
                    }
                }

                HomeScreenState.CONTENT -> {
                    item {
                        HomeSummaryCard(summary = uiState.summary)
                    }
                    item {
                        PendingCard(
                            pendingSection = uiState.pendingSection,
                            onPendingClick = { onAction(HomeUiAction.PendingClicked) },
                        )
                    }
                    item {
                        ActionPanel(
                            primaryActions = uiState.primaryActions,
                            onAction = onAction,
                        )
                    }
                    item {
                        SectionTitle(
                            title = LabelRecentSpendings,
                            subtitle = LabelRecentSpendingsHint,
                        )
                    }
                    items(
                        items = uiState.recentSpendings,
                        key = { item -> item.entryId },
                    ) { spending ->
                        RecentSpendingCard(
                            spending = spending,
                            onClick = {
                                onAction(HomeUiAction.RecentSpendingClicked(spending.entryId))
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    lastUpdatedLabel: String?,
    isRefreshing: Boolean,
    refreshErrorMessage: String?,
    onRefresh: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = LabelHome,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MoneyTextPrimary,
                )
                Text(
                    text = buildString {
                        append(LabelHomeHint)
                        if (!lastUpdatedLabel.isNullOrBlank()) {
                            append(" | ")
                            append(LabelUpdatedAt)
                            append(" ")
                            append(lastUpdatedLabel)
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MoneyTextSecondary,
                )
            }
            TextButton(
                onClick = onRefresh,
                enabled = !isRefreshing,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MoneyPrimaryDeep,
                    disabledContentColor = MoneyTextSecondary,
                ),
            ) {
                Text(if (isRefreshing) LabelRefreshing else LabelRefresh)
            }
        }

        if (!refreshErrorMessage.isNullOrBlank()) {
            Text(
                text = "$LabelRefreshFailed: $refreshErrorMessage",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun HomeSummaryCard(summary: HomeSummaryUiModel) {
    Card(
        shape = HomeCardShape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(MoneyPrimary, MoneyPrimaryDeep),
                    ),
                )
                .padding(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = LabelMonthlyExpense,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.78f),
                    )
                    Text(
                        text = summary.monthlyExpenseLabel,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.8).sp,
                        ),
                        color = Color.White,
                    )
                    Text(
                        text = LabelSummaryHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SummaryMetric(
                        title = LabelTodayExpense,
                        value = summary.todayExpenseLabel,
                        modifier = Modifier.weight(1f),
                    )
                    SummaryMetric(
                        title = LabelPendingAmount,
                        value = summary.pendingAmountLabel,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryMetric(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.16f),
        shape = HomeCardShape,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.76f),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun PendingCard(
    pendingSection: HomePendingSectionUiModel,
    onPendingClick: () -> Unit,
) {
    Card(
        shape = HomeCardShape,
        colors = CardDefaults.cardColors(containerColor = MoneySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = LabelPending,
                        style = MaterialTheme.typography.titleMedium,
                        color = MoneyTextPrimary,
                    )
                    Text(
                        text = if (pendingSection.hasPending) {
                            "\u8FD8\u6709 ${pendingSection.pendingCountLabel} \u7B14\u4ED8\u6B3E\u9700\u8981\u4F60\u786E\u8BA4"
                        } else {
                            LabelNoPending
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MoneyTextSecondary,
                    )
                }
                PendingCountBadge(count = pendingSection.pendingCountLabel)
            }

            if (pendingSection.hasPending) {
                val pendingItem = pendingSection.latestPending
                Surface(
                    color = MoneyPendingTint,
                    shape = HomeCardShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MoneyPendingBorder, HomeCardShape)
                        .clickable(onClick = onPendingClick),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = pendingItem?.merchantName.orEmpty(),
                                style = MaterialTheme.typography.titleMedium,
                                color = MoneyTextPrimary,
                            )
                            Text(
                                text = listOfNotNull(
                                    pendingItem?.categoryName,
                                    pendingItem?.timeLabel,
                                ).joinToString(" / "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MoneyTextSecondary,
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = pendingItem?.amountLabel.orEmpty(),
                                style = MaterialTheme.typography.titleLarge,
                                color = MoneyPrimaryDeep,
                            )
                            Text(
                                text = LabelPendingAction,
                                style = MaterialTheme.typography.labelLarge,
                                color = MoneyPrimaryDeep,
                            )
                        }
                    }
                }
            } else {
                Surface(
                    color = MoneySurfaceSoft,
                    shape = HomeCardShape,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = LabelPendingCalm,
                        style = MaterialTheme.typography.bodySmall,
                        color = MoneyTextSecondary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun PendingCountBadge(count: String) {
    Surface(
        color = MoneyPrimarySoft,
        shape = CircleShape,
    ) {
        Text(
            text = count,
            style = MaterialTheme.typography.labelLarge,
            color = MoneyPrimaryDeep,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun ActionPanel(
    primaryActions: HomePrimaryActionsUiModel,
    onAction: (HomeUiAction) -> Unit,
) {
    Card(
        shape = HomeCardShape,
        colors = CardDefaults.cardColors(containerColor = MoneySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = { onAction(HomeUiAction.AddPaymentClicked) },
                enabled = primaryActions.canAddPayment,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = HomeCardShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MoneyPrimary,
                    contentColor = Color.White,
                ),
            ) {
                Text(LabelAddPayment)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ActionSecondaryCard(
                    title = LabelViewAll,
                    subtitle = LabelOpenRecords,
                    onClick = { onAction(HomeUiAction.ViewAllClicked) },
                    enabled = primaryActions.canViewAll,
                    modifier = Modifier.weight(1f),
                )
                ActionSecondaryCard(
                    title = LabelProcessPending,
                    subtitle = if (primaryActions.canProcessPending) {
                        LabelProcessPendingHint
                    } else {
                        LabelNoPendingShort
                    },
                    onClick = { onAction(HomeUiAction.PendingClicked) },
                    enabled = primaryActions.canProcessPending,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ActionSecondaryCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .heightIn(min = 96.dp)
            .clip(HomeCardShape)
            .clickable(enabled = enabled, onClick = onClick),
        color = if (enabled) MoneySurfaceSoft else MoneySurfaceSoft.copy(alpha = 0.65f),
        shape = HomeCardShape,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (enabled) MoneyTextPrimary else MoneyTextSecondary,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MoneyTextSecondary,
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MoneyTextPrimary,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MoneyTextSecondary,
        )
    }
}

@Composable
private fun RecentSpendingCard(
    spending: HomeRecentSpendingUiModel,
    onClick: () -> Unit,
) {
    val amountColor = MoneyTextPrimary
    val emotionColor = spending.amountColorToken?.let(::spendingAmountColor)
    val chipColor = emotionColor?.copy(alpha = 0.12f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = HomeCardShape,
        colors = CardDefaults.cardColors(containerColor = MoneySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = spending.merchantName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MoneyTextPrimary,
                )
                Text(
                    text = "${spending.categoryName} / ${spending.timeLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MoneyTextSecondary,
                )
                if (spending.emotion != null && emotionColor != null && chipColor != null) {
                    EmotionChip(
                        label = spendingEmotionLabel(spending.emotion),
                        color = emotionColor,
                        background = chipColor,
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = spending.amountLabel,
                    style = MaterialTheme.typography.titleLarge,
                    color = amountColor,
                )
                Text(
                    text = LabelViewDetail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MoneyTextSecondary,
                )
            }
        }
    }
}

@Composable
private fun EmotionChip(
    label: String,
    color: Color,
    background: Color,
) {
    Surface(
        shape = CircleShape,
        color = background,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun EmptyStateCard(
    onAddPayment: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = HomeCardShape,
        colors = CardDefaults.cardColors(containerColor = MoneySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(MoneyPrimarySoft, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MoneyPrimaryDeep,
                )
            }
            Text(
                text = LabelEmptyTitle,
                style = MaterialTheme.typography.titleLarge,
                color = MoneyTextPrimary,
            )
            Text(
                text = LabelEmptyHint,
                style = MaterialTheme.typography.bodySmall,
                color = MoneyTextSecondary,
                textAlign = TextAlign.Center,
            )
            OutlinedButton(
                onClick = onAddPayment,
                shape = HomeCardShape,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MoneyPrimaryDeep,
                ),
            ) {
                Text(LabelAddFirstPayment)
            }
        }
    }
}

@Composable
private fun HomeSummaryLoadingCard() {
    LoadingCard(height = 220.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            LoadingLine(width = 92.dp, height = 14.dp)
            LoadingLine(width = 196.dp, height = 38.dp)
            LoadingLine(width = 240.dp, height = 12.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LoadingBlock(modifier = Modifier.weight(1f).height(82.dp))
                LoadingBlock(modifier = Modifier.weight(1f).height(82.dp))
            }
        }
    }
}

@Composable
private fun HomePendingLoadingCard() {
    LoadingCard(height = 164.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LoadingLine(width = 84.dp, height = 16.dp)
                    LoadingLine(width = 172.dp, height = 12.dp)
                }
                LoadingLine(width = 36.dp, height = 36.dp, shape = CircleShape)
            }
            LoadingBlock(modifier = Modifier.fillMaxWidth().height(72.dp))
        }
    }
}

@Composable
private fun HomeActionLoadingSection() {
    LoadingCard(height = 154.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LoadingBlock(modifier = Modifier.fillMaxWidth().height(54.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                LoadingBlock(modifier = Modifier.weight(1f).height(72.dp))
                LoadingBlock(modifier = Modifier.weight(1f).height(72.dp))
            }
        }
    }
}

@Composable
private fun SpendingLoadingCard() {
    LoadingCard(height = 112.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LoadingLine(width = 122.dp, height = 16.dp)
                LoadingLine(width = 150.dp, height = 12.dp)
                LoadingLine(width = 60.dp, height = 24.dp, shape = CircleShape)
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LoadingLine(width = 78.dp, height = 20.dp)
                LoadingLine(width = 48.dp, height = 12.dp)
            }
        }
    }
}

@Composable
private fun LoadingCard(
    height: Dp,
    content: @Composable () -> Unit,
) {
    Card(
        shape = HomeCardShape,
        colors = CardDefaults.cardColors(containerColor = MoneySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
    ) {
        Box(modifier = Modifier.padding(18.dp)) {
            content()
        }
    }
}

@Composable
private fun LoadingBlock(modifier: Modifier) {
    val placeholderAlpha = loadingPlaceholderAlpha()
    Spacer(
        modifier = modifier
            .clip(HomeCardShape)
            .background(MoneyDivider.copy(alpha = placeholderAlpha)),
    )
}

@Composable
private fun LoadingLine(
    width: Dp,
    height: Dp,
    shape: Shape = RoundedCornerShape(999.dp),
) {
    val placeholderAlpha = loadingPlaceholderAlpha()
    Spacer(
        modifier = Modifier
            .width(width)
            .height(height)
            .clip(shape)
            .background(MoneyDivider.copy(alpha = placeholderAlpha)),
    )
}

@Composable
private fun loadingPlaceholderAlpha(): Float {
    val transition = rememberInfiniteTransition(label = "loading")
    val alpha by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "loading_alpha",
    )
    return alpha
}

private fun spendingEmotionLabel(emotion: SpendingEmotion): String {
    return when (emotion) {
        SpendingEmotion.HAPPY -> LabelEmotionHappy
        SpendingEmotion.NEUTRAL -> LabelEmotionNeutral
        SpendingEmotion.REGRETFUL -> LabelEmotionRegretful
        SpendingEmotion.IMPULSIVE -> LabelEmotionImpulsive
    }
}

private fun spendingAmountColor(token: EmotionAmountColorToken): Color {
    return when (token) {
        EmotionAmountColorToken.HAPPY_AMOUNT -> EmotionHappy
        EmotionAmountColorToken.NEUTRAL_AMOUNT -> EmotionNeutral
        EmotionAmountColorToken.REGRETFUL_AMOUNT -> EmotionRegretful
        EmotionAmountColorToken.IMPULSIVE_AMOUNT -> EmotionImpulsive
    }
}
