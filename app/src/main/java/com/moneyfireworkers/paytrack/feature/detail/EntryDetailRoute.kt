package com.moneyfireworkers.paytrack.feature.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyGlow
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyMint
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyPeach
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyPrimary
import com.moneyfireworkers.paytrack.core.ui.theme.MoneySurface
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyTextPrimary
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyTextSecondary
import com.moneyfireworkers.paytrack.domain.repository.CategoryRepository
import com.moneyfireworkers.paytrack.domain.repository.LedgerRepository
import com.moneyfireworkers.paytrack.domain.repository.SettingsRepository
import com.moneyfireworkers.paytrack.feature.common.AmbientOrb
import com.moneyfireworkers.paytrack.feature.common.GentleScreen
import com.moneyfireworkers.paytrack.feature.common.GlassCard
import com.moneyfireworkers.paytrack.feature.common.HomeBackdropPalette
import com.moneyfireworkers.paytrack.feature.common.LocalAppLanguage
import com.moneyfireworkers.paytrack.feature.common.entryStatusLabel
import com.moneyfireworkers.paytrack.feature.common.formatDateTime
import com.moneyfireworkers.paytrack.feature.common.formatMoney
import com.moneyfireworkers.paytrack.feature.common.pick
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

private data class EntryDetailUiState(
    val isLoading: Boolean = true,
    val title: String = "",
    val amountLabel: String = "",
    val categoryName: String = "",
    val occurredAtLabel: String = "",
    val statusLabel: String = "",
    val note: String? = null,
    val explanation: String? = null,
    val notFoundMessage: String? = null,
)

private class EntryDetailViewModel(
    entryId: Long,
    ledgerRepository: LedgerRepository,
    categoryRepository: CategoryRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EntryDetailUiState())
    val uiState: StateFlow<EntryDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                ledgerRepository.observeById(entryId),
                categoryRepository.observeAllEnabled(),
                settingsRepository.observeSettings(),
            ) { entry, categories, settings ->
                val language = settings.appLanguage
                val categoryNames = categories.associateBy({ it.id }, { it.name })
                if (entry == null) {
                    EntryDetailUiState(
                        isLoading = false,
                        notFoundMessage = language.pick("没有找到这笔账单。", "This expense could not be found."),
                    )
                } else {
                    EntryDetailUiState(
                        isLoading = false,
                        title = entry.merchantName.ifBlank { language.pick("未命名商户", "Unnamed merchant") },
                        amountLabel = formatMoney(entry.amountInCent, language),
                        categoryName = categoryNames[entry.categoryIdFinal ?: entry.categoryIdSuggested]
                            ?: language.pick("未分类", "Uncategorized"),
                        occurredAtLabel = formatDateTime(entry.occurredAt, language),
                        statusLabel = entryStatusLabel(entry.entryStatus, language),
                        note = entry.note,
                        explanation = entry.classificationExplanationSnapshot,
                    )
                }
            }.collectLatest { mapped ->
                _uiState.value = mapped
            }
        }
    }
}

