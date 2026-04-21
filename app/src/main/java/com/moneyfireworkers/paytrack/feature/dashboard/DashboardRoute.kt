package com.moneyfireworkers.paytrack.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowOutward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moneyfireworkers.paytrack.core.model.EntryStatus
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyAccent
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyBackground
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyBerry
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyButter
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyGlow
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyMint
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyPeach
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyPrimary
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyPrimaryDeep
import com.moneyfireworkers.paytrack.core.ui.theme.MoneySky
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyTextPrimary
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyTextSecondary
import com.moneyfireworkers.paytrack.domain.model.AppLanguage
import com.moneyfireworkers.paytrack.domain.model.AppSettings
import com.moneyfireworkers.paytrack.domain.model.Category
import com.moneyfireworkers.paytrack.domain.model.LedgerEntry
import com.moneyfireworkers.paytrack.domain.model.PendingAction
import com.moneyfireworkers.paytrack.domain.model.SpendingEmotion
import com.moneyfireworkers.paytrack.domain.repository.CategoryRepository
import com.moneyfireworkers.paytrack.domain.repository.LedgerRepository
import com.moneyfireworkers.paytrack.domain.repository.PendingRepository
import com.moneyfireworkers.paytrack.domain.repository.SettingsRepository
import com.moneyfireworkers.paytrack.feature.common.AmbientOrb
import com.moneyfireworkers.paytrack.feature.common.AppScreenHeader
import com.moneyfireworkers.paytrack.feature.common.EmotionPill
import com.moneyfireworkers.paytrack.feature.common.GentleScreen
import com.moneyfireworkers.paytrack.feature.common.GlassCard
import com.moneyfireworkers.paytrack.feature.common.HomeBackdropPalette
import com.moneyfireworkers.paytrack.feature.common.LocalAppLanguage
import com.moneyfireworkers.paytrack.feature.common.breathingStatusLabel
import com.moneyfireworkers.paytrack.feature.common.categoryEmoji
import com.moneyfireworkers.paytrack.feature.common.categoryVector
import com.moneyfireworkers.paytrack.feature.common.dayGroupLabel
import com.moneyfireworkers.paytrack.feature.common.emotionColor
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

private data class DashboardItemUi(
    val id: Long,
    val title: String,
    val amountLabel: String,
    val categoryName: String,
    val categoryEmoji: String,
    val categoryIcon: ImageVector,
    val emotionLabel: String,
    val emotionColor: Color,
)

private data class DashboardUiState(
    val breathingRoomLabel: String = formatMoney(0L),
    val breathingRoomStatus: String = "",
    val breathingRoomTone: BreathingRoomTone = BreathingRoomTone.Comfortable,
    val monthlyExpenseLabel: String = formatMoney(0L),
    val monthlyExpectedLabel: String = formatMoney(0L),
    val pendingExpenseLabel: String = formatMoney(0L),
    val pendingCountLabel: String = "0",
    val latestPending: DashboardPendingUi? = null,
    val groupedItems: List<Pair<String, List<DashboardItemUi>>> = emptyList(),
)

private data class DashboardPendingUi(
    val pendingActionId: Long,
    val ledgerEntryId: Long,
    val merchantName: String,
    val amountLabel: String,
    val categoryName: String,
    val explanation: String?,
)

private enum class BreathingRoomTone(
    val orbEmoji: String,
    val badgePrefix: String,
) {
    Comfortable("🍃", "舒展"),
    Tight("🌤️", "收一收"),
    Danger("🫧", "要抱紧"),
}

