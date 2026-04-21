package com.moneyfireworkers.paytrack.feature.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moneyfireworkers.paytrack.core.model.EntryStatus
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyBackground
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyButter
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyPeach
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyPrimary
import com.moneyfireworkers.paytrack.core.ui.theme.MoneySky
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyTextPrimary
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyTextSecondary
import com.moneyfireworkers.paytrack.domain.model.AppLanguage
import com.moneyfireworkers.paytrack.domain.model.AppSettings
import com.moneyfireworkers.paytrack.domain.model.Category
import com.moneyfireworkers.paytrack.domain.model.LedgerEntry
import com.moneyfireworkers.paytrack.domain.model.SpendingEmotion
import com.moneyfireworkers.paytrack.domain.repository.CategoryRepository
import com.moneyfireworkers.paytrack.domain.repository.LedgerRepository
import com.moneyfireworkers.paytrack.domain.repository.SettingsRepository
import com.moneyfireworkers.paytrack.feature.common.AmbientOrb
import com.moneyfireworkers.paytrack.feature.common.AppScreenHeader
import com.moneyfireworkers.paytrack.feature.common.GentleScreen
import com.moneyfireworkers.paytrack.feature.common.GlassCard
import com.moneyfireworkers.paytrack.feature.common.LocalAppLanguage
import com.moneyfireworkers.paytrack.feature.common.StatsBackdropPalette
import com.moneyfireworkers.paytrack.feature.common.breathingStatusLabel
import com.moneyfireworkers.paytrack.feature.common.emotionColor
import com.moneyfireworkers.paytrack.feature.common.emotionEmoji
import com.moneyfireworkers.paytrack.feature.common.emotionLabel
import com.moneyfireworkers.paytrack.feature.common.formatMoney
import com.moneyfireworkers.paytrack.feature.common.pick
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

private data class PortionUi(val label: String, val amountLabel: String, val ratio: Float, val color: Color)
private data class TrendUi(val label: String, val ratio: Float)

private data class StatsUiState(
    val breathingRoomLabel: String = formatMoney(0L),
    val monthlyExpenseLabel: String = formatMoney(0L),
    val monthlyExpectedLabel: String = formatMoney(0L),
    val breathingStatus: String = "",
    val trends: List<TrendUi> = emptyList(),
    val categoryPortions: List<PortionUi> = emptyList(),
    val emotionPortions: List<PortionUi> = emptyList(),
)

