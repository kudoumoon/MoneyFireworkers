package com.moneyfireworkers.paytrack.feature.addpayment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.moneyfireworkers.paytrack.core.model.DedupStatus
import com.moneyfireworkers.paytrack.domain.model.ProcessPaymentResult
import com.moneyfireworkers.paytrack.domain.usecase.process.ProcessFormPaymentUseCase
import com.moneyfireworkers.paytrack.domain.usecase.process.ProcessImagePaymentUseCase
import com.moneyfireworkers.paytrack.domain.usecase.process.ProcessTextPaymentUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private enum class AddPaymentInputMode {
    TEXT,
    FORM,
    IMAGE,
}

private data class AddPaymentUiState(
    val selectedMode: AddPaymentInputMode = AddPaymentInputMode.TEXT,
    val textInput: String = "",
    val amountInput: String = "",
    val merchantInput: String = "",
    val imageUriInput: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val helperMessage: String? = null,
)

private sealed interface AddPaymentEvent {
    data object NavigateBack : AddPaymentEvent

    data class NavigateToPending(
        val pendingActionId: Long,
        val ledgerEntryId: Long,
    ) : AddPaymentEvent
}

private class AddPaymentViewModel(
    private val processTextPaymentUseCase: ProcessTextPaymentUseCase,
    private val processFormPaymentUseCase: ProcessFormPaymentUseCase,
    private val processImagePaymentUseCase: ProcessImagePaymentUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddPaymentUiState())
    val uiState: StateFlow<AddPaymentUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AddPaymentEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<AddPaymentEvent> = _events.asSharedFlow()

    fun selectMode(mode: AddPaymentInputMode) {
        _uiState.update { current ->
            current.copy(
                selectedMode = mode,
                errorMessage = null,
                helperMessage = null,
            )
        }
    }

    fun updateTextInput(value: String) {
        _uiState.update { current -> current.copy(textInput = value, errorMessage = null) }
    }

    fun updateAmountInput(value: String) {
        _uiState.update { current -> current.copy(amountInput = value, errorMessage = null) }
    }

    fun updateMerchantInput(value: String) {
        _uiState.update { current -> current.copy(merchantInput = value, errorMessage = null) }
    }

    fun updateImageUriInput(value: String) {
        _uiState.update { current -> current.copy(imageUriInput = value, errorMessage = null) }
    }

    fun submit() {
        viewModelScope.launch {
            val currentState = _uiState.value
            _uiState.update { current ->
                current.copy(
                    isSubmitting = true,
                    errorMessage = null,
                    helperMessage = null,
                )
            }

            try {
                when (currentState.selectedMode) {
                    AddPaymentInputMode.TEXT -> submitText(currentState.textInput)
                    AddPaymentInputMode.FORM -> submitForm(
                        amount = currentState.amountInput,
                        merchant = currentState.merchantInput,
                    )
                    AddPaymentInputMode.IMAGE -> submitImage(currentState.imageUriInput)
                }
            } catch (throwable: Throwable) {
                _uiState.update { current ->
                    current.copy(
                        errorMessage = throwable.message ?: "保存失败，请稍后再试。",
                    )
                }
            } finally {
                _uiState.update { current -> current.copy(isSubmitting = false) }
            }
        }
    }

    private suspend fun submitText(rawText: String) {
        require(rawText.isNotBlank()) { "请输入付款文本。" }
        handleProcessResult(
            processTextPaymentUseCase(
                rawText = rawText,
                now = System.currentTimeMillis(),
            ),
            successMessage = "付款事件已生成。",
        )
    }

    private suspend fun submitForm(amount: String, merchant: String) {
        handleProcessResult(
            processFormPaymentUseCase(
                amountRaw = amount,
                merchantRaw = merchant,
                occurredAt = System.currentTimeMillis(),
                now = System.currentTimeMillis(),
            ),
            successMessage = "付款信息已保存。",
        )
    }

    private suspend fun submitImage(imageUri: String) {
        require(imageUri.isNotBlank()) { "请先填写图片地址或标识。" }
        val result = processImagePaymentUseCase(
            imageUri = imageUri,
            now = System.currentTimeMillis(),
        )
        _uiState.update { current ->
            current.copy(
                helperMessage = "图片凭证已保存。第一版会先保留凭证，后续再接入自动识别。",
            )
        }
        if (result.pendingAction != null && result.ledgerEntry != null) {
            _events.tryEmit(
                AddPaymentEvent.NavigateToPending(
                    pendingActionId = result.pendingAction.id,
                    ledgerEntryId = result.ledgerEntry.id,
                ),
            )
        }
    }

    private fun handleProcessResult(result: ProcessPaymentResult, successMessage: String) {
        val pendingAction = result.pendingAction
        val ledgerEntry = result.ledgerEntry
        if (pendingAction != null && ledgerEntry != null) {
            _events.tryEmit(
                AddPaymentEvent.NavigateToPending(
                    pendingActionId = pendingAction.id,
                    ledgerEntryId = ledgerEntry.id,
                ),
            )
            return
        }

        _uiState.update { current ->
            current.copy(
                helperMessage = if (result.dedupStatus == DedupStatus.CONFIRMED_DUPLICATE) {
                    "检测到重复付款，系统没有重复入账。"
                } else {
                    successMessage
                },
            )
        }
    }
}