private class DashboardViewModel(
    ledgerRepository: LedgerRepository,
    pendingRepository: PendingRepository,
    categoryRepository: CategoryRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                ledgerRepository.observeAll(),
                pendingRepository.observeActiveActions(),
                categoryRepository.observeAllEnabled(),
                settingsRepository.observeSettings(),
            ) { entries, pendingActions, categories, settings ->
                buildUiState(entries, pendingActions, categories, settings)
            }.collect { _uiState.value = it }
        }
    }

    private fun buildUiState(
        entries: List<LedgerEntry>,
        pendingActions: List<PendingAction>,
        categories: List<Category>,
        settings: AppSettings,
    ): DashboardUiState {
        val language = settings.appLanguage
        val categoryMap = categories.associateBy { it.id }
        val monthStart = Instant.ofEpochMilli(System.currentTimeMillis())
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .withDayOfMonth(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val confirmedEntries = entries.filter {
            it.entryStatus == EntryStatus.CONFIRMED_AUTO || it.entryStatus == EntryStatus.CONFIRMED_WITH_EDIT
        }
        val pendingEntries = entries.filter { it.entryStatus == EntryStatus.PENDING_CONFIRMATION }
        val monthlyExpense = confirmedEntries.filter { it.occurredAt >= monthStart }.sumOf { it.amountInCent }
        val breathingRoom = settings.monthlyExpectedExpenseInCent - monthlyExpense
        val breathingRoomTone = when {
            breathingRoom <= 0L -> BreathingRoomTone.Danger
            settings.monthlyExpectedExpenseInCent <= 0L -> BreathingRoomTone.Tight
            breathingRoom <= settings.monthlyExpectedExpenseInCent * 18 / 100 -> BreathingRoomTone.Danger
            breathingRoom <= settings.monthlyExpectedExpenseInCent * 40 / 100 -> BreathingRoomTone.Tight
            else -> BreathingRoomTone.Comfortable
        }

        return DashboardUiState(
            breathingRoomLabel = formatMoney(breathingRoom, language),
            breathingRoomStatus = breathingStatusLabel(breathingRoom, settings.monthlyExpectedExpenseInCent, language),
            breathingRoomTone = breathingRoomTone,
            monthlyExpenseLabel = formatMoney(monthlyExpense, language),
            monthlyExpectedLabel = formatMoney(settings.monthlyExpectedExpenseInCent, language),
            pendingExpenseLabel = formatMoney(pendingEntries.sumOf { it.amountInCent }, language),
            pendingCountLabel = pendingActions.size.toString(),
            latestPending = pendingActions.firstNotNullOfOrNull { pending ->
                val ledger = entries.firstOrNull { it.id == pending.ledgerEntryId } ?: return@firstNotNullOfOrNull null
                val category = categoryMap[ledger.categoryIdFinal ?: ledger.categoryIdSuggested]
                DashboardPendingUi(
                    pendingActionId = pending.id,
                    ledgerEntryId = ledger.id,
                    merchantName = ledger.merchantName.ifBlank { language.pick("待补全商户", "Needs merchant") },
                    amountLabel = formatMoney(ledger.amountInCent, language),
                    categoryName = category?.name ?: language.pick("待分类", "Needs category"),
                    explanation = ledger.classificationExplanationSnapshot,
                )
            },
            groupedItems = confirmedEntries
                .sortedByDescending { it.occurredAt }
                .map { entry ->
                    val category = categoryMap[entry.categoryIdFinal ?: entry.categoryIdSuggested]
                    DashboardItemUi(
                        id = entry.id,
                        title = entry.note?.takeIf { it.isNotBlank() } ?: entry.merchantName,
                        amountLabel = formatMoney(entry.amountInCent, language),
                        categoryName = category?.name ?: language.pick("未分类", "Uncategorized"),
                        categoryEmoji = categoryEmoji(category?.iconToken),
                        categoryIcon = categoryVector(category?.iconToken),
                        emotionLabel = emotionLabel(entry.emotion ?: SpendingEmotion.NEUTRAL, language),
                        emotionColor = emotionColor(entry.emotion ?: SpendingEmotion.NEUTRAL),
                    ) to dayGroupLabel(entry.occurredAt, language)
                }
                .groupBy({ it.second }, { it.first })
                .toList(),
        )
    }
}

