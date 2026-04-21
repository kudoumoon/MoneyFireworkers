package com.moneyfireworkers.paytrack.feature.pending

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
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
import com.moneyfireworkers.paytrack.core.model.PendingStatus
import com.moneyfireworkers.paytrack.domain.repository.CategoryRepository
import com.moneyfireworkers.paytrack.domain.repository.LedgerRepository
import com.moneyfireworkers.paytrack.domain.repository.PendingRepository
import com.moneyfireworkers.paytrack.domain.usecase.pending.ConfirmPendingEntryUseCase
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

private data class PendingDetailUiState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val title: String = "",
    val amountLabel: String = "",
    val categoryName: String = "",
    val occurredAtLabel: String = "",
    val explanation: String? = null,
    val statusLabel: String = "",
    val canConfirm: Boolean = false,
    val errorMessage: String? = null,
)

private sealed interface PendingDetailEvent {
    data class NavigateToEntryDetail(val entryId: Long) : PendingDetailEvent
}

private class PendingDetailViewModel(
    private val pendingActionId: Long,
    private val ledgerEntryId: Long,
    pendingRepository: PendingRepository,
    ledgerRepository: LedgerRepository,
    categoryRepository: CategoryRepository,
    private val confirmPendingEntryUseCase: ConfirmPendingEntryUseCase,
) : ViewModel() {
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.SIMPLIFIED_CHINESE)
    private val timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    private val zoneId = ZoneId.systemDefault()

    private val _uiState = MutableStateFlow(PendingDetailUiState())
    val uiState: StateFlow<PendingDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PendingDetailEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<PendingDetailEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(
                pendingRepository.observeById(pendingActionId),
                ledgerRepository.observeById(ledgerEntryId),
                categoryRepository.observeAllEnabled(),
            ) { pendingAction, ledgerEntry, categories ->
                val categoryNames = categories.associateBy({ it.id }, { it.name })
                if (pendingAction == null || ledgerEntry == null) {
                    PendingDetailUiState(
                        isLoading = false,
                        errorMessage = "没有找到待确认记录。",
                    )
                } else {
                    PendingDetailUiState(
                        isLoading = false,
                        isSubmitting = _uiState.value.isSubmitting,
                        title = ledgerEntry.merchantName.ifBlank { "待补充商户" },
                        amountLabel = currencyFormatter.format(ledgerEntry.amountInCent / 100.0),
                        categoryName = categoryNames[ledgerEntry.categoryIdFinal ?: ledgerEntry.categoryIdSuggested]
                            ?: "待分类",
                        occurredAtLabel = Instant.ofEpochMilli(ledgerEntry.occurredAt)
                            .atZone(zoneId)
                            .format(timeFormatter),
                        explanation = ledgerEntry.classificationExplanationSnapshot,
                        statusLabel = pendingAction.pendingStatus.toLabel(),
                        canConfirm = pendingAction.pendingStatus != PendingStatus.RESOLVED,
                        errorMessage = null,
                    )
                }
            }.collectLatest { mapped ->
                _uiState.value = mapped
            }
        }
    }

    fun confirm() {
        if (!_uiState.value.canConfirm) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSubmitting = true,
                errorMessage = null,
            )
            try {
                confirmPendingEntryUseCase(
                    pendingActionId = pendingActionId,
                    ledgerEntryId = ledgerEntryId,
                    now = System.currentTimeMillis(),
                )
                _events.tryEmit(PendingDetailEvent.NavigateToEntryDetail(ledgerEntryId))
            } catch (throwable: Throwable) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = throwable.message ?: "确认失败，请稍后再试。",
                )
            } finally {
                _uiState.value = _uiState.value.copy(isSubmitting = false)
            }
        }
    }

    private fun PendingStatus.toLabel(): String {
        return when (this) {
            PendingStatus.ACTIVE -> "待确认"
            PendingStatus.SHOWN_IN_APP -> "已展示"
            PendingStatus.NOTIFIED -> "已提醒"
            PendingStatus.RESUMED -> "已恢复"
            PendingStatus.RESOLVED -> "已处理"
            PendingStatus.EXPIRED -> "已过期"
        }
    }
}

class PendingDetailViewModelFactory(
    private val pendingActionId: Long,
    private val ledgerEntryId: Long,
    private val pendingRepository: PendingRepository,
    private val ledgerRepository: LedgerRepository,
    private val categoryRepository: CategoryRepository,
    private val confirmPendingEntryUseCase: ConfirmPendingEntryUseCase,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PendingDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PendingDetailViewModel(
                pendingActionId = pendingActionId,
                ledgerEntryId = ledgerEntryId,
                pendingRepository = pendingRepository,
                ledgerRepository = ledgerRepository,
                categoryRepository = categoryRepository,
                confirmPendingEntryUseCase = confirmPendingEntryUseCase,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

@Composable
fun PendingDetailRoute(
    onBack: () -> Unit,
    onNavigateToEntryDetail: (Long) -> Unit,
    viewModelFactory: PendingDetailViewModelFactory,
) {
    val viewModel: PendingDetailViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is PendingDetailEvent.NavigateToEntryDetail -> onNavigateToEntryDetail(event.entryId)
            }
        }
    }

    PendingDetailScreen(
        uiState = uiState,
        onBack = onBack,
        onConfirm = viewModel::confirm,
    )
}

@Composable
private fun PendingDetailScreen(
    uiState: PendingDetailUiState,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
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
                HeaderCard(onBack = onBack)
            }
            when {
                uiState.isLoading -> {
                    item { Text("正在恢复待确认账单...") }
                }
                !uiState.errorMessage.isNullOrBlank() -> {
                    item {
                        Text(
                            text = uiState.errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                else -> {
                    item { DetailCard("商户", uiState.title) }
                    item { DetailCard("金额", uiState.amountLabel) }
                    item { DetailCard("分类", uiState.categoryName) }
                    item { DetailCard("时间", uiState.occurredAtLabel) }
                    item { DetailCard("当前状态", uiState.statusLabel) }
                    if (!uiState.explanation.isNullOrBlank()) {
                        item { DetailCard("自动分类说明", uiState.explanation) }
                    }
                    item {
                        Button(
                            onClick = onConfirm,
                            enabled = uiState.canConfirm && !uiState.isSubmitting,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(if (uiState.isSubmitting) "确认中..." else "确认入账")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderCard(onBack: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "处理待确认",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = "这是真正接在本地数据库上的待确认处理页。",
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

@Composable
private fun DetailCard(
    title: String,
    value: String,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
