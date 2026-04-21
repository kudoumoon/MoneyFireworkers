package com.moneyfireworkers.paytrack.feature.addexpense

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyBackground
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyGlow
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyMint
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyPeach
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyPrimary
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyPrimaryDeep
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyPrimarySoft
import com.moneyfireworkers.paytrack.core.ui.theme.MoneySurface
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyTextPrimary
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyTextSecondary
import com.moneyfireworkers.paytrack.domain.model.AppLanguage
import com.moneyfireworkers.paytrack.domain.model.Category
import com.moneyfireworkers.paytrack.domain.model.SpendingEmotion
import com.moneyfireworkers.paytrack.domain.repository.CategoryRepository
import com.moneyfireworkers.paytrack.domain.repository.SettingsRepository
import com.moneyfireworkers.paytrack.domain.usecase.expense.RecordExpenseUseCase
import com.moneyfireworkers.paytrack.feature.common.AddBackdropPalette
import com.moneyfireworkers.paytrack.feature.common.AmbientOrb
import com.moneyfireworkers.paytrack.feature.common.AppScreenHeader
import com.moneyfireworkers.paytrack.feature.common.GentleScreen
import com.moneyfireworkers.paytrack.feature.common.GlassCard
import com.moneyfireworkers.paytrack.feature.common.LocalAppLanguage
import com.moneyfireworkers.paytrack.feature.common.categoryEmoji
import com.moneyfireworkers.paytrack.feature.common.categoryVector
import com.moneyfireworkers.paytrack.feature.common.emotionColor
import com.moneyfireworkers.paytrack.feature.common.emotionEmoji
import com.moneyfireworkers.paytrack.feature.common.emotionLabel
import com.moneyfireworkers.paytrack.feature.common.pick
import java.math.BigDecimal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private data class AddExpenseUiState(
    val amountInput: String = "",
    val selectedCategoryId: Long? = null,
    val selectedEmotion: SpendingEmotion = SpendingEmotion.NEUTRAL,
    val noteInput: String = "",
    val categories: List<Category> = emptyList(),
    val isSaving: Boolean = false,
    val message: String? = null,
    val error: String? = null,
)

private class AddExpenseViewModel(
    private val categoryRepository: CategoryRepository,
    private val recordExpenseUseCase: RecordExpenseUseCase,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            categoryRepository.observeAllEnabled().collect { categories ->
                _uiState.update { current ->
                    current.copy(
                        categories = categories,
                        selectedCategoryId = current.selectedCategoryId ?: categories.firstOrNull()?.id,
                    )
                }
            }
        }
    }

    fun updateAmount(value: String) {
        _uiState.update { it.copy(amountInput = value, error = null, message = null) }
    }

    fun selectCategory(categoryId: Long) {
        _uiState.update { it.copy(selectedCategoryId = categoryId, error = null) }
    }

    fun selectEmotion(emotion: SpendingEmotion) {
        _uiState.update { it.copy(selectedEmotion = emotion, error = null) }
    }

    fun updateNote(value: String) {
        _uiState.update { it.copy(noteInput = value, message = null) }
    }

    fun save(onSaved: () -> Unit) {
        viewModelScope.launch {
            val language = settingsRepository.getSettings().appLanguage
            val current = _uiState.value
            val amountInCent = parseAmountToCent(current.amountInput)
            val categoryId = current.selectedCategoryId

            if (amountInCent == null || amountInCent <= 0L) {
                _uiState.update {
                    it.copy(error = language.pick("请输入正确的支出金额", "Enter a valid expense amount"))
                }
                return@launch
            }
            if (categoryId == null) {
                _uiState.update {
                    it.copy(error = language.pick("请选择一个分类", "Select a category"))
                }
                return@launch
            }

            _uiState.update { it.copy(isSaving = true, error = null, message = null) }
            try {
                recordExpenseUseCase(
                    amountInCent = amountInCent,
                    categoryId = categoryId,
                    emotion = current.selectedEmotion,
                    note = current.noteInput,
                    occurredAt = System.currentTimeMillis(),
                    now = System.currentTimeMillis(),
                )
                _uiState.value = AddExpenseUiState(
                    categories = current.categories,
                    selectedCategoryId = current.categories.firstOrNull()?.id,
                    message = language.pick("这笔支出已经记好了。", "Expense saved."),
                )
                onSaved()
            } catch (throwable: Throwable) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = throwable.message ?: language.pick("保存失败，请稍后再试", "Save failed. Please try again."),
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(isSaving = false) }
        }
    }

    private fun parseAmountToCent(input: String): Long? {
        val amount = input.trim().toBigDecimalOrNull() ?: return null
        return amount.multiply(BigDecimal(100)).longValueExact()
    }
}