class DashboardViewModelFactory(
    private val ledgerRepository: LedgerRepository,
    private val pendingRepository: PendingRepository,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(ledgerRepository, pendingRepository, categoryRepository, settingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

@Composable
fun DashboardRoute(
    viewModelFactory: DashboardViewModelFactory,
    onAddExpense: () -> Unit,
    onEntryClick: (Long) -> Unit,
    onPendingClick: (Long, Long) -> Unit,
) {
    val language = LocalAppLanguage.current
    val viewModel: DashboardViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MoneyBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        GentleScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            palette = HomeBackdropPalette,
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    AppScreenHeader(
                        eyebrow = language.pick("HOME · 今日账本", "HOME · Today's ledger"),
                        title = language.pick("今天也慢慢记，别着急 ☁️", "Take it slow today, no rush ☁️"),
                        subtitle = language.pick(
                            "把预算、花费和一点点生活痕迹放在同一页里，看起来会安心很多。",
                            "Keep budget, spending, and little traces of daily life on one page so the month feels easier to hold."
                        ),
                    )
                }
                item {
                    GlassCard(onClick = onAddExpense) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    language.pick("记一笔小开销 🌿", "Log a small spend 🌿"),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MoneyTextPrimary,
                                )
                                Text(
                                    language.pick(
                                        "奶茶、打车、午饭都可以顺手放进来，动作越轻，越容易坚持。",
                                        "Milk tea, rides, and lunch can all go here. The lighter the action feels, the easier it is to keep going."
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MoneyTextSecondary,
                                )
                            }
                            AmbientOrb(emoji = "✍️", color = MoneyPeach)
                        }
                    }
                }
                item {
                    BreathingHeroCard(uiState = uiState, language = language)
                }
                uiState.latestPending?.let { pending ->
                    item {
                        PendingEntryCard(
                            pending = pending,
                            pendingCountLabel = uiState.pendingCountLabel,
                            language = language,
                            onClick = { onPendingClick(pending.pendingActionId, pending.ledgerEntryId) },
                        )
                    }
                }
                if (uiState.groupedItems.isEmpty()) {
                    item {
                        GlassCard(onClick = onAddExpense) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    language.pick("还没有新的支出记录", "No expenses yet"),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MoneyTextPrimary,
                                )
                                Text(
                                    language.pick(
                                        "去“添加”里记下第一笔，首页就会开始长出属于这个月的生活轮廓。",
                                        "Log your first expense from Add and this screen will start sketching the shape of your month."
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MoneyTextSecondary,
                                )
                            }
                        }
                    }
                } else {
                    uiState.groupedItems.forEach { group ->
                        item {
                            Text(
                                text = group.first,
                                style = MaterialTheme.typography.titleMedium,
                                color = MoneyTextPrimary,
                                modifier = Modifier.padding(top = 8.dp, start = 4.dp),
                            )
                        }
                        items(group.second, key = { it.id }) { item ->
                            ExpenseRow(
                                item = item,
                                onClick = { onEntryClick(item.id) },
                                language = language,
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(90.dp)) }
            }
        }
    }
}

@Composable
private fun PendingEntryCard(
    pending: DashboardPendingUi,
    pendingCountLabel: String,
    language: AppLanguage,
    onClick: () -> Unit,
) {
    GlassCard(onClick = onClick) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = language.pick("待确认支出", "Pending confirmation"),
                    style = MaterialTheme.typography.titleMedium,
                    color = MoneyTextPrimary,
                )
                EmotionPill(
                    label = language.pick("共 $pendingCountLabel 笔", "$pendingCountLabel waiting"),
                    color = MoneyButter,
                )
            }
            Text(
                text = pending.merchantName,
                style = MaterialTheme.typography.titleLarge,
                color = MoneyTextPrimary,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = pending.amountLabel,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = pending.categoryName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MoneyTextSecondary,
                    )
                }
                AmbientOrb(emoji = "⏳", color = MoneyPeach)
            }
            pending.explanation?.takeIf { it.isNotBlank() }?.let { explanation ->
                Text(
                    text = explanation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MoneyTextSecondary,
                )
            }
        }
    }
}