private class StatsViewModel(
    ledgerRepository: LedgerRepository,
    categoryRepository: CategoryRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                ledgerRepository.observeAll(),
                categoryRepository.observeAllEnabled(),
                settingsRepository.observeSettings(),
            ) { entries, categories, settings ->
                buildUiState(entries, categories, settings)
            }.collect { _uiState.value = it }
        }
    }

    private fun buildUiState(
        entries: List<LedgerEntry>,
        categories: List<Category>,
        settings: AppSettings,
    ): StatsUiState {
        val language = settings.appLanguage
        val confirmedEntries = entries.filter {
            it.entryStatus == EntryStatus.CONFIRMED_AUTO || it.entryStatus == EntryStatus.CONFIRMED_WITH_EDIT
        }
        val zoneId = ZoneId.systemDefault()
        val today = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(zoneId).toLocalDate()
        val monthStart = today.withDayOfMonth(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val monthlyEntries = confirmedEntries.filter { it.occurredAt >= monthStart }
        val monthlyExpense = monthlyEntries.sumOf { it.amountInCent }
        val breathingRoom = settings.monthlyExpectedExpenseInCent - monthlyExpense
        val maxTrend = (0..6).maxOf { index ->
            val day = today.minusDays((6 - index).toLong())
            val dayStart = day.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val nextDayStart = day.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
            monthlyEntries.filter { it.occurredAt in dayStart until nextDayStart }.sumOf { it.amountInCent }
        }.coerceAtLeast(1L)
        val trends = (0..6).map { index ->
            val day = today.minusDays((6 - index).toLong())
            val dayStart = day.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val nextDayStart = day.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
            val amount = monthlyEntries.filter { it.occurredAt in dayStart until nextDayStart }.sumOf { it.amountInCent }
            TrendUi(
                label = if (language == AppLanguage.CHINESE) {
                    "${day.monthValue}/${day.dayOfMonth}"
                } else {
                    "${day.month.name.take(3)} ${day.dayOfMonth}"
                },
                ratio = amount.toFloat() / maxTrend.toFloat(),
            )
        }
        val categoryMap = categories.associateBy { it.id }
        val total = monthlyExpense.coerceAtLeast(1L)
        val categoryPortions = monthlyEntries
            .groupBy { it.categoryIdFinal ?: it.categoryIdSuggested ?: -1L }
            .map { (categoryId, items) ->
                val amount = items.sumOf { it.amountInCent }
                PortionUi(
                    label = categoryMap[categoryId]?.name ?: language.pick("未分类", "Uncategorized"),
                    amountLabel = formatMoney(amount, language),
                    ratio = amount.toFloat() / total.toFloat(),
                    color = categoryColor(categoryMap[categoryId]?.colorToken),
                )
            }
            .sortedByDescending { it.ratio }
        val emotionPortions = SpendingEmotion.entries.map { emotion ->
            val amount = monthlyEntries.filter { it.emotion == emotion }.sumOf { it.amountInCent }
            PortionUi(
                label = "${emotionEmoji(emotion)} ${emotionLabel(emotion, language)}",
                amountLabel = formatMoney(amount, language),
                ratio = amount.toFloat() / total.toFloat(),
                color = emotionColor(emotion),
            )
        }
        return StatsUiState(
            breathingRoomLabel = formatMoney(breathingRoom, language),
            monthlyExpenseLabel = formatMoney(monthlyExpense, language),
            monthlyExpectedLabel = formatMoney(settings.monthlyExpectedExpenseInCent, language),
            breathingStatus = breathingStatusLabel(breathingRoom, settings.monthlyExpectedExpenseInCent, language),
            trends = trends,
            categoryPortions = categoryPortions,
            emotionPortions = emotionPortions,
        )
    }
}

class StatsViewModelFactory(
    private val ledgerRepository: LedgerRepository,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StatsViewModel(ledgerRepository, categoryRepository, settingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

@Composable
fun StatsRoute(viewModelFactory: StatsViewModelFactory) {
    val language = LocalAppLanguage.current
    val viewModel: StatsViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(containerColor = Color.Transparent) { innerPadding ->
        GentleScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            palette = StatsBackdropPalette,
        ) {
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    AppScreenHeader(
                        eyebrow = language.pick("STATS · 这个月", "STATS · This month"),
                        title = language.pick("支出曲线也可以很柔软 📊", "Spending patterns can feel softer too 📊"),
                        subtitle = language.pick(
                            "把波动拆开看，就知道哪些是生活日常，哪些只是情绪顺手带走的钱。",
                            "Break the movement apart and it becomes easier to see what was routine and what came from the moment."
                        ),
                    )
                }
                item {
                    GlassCard {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top,
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(language.pick("本月总览", "Monthly snapshot"), style = MaterialTheme.typography.titleLarge, color = MoneyTextPrimary)
                                    Text(
                                        language.pick("先看余额感，再看波动和去向。", "Start with remaining room, then look at movement and where money went."),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MoneyTextSecondary,
                                    )
                                }
                                AmbientOrb(emoji = "🌤️", color = MoneyButter)
                            }
                            Text(uiState.breathingRoomLabel, style = MaterialTheme.typography.displayMedium, color = MoneyTextPrimary)
                            Text(uiState.breathingStatus, style = MaterialTheme.typography.bodyMedium, color = MoneyTextSecondary)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                StatChip(language.pick("已经花掉", "Spent"), uiState.monthlyExpenseLabel, "🧾", Modifier.weight(1f))
                                StatChip(language.pick("预算总额", "Budget"), uiState.monthlyExpectedLabel, "🍵", Modifier.weight(1f))
                            }
                        }
                    }
                }
                item {
                    TrendCard(
                        items = uiState.trends,
                        title = language.pick("最近 7 天", "Last 7 days"),
                        subtitle = language.pick("花费有起伏很正常，重点是看见节奏。", "Ups and downs are normal. What matters is seeing the rhythm."),
                    )
                }
                item {
                    PortionCard(
                        title = language.pick("分类占比", "Category share"),
                        subtitle = language.pick("看看钱更爱流向哪里", "See where your money naturally tends to flow"),
                        items = uiState.categoryPortions,
                        emptyLabel = language.pick("这个月还没有足够的数据。", "There is not enough data for this month yet."),
                    )
                }
                item {
                    PortionCard(
                        title = language.pick("情绪痕迹", "Emotion trace"),
                        subtitle = language.pick("有些消费只是当下想被安慰一下", "Some spending is just a little search for comfort"),
                        items = uiState.emotionPortions,
                        emptyLabel = language.pick("这个月还没有足够的数据。", "There is not enough data for this month yet."),
                    )
                }
            }
        }
    }
}