class AddExpenseViewModelFactory(
    private val categoryRepository: CategoryRepository,
    private val recordExpenseUseCase: RecordExpenseUseCase,
    private val settingsRepository: SettingsRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddExpenseViewModel(categoryRepository, recordExpenseUseCase, settingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

@Composable
fun AddExpenseRoute(
    viewModelFactory: AddExpenseViewModelFactory,
    onSaved: () -> Unit,
) {
    val language = LocalAppLanguage.current
    val viewModel: AddExpenseViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    GentleScreen(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        palette = AddBackdropPalette,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AppScreenHeader(
                eyebrow = language.pick("ADD · 今天花了什么", "ADD · What did you spend today"),
                title = language.pick("记一笔，让心里更有数 ✍️", "Log it once and feel clearer ✍️"),
                subtitle = language.pick(
                    "金额、分类和心情一起记下来，这页应该像顺手写便签，而不是在填后台表单。",
                    "Capture amount, category, and feeling together. This page should feel like a quick note, not a cold form."
                ),
            )

            GlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(language.pick("金额", "Amount"), style = MaterialTheme.typography.titleLarge, color = MoneyTextPrimary)
                            Text(
                                language.pick("先把这次花了多少写清楚。", "Start with how much you spent this time."),
                                style = MaterialTheme.typography.bodySmall,
                                color = MoneyTextSecondary,
                            )
                        }
                        AmbientOrb(emoji = "💸", color = MoneyPeach)
                    }

                    Surface(
                        shape = RoundedCornerShape(26.dp),
                        color = Color.White.copy(alpha = 0.70f),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.White.copy(alpha = 0.84f), MoneyBackground.copy(alpha = 0.88f)),
                                    ),
                                )
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(
                                language.pick("这次的小花费", "This little spend"),
                                style = MaterialTheme.typography.labelLarge,
                                color = MoneyTextSecondary,
                            )
                            OutlinedTextField(
                                value = uiState.amountInput,
                                onValueChange = viewModel::updateAmount,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(language.pick("输入金额", "Enter amount")) },
                                placeholder = { Text(language.pick("例如 32.50", "For example 32.50")) },
                                prefix = {
                                    Text(
                                        text = if (language == AppLanguage.CHINESE) "¥" else "$",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MoneyPrimaryDeep,
                                    )
                                },
                                textStyle = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MoneyTextPrimary,
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = MoneyTextPrimary,
                                    unfocusedTextColor = MoneyTextPrimary,
                                    focusedContainerColor = Color.White.copy(alpha = 0.92f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.76f),
                                    focusedBorderColor = MoneyPrimary,
                                    unfocusedBorderColor = MoneyPrimary.copy(alpha = 0.22f),
                                    focusedLabelColor = MoneyPrimaryDeep,
                                    unfocusedLabelColor = MoneyTextSecondary,
                                    focusedPlaceholderColor = MoneyTextSecondary.copy(alpha = 0.72f),
                                    unfocusedPlaceholderColor = MoneyTextSecondary.copy(alpha = 0.72f),
                                    cursorColor = MoneyPrimaryDeep,
                                ),
                            )
                            Text(
                                text = language.pick(
                                    "金额文字、占位和光标都做了加深处理，输入时会更清楚。",
                                    "The amount text, placeholder, and cursor are all boosted for clearer readability."
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MoneyTextSecondary,
                            )
                        }
                    }
                }
            }

            Text(language.pick("分类", "Category"), style = MaterialTheme.typography.titleMedium, color = MoneyTextPrimary)

            CategoryGrid(
                categories = uiState.categories,
                selectedCategoryId = uiState.selectedCategoryId,
                onSelect = viewModel::selectCategory,
            )

            Text(
                language.pick("这笔花费带来的感受", "How this expense felt"),
                style = MaterialTheme.typography.titleMedium,
                color = MoneyTextPrimary,
            )

            EmotionRow(
                selectedEmotion = uiState.selectedEmotion,
                onSelect = viewModel::selectEmotion,
                language = language,
            )

            GlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(language.pick("备注", "Note"), style = MaterialTheme.typography.titleLarge, color = MoneyTextPrimary)
                            Text(
                                language.pick("可以写下场景，比如午饭、打车、临时嘴馋。", "Add a little context like lunch, ride, or a sudden craving."),
                                style = MaterialTheme.typography.bodySmall,
                                color = MoneyTextSecondary,
                            )
                        }
                        AmbientOrb(emoji = "📝", color = MoneyGlow)
                    }
                    OutlinedTextField(
                        value = uiState.noteInput,
                        onValueChange = viewModel::updateNote,
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        label = { Text(language.pick("留一点生活气息", "Leave a little life context")) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.76f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.62f),
                        ),
                    )

                    if (!uiState.error.isNullOrBlank()) {
                        StatusBanner(text = uiState.error.orEmpty(), isError = true)
                    }
                    if (!uiState.message.isNullOrBlank()) {
                        StatusBanner(text = uiState.message.orEmpty(), isError = false)
                    }

                    Button(
                        onClick = { viewModel.save(onSaved) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isSaving,
                    ) {
                        Text(
                            if (uiState.isSaving) {
                                language.pick("保存中…", "Saving…")
                            } else {
                                language.pick("保存这笔支出", "Save this expense")
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryGrid(
    categories: List<Category>,
    selectedCategoryId: Long?,
    onSelect: (Long) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        categories.chunked(3).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                rowItems.forEach { category ->
                    val selected = category.id == selectedCategoryId
                    Card(
                        modifier = Modifier.weight(1f),
                        onClick = { onSelect(category.id) },
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) MoneyPrimarySoft else MoneySurface.copy(alpha = 0.88f),
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 6.dp else 2.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp, horizontal = 12.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                if (selected) MoneyPrimary.copy(alpha = 0.22f) else MoneyMint.copy(alpha = 0.72f),
                                                Color.White.copy(alpha = 0.82f),
                                            ),
                                        ),
                                        CircleShape,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = categoryVector(category.iconToken),
                                    contentDescription = null,
                                    tint = if (selected) MoneyPrimary else MoneyTextSecondary,
                                )
                                Text(
                                    text = categoryEmoji(category.iconToken),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(top = 2.dp, end = 2.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MoneyTextPrimary,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                    }
                }

                repeat(3 - rowItems.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun EmotionRow(
    selectedEmotion: SpendingEmotion,
    onSelect: (SpendingEmotion) -> Unit,
    language: AppLanguage,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SpendingEmotion.entries.forEach { emotion ->
            val selected = emotion == selectedEmotion
            Card(
                modifier = Modifier.weight(1f),
                onClick = { onSelect(emotion) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selected) emotionColor(emotion).copy(alpha = 0.16f) else MoneySurface.copy(alpha = 0.88f),
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 6.dp else 2.dp),
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(emotionColor(emotion).copy(alpha = 0.18f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = emotionEmoji(emotion), style = MaterialTheme.typography.bodyLarge)
                    }
                    Text(
                        text = emotionLabel(emotion, language),
                        color = emotionColor(emotion),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBanner(text: String, isError: Boolean) {
    val color = if (isError) MaterialTheme.colorScheme.error else MoneyPrimary
    val icon = if (isError) Icons.Outlined.ErrorOutline else Icons.Outlined.CheckCircle

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = color.copy(alpha = 0.10f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = color)
            Text(text, color = color, style = MaterialTheme.typography.bodySmall)
        }
    }
}