class AddPaymentViewModelFactory(
    private val processTextPaymentUseCase: ProcessTextPaymentUseCase,
    private val processFormPaymentUseCase: ProcessFormPaymentUseCase,
    private val processImagePaymentUseCase: ProcessImagePaymentUseCase,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddPaymentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddPaymentViewModel(
                processTextPaymentUseCase = processTextPaymentUseCase,
                processFormPaymentUseCase = processFormPaymentUseCase,
                processImagePaymentUseCase = processImagePaymentUseCase,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

@Composable
fun AddPaymentRoute(
    onBack: () -> Unit,
    onNavigateToPending: (pendingActionId: Long, ledgerEntryId: Long) -> Unit,
    viewModelFactory: AddPaymentViewModelFactory,
) {
    val viewModel: AddPaymentViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                AddPaymentEvent.NavigateBack -> onBack()
                is AddPaymentEvent.NavigateToPending -> onNavigateToPending(
                    event.pendingActionId,
                    event.ledgerEntryId,
                )
            }
        }
    }

    AddPaymentScreen(
        uiState = uiState,
        onBack = onBack,
        onModeSelected = viewModel::selectMode,
        onTextChanged = viewModel::updateTextInput,
        onAmountChanged = viewModel::updateAmountInput,
        onMerchantChanged = viewModel::updateMerchantInput,
        onImageUriChanged = viewModel::updateImageUriInput,
        onSubmit = viewModel::submit,
    )
}

@Composable
private fun AddPaymentScreen(
    uiState: AddPaymentUiState,
    onBack: () -> Unit,
    onModeSelected: (AddPaymentInputMode) -> Unit,
    onTextChanged: (String) -> Unit,
    onAmountChanged: (String) -> Unit,
    onMerchantChanged: (String) -> Unit,
    onImageUriChanged: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .safeDrawingPadding(),
            state = rememberLazyListState(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                HeaderCard(
                    title = "添加付款",
                    subtitle = "先做最小真实流程：文本、表单和图片凭证都能进入本地账本链路。",
                    onBack = onBack,
                )
            }
            item {
                ModeSelector(
                    selectedMode = uiState.selectedMode,
                    onModeSelected = onModeSelected,
                )
            }
            item {
                when (uiState.selectedMode) {
                    AddPaymentInputMode.TEXT -> InputCard(
                        title = "文本输入",
                        subtitle = "示例：瑞幸咖啡 18 元",
                    ) {
                        OutlinedTextField(
                            value = uiState.textInput,
                            onValueChange = onTextChanged,
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4,
                            label = { Text("付款文本") },
                        )
                    }
                    AddPaymentInputMode.FORM -> InputCard(
                        title = "手动填写",
                        subtitle = "金额和商户会直接走真实记账流程。",
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = uiState.amountInput,
                                onValueChange = onAmountChanged,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("金额") },
                            )
                            OutlinedTextField(
                                value = uiState.merchantInput,
                                onValueChange = onMerchantChanged,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("商户") },
                            )
                        }
                    }
                    AddPaymentInputMode.IMAGE -> InputCard(
                        title = "图片凭证",
                        subtitle = "第一版先保存图片引用，后续再接入自动识别。",
                    ) {
                        OutlinedTextField(
                            value = uiState.imageUriInput,
                            onValueChange = onImageUriChanged,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("图片地址或标识") },
                            supportingText = {
                                Text("例如 content://、file:// 或你当前拍照后的标识。")
                            },
                        )
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!uiState.errorMessage.isNullOrBlank()) {
                        Text(
                            text = uiState.errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    if (!uiState.helperMessage.isNullOrBlank()) {
                        Text(
                            text = uiState.helperMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Button(
                        onClick = onSubmit,
                        enabled = !uiState.isSubmitting,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (uiState.isSubmitting) "提交中..." else "保存并进入流程")
                    }
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
private fun ModeSelector(
    selectedMode: AddPaymentInputMode,
    onModeSelected: (AddPaymentInputMode) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PaymentModeButton(
                label = "文本",
                selected = selectedMode == AddPaymentInputMode.TEXT,
                onClick = { onModeSelected(AddPaymentInputMode.TEXT) },
                modifier = Modifier.weight(1f),
            )
            PaymentModeButton(
                label = "表单",
                selected = selectedMode == AddPaymentInputMode.FORM,
                onClick = { onModeSelected(AddPaymentInputMode.FORM) },
                modifier = Modifier.weight(1f),
            )
            PaymentModeButton(
                label = "图片",
                selected = selectedMode == AddPaymentInputMode.IMAGE,
                onClick = { onModeSelected(AddPaymentInputMode.IMAGE) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PaymentModeButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 12.dp),
    ) {
        Text(if (selected) "[$label]" else label)
    }
}

@Composable
private fun InputCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            content()
        }
    }
}