@Composable
private fun TrendCard(
    items: List<TrendUi>,
    title: String,
    subtitle: String,
) {
    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(title, style = MaterialTheme.typography.titleLarge, color = MoneyTextPrimary)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MoneyTextSecondary)
                }
                AmbientOrb(emoji = "🌊", color = MoneySky)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                items.forEach { item ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((item.ratio.coerceAtLeast(0.08f) * 118f).dp)
                                .background(
                                    Brush.verticalGradient(listOf(MoneySky.copy(alpha = 0.45f), MoneyPrimary.copy(alpha = 0.92f))),
                                    RoundedCornerShape(999.dp),
                                ),
                        )
                        Text(item.label, style = MaterialTheme.typography.labelSmall, color = MoneyTextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun PortionCard(
    title: String,
    subtitle: String,
    items: List<PortionUi>,
    emptyLabel: String,
) {
    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge, color = MoneyTextPrimary)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MoneyTextSecondary)
            }
            if (items.isEmpty()) {
                Text(emptyLabel, style = MaterialTheme.typography.bodyMedium, color = MoneyTextSecondary)
            } else {
                items.forEach { item ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(item.color, RoundedCornerShape(999.dp)),
                                )
                                Text(item.label, style = MaterialTheme.typography.bodyMedium, color = MoneyTextPrimary)
                            }
                            Text(item.amountLabel, style = MaterialTheme.typography.bodySmall, color = MoneyTextSecondary)
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .background(Color.White.copy(alpha = 0.75f), RoundedCornerShape(999.dp)),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(item.ratio.coerceIn(0f, 1f))
                                    .height(10.dp)
                                    .background(item.color, RoundedCornerShape(999.dp)),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatChip(title: String, value: String, emoji: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.76f), MoneyBackground.copy(alpha = 0.88f))),
                RoundedCornerShape(20.dp),
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.labelSmall, color = MoneyTextSecondary)
                Text(value, style = MaterialTheme.typography.titleMedium, color = MoneyTextPrimary)
            }
            AmbientOrb(modifier = Modifier.size(36.dp), emoji = emoji, color = MoneyPeach)
        }
    }
}

private fun categoryColor(colorToken: String?): Color {
    return when (colorToken) {
        "category_food" -> Color(0xFF75AA78)
        "category_coffee" -> Color(0xFF9A7966)
        "category_transport" -> Color(0xFF6FA9C6)
        "category_daily" -> Color(0xFFE1B85F)
        else -> Color(0xFF94A4A7)
    }
}