class EntryDetailViewModelFactory(
    private val entryId: Long,
    private val ledgerRepository: LedgerRepository,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EntryDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EntryDetailViewModel(
                entryId = entryId,
                ledgerRepository = ledgerRepository,
                categoryRepository = categoryRepository,
                settingsRepository = settingsRepository,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

@Composable
fun EntryDetailRoute(
    entryId: Long,
    onBack: () -> Unit,
    viewModelFactory: EntryDetailViewModelFactory,
) {
    val viewModel: EntryDetailViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()

    EntryDetailScreen(
        uiState = uiState,
        onBack = onBack,
    )
}

@Composable
private fun EntryDetailScreen(
    uiState: EntryDetailUiState,
    onBack: () -> Unit,
) {
    val language = LocalAppLanguage.current

    Scaffold(containerColor = Color.Transparent) { innerPadding ->
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
                    DetailHero(
                        title = uiState.title,
                        amountLabel = uiState.amountLabel,
                        onBack = onBack,
                        language = language,
                    )
                }

                when {
                    uiState.isLoading -> {
                        item {
                            GlassCard {
                                Text(
                                    text = language.pick("正在读取账单详情...", "Loading expense details..."),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MoneyTextSecondary,
                                )
                            }
                        }
                    }

                    !uiState.notFoundMessage.isNullOrBlank() -> {
                        item {
                            GlassCard {
                                Text(
                                    text = uiState.notFoundMessage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MoneyTextSecondary,
                                )
                            }
                        }
                    }

                    else -> {
                        item {
                            InfoBlock(
                                icon = Icons.Outlined.Storefront,
                                title = language.pick("商户", "Merchant"),
                                value = uiState.title,
                                glow = MoneyMint,
                            )
                        }
                        item {
                            InfoBlock(
                                icon = Icons.Outlined.Sell,
                                title = language.pick("分类", "Category"),
                                value = uiState.categoryName,
                                glow = MoneyPeach,
                            )
                        }
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                CompactInfoBlock(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Outlined.ReceiptLong,
                                    title = language.pick("状态", "Status"),
                                    value = uiState.statusLabel,
                                )
                                CompactInfoBlock(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Outlined.Event,
                                    title = language.pick("时间", "Time"),
                                    value = uiState.occurredAtLabel,
                                )
                            }
                        }
                        if (!uiState.explanation.isNullOrBlank()) {
                            item {
                                InfoBlock(
                                    icon = Icons.Outlined.Inventory2,
                                    title = language.pick("分类说明", "Why it was classified this way"),
                                    value = uiState.explanation,
                                    glow = MoneyGlow,
                                )
                            }
                        }
                        if (!uiState.note.isNullOrBlank()) {
                            item {
                                InfoBlock(
                                    icon = Icons.Outlined.ReceiptLong,
                                    title = language.pick("备注", "Note"),
                                    value = uiState.note,
                                    glow = MoneyPeach,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailHero(
    title: String,
    amountLabel: String,
    onBack: () -> Unit,
    language: com.moneyfireworkers.paytrack.domain.model.AppLanguage,
) {
    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = null, tint = MoneyPrimary)
                    Text(
                        text = language.pick("返回", "Back"),
                        color = MoneyPrimary,
                        modifier = Modifier.padding(start = 6.dp),
                    )
                }
                AmbientOrb(emoji = "🧾", color = MoneyGlow)
            }
            Text(
                text = language.pick("这一笔花销的样子", "A closer look at this expense"),
                style = MaterialTheme.typography.headlineSmall,
                color = MoneyTextPrimary,
            )
            Text(
                text = language.pick(
                    "不用再看一堆黑色方块，这里只保留最重要的信息。",
                    "Only the useful details stay here, without the heavy dashboard feeling."
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MoneyTextSecondary,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            listOf(MoneyPrimary.copy(alpha = 0.92f), MoneyMint.copy(alpha = 0.88f)),
                        ),
                        shape = RoundedCornerShape(28.dp),
                    )
                    .padding(22.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White.copy(alpha = 0.9f),
                    )
                    Text(
                        text = amountLabel,
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoBlock(
    icon: ImageVector,
    title: String,
    value: String,
    glow: Color,
) {
    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(glow.copy(alpha = 0.56f), MoneySurface.copy(alpha = 0.92f)),
                        ),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = MoneyPrimary)
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(title, style = MaterialTheme.typography.labelLarge, color = MoneyPrimary)
                Text(value, style = MaterialTheme.typography.bodyLarge, color = MoneyTextPrimary)
            }
        }
    }
}

@Composable
private fun CompactInfoBlock(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    value: String,
) {
    GlassCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(MoneyGlow.copy(alpha = 0.58f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = MoneyPrimary)
            }
            Text(title, style = MaterialTheme.typography.labelLarge, color = MoneyPrimary)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = MoneyTextPrimary)
        }
    }
}