@Composable
private fun HeroCard(
    uiState: DashboardUiState,
    language: AppLanguage,
) {
    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(MoneyPrimaryDeep, MoneyPrimary, MoneyGlow),
                        ),
                    )
                    .padding(22.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                language.pick("本月还剩的呼吸感", "Remaining breathing room"),
                                color = Color.White.copy(alpha = 0.78f),
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Text(
                                text = uiState.breathingRoomLabel,
                                style = MaterialTheme.typography.displayMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        AmbientOrb(emoji = "🍃", color = Color.White.copy(alpha = 0.35f))
                    }
                    EmotionPill(label = uiState.breathingRoomStatus, color = Color.White)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                MiniMetric(language.pick("已经花掉", "Spent"), uiState.monthlyExpenseLabel, "🧾", Modifier.weight(1f))
                MiniMetric(language.pick("原本预算", "Budget"), uiState.monthlyExpectedLabel, "🌤️", Modifier.weight(1f))
            }
            MiniMetric(language.pick("待确认支出", "Pending"), uiState.pendingExpenseLabel, "⏳", Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun BreathingHeroCard(
    uiState: DashboardUiState,
    language: AppLanguage,
) {
    val gradientColors = when (uiState.breathingRoomTone) {
        BreathingRoomTone.Comfortable -> listOf(MoneyPrimaryDeep, MoneyPrimary, MoneyGlow)
        BreathingRoomTone.Tight -> listOf(Color(0xFFA28C50), MoneyButter, Color(0xFFF6EDD3))
        BreathingRoomTone.Danger -> listOf(Color(0xFFAF655D), MoneyBerry, Color(0xFFF4D9D4))
    }
    val heroTextColor = if (uiState.breathingRoomTone == BreathingRoomTone.Comfortable) Color.White else Color(0xFF3E332D)
    val heroSecondaryColor = heroTextColor.copy(alpha = if (uiState.breathingRoomTone == BreathingRoomTone.Comfortable) 0.78f else 0.72f)
    val orbColor = when (uiState.breathingRoomTone) {
        BreathingRoomTone.Comfortable -> Color.White.copy(alpha = 0.35f)
        BreathingRoomTone.Tight -> Color(0xFFFFF3CF)
        BreathingRoomTone.Danger -> Color(0xFFF8E0DB)
    }

    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Brush.linearGradient(colors = gradientColors))
                    .padding(22.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = language.pick("本月还剩的呼吸感", "Remaining breathing room"),
                                color = heroSecondaryColor,
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Text(
                                text = uiState.breathingRoomLabel,
                                style = MaterialTheme.typography.displayMedium,
                                color = heroTextColor,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        AmbientOrb(
                            emoji = uiState.breathingRoomTone.orbEmoji,
                            color = orbColor,
                        )
                    }
                    EmotionPill(
                        label = "${uiState.breathingRoomTone.badgePrefix} ${uiState.breathingRoomStatus}",
                        color = if (uiState.breathingRoomTone == BreathingRoomTone.Comfortable) Color.White else MoneySky,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                MiniMetric(language.pick("已经花掉", "Spent"), uiState.monthlyExpenseLabel, "🧾", Modifier.weight(1f))
                MiniMetric(language.pick("原本预算", "Budget"), uiState.monthlyExpectedLabel, "🪴", Modifier.weight(1f))
            }
            MiniMetric(language.pick("待确认支出", "Pending"), uiState.pendingExpenseLabel, "⏳", Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun MiniMetric(title: String, value: String, emoji: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.76f), MoneyBackground.copy(alpha = 0.92f)),
                ),
            )
            .padding(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.bodySmall, color = MoneyTextSecondary)
                Text(value, style = MaterialTheme.typography.titleLarge, color = MoneyTextPrimary)
            }
            AmbientOrb(modifier = Modifier.size(38.dp), emoji = emoji, color = MoneyMint)
        }
    }
}

@Composable
private fun ExpenseRow(
    item: DashboardItemUi,
    onClick: () -> Unit,
    language: AppLanguage,
) {
    GlassCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(MoneyMint.copy(alpha = 0.85f), Color.White.copy(alpha = 0.9f)),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(item.categoryIcon, contentDescription = null, tint = MoneyAccent)
                    Text(
                        text = item.categoryEmoji,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 5.dp, end = 6.dp),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(item.title, style = MaterialTheme.typography.titleMedium, color = MoneyTextPrimary)
                    Text(item.categoryName, style = MaterialTheme.typography.bodySmall, color = MoneyTextSecondary)
                    EmotionPill(label = item.emotionLabel, color = item.emotionColor)
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(item.amountLabel, style = MaterialTheme.typography.titleLarge, color = MoneyTextPrimary)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        language.pick("详情", "Details"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MoneyTextSecondary,
                    )
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.72f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Outlined.ArrowOutward,
                            contentDescription = null,
                            tint = MoneyTextSecondary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}
