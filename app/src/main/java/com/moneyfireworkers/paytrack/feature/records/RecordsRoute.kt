package com.moneyfireworkers.paytrack.feature.records

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moneyfireworkers.paytrack.core.model.EntryStatus
import com.moneyfireworkers.paytrack.domain.repository.CategoryRepository
import com.moneyfireworkers.paytrack.domain.repository.LedgerRepository
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private data class RecordsUiState(
    val isLoading: Boolean = true,
    val items: List<RecordItemUiModel> = emptyList(),
)

private data class RecordItemUiModel(
    val entryId: Long,
    val merchantName: String,
    val amountLabel: String,
    val categoryName: String,
    val statusLabel: String,
    val occurredAtLabel: String,
)

private sealed interface RecordsEvent {
    data class NavigateToDetail(val entryId: Long) : RecordsEvent
}

private class RecordsViewModel(
    ledgerRepository: LedgerRepository,
    categoryRepository: CategoryRepository,
) : ViewModel() {
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.SIMPLIFIED_CHINESE)
    private val timeFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm")
    private val zoneId = ZoneId.systemDefault()

    private val _uiState = MutableStateFlow(RecordsUiState())
    val uiState: StateFlow<RecordsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<RecordsEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<RecordsEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(
                ledgerRepository.observeAll(),
                categoryRepository.observeAllEnabled(),
            ) { entries, categories ->
                val categoryNames = categories.associateBy({ it.id }, { it.name })
                entries
                    .sortedByDescending { entry -> entry.occurredAt }
                    .map { entry ->
                        RecordItemUiModel(
                            entryId = entry.id,
                            merchantName = entry.merchantName.ifBlank { "未命名商户" },
                            amountLabel = currencyFormatter.format(entry.amountInCent / 100.0),
                            categoryName = categoryNames[entry.categoryIdFinal ?: entry.categoryIdSuggested]
                                ?: "待分类",
                            statusLabel = entry.entryStatus.toLabel(),
                            occurredAtLabel = Instant.ofEpochMilli(entry.occurredAt)
                                .atZone(zoneId)
                                .format(timeFormatter),
                        )
                    }
            }.collectLatest { items ->
                _uiState.value = RecordsUiState(
                    isLoading = false,
                    items = items,
                )
            }
        }
    }

    fun openDetail(entryId: Long) {
        _events.tryEmit(RecordsEvent.NavigateToDetail(entryId))
    }

    private fun EntryStatus.toLabel(): String {
        return when (this) {
            EntryStatus.DRAFT -> "草稿"
            EntryStatus.PENDING_CONFIRMATION -> "待确认"
            EntryStatus.CONFIRMED_AUTO -> "已确认"
            EntryStatus.CONFIRMED_WITH_EDIT -> "已修改确认"
            EntryStatus.CANCELLED -> "已取消"
        }
    }
}

class RecordsViewModelFactory(
    private val ledgerRepository: LedgerRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecordsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecordsViewModel(
                ledgerRepository = ledgerRepository,
                categoryRepository = categoryRepository,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

@Composable
fun RecordsRoute(
    onBack: () -> Unit,
    onOpenDetail: (Long) -> Unit,
    viewModelFactory: RecordsViewModelFactory,
) {
    val viewModel: RecordsViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is RecordsEvent.NavigateToDetail -> onOpenDetail(event.entryId)
            }
        }
    }

    RecordsScreen(
        uiState = uiState,
        onBack = onBack,
        onEntryClick = viewModel::openDetail,
    )
}

@Composable
private fun RecordsScreen(
    uiState: RecordsUiState,
    onBack: () -> Unit,
    onEntryClick: (Long) -> Unit,
) {
    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .safeDrawingPadding(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                HeaderCard(
                    title = "账单列表",
                    subtitle = "这里展示本地数据库里的真实账单状态。",
                    onBack = onBack,
                )
            }
            if (uiState.isLoading) {
                item { Text("正在加载账单...") }
            } else if (uiState.items.isEmpty()) {
                item {
                    Text(
                        text = "还没有账单，先从首页添加一笔付款。",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            } else {
                items(uiState.items, key = { item -> item.entryId }) { item ->
                    RecordCard(
                        item = item,
                        onClick = { onEntryClick(item.entryId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderCard(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                TextButton(onClick = onBack) {
                    Text("返回")
                }
            }
        }
    }
}

@Composable
private fun RecordCard(
    item: RecordItemUiModel,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = item.merchantName,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = item.amountLabel,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = "${item.categoryName} / ${item.occurredAtLabel}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = item.statusLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
