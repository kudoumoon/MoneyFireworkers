package com.moneyfireworkers.paytrack.feature.category

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LibraryAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyBackground
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyButter
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyMint
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyPrimary
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyPrimarySoft
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyTextPrimary
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyTextSecondary
import com.moneyfireworkers.paytrack.domain.model.Category
import com.moneyfireworkers.paytrack.domain.repository.CategoryRepository
import com.moneyfireworkers.paytrack.domain.repository.SettingsRepository
import com.moneyfireworkers.paytrack.feature.common.AmbientOrb
import com.moneyfireworkers.paytrack.feature.common.AppScreenHeader
import com.moneyfireworkers.paytrack.feature.common.CategoryBackdropPalette
import com.moneyfireworkers.paytrack.feature.common.GentleScreen
import com.moneyfireworkers.paytrack.feature.common.GlassCard
import com.moneyfireworkers.paytrack.feature.common.LocalAppLanguage
import com.moneyfireworkers.paytrack.feature.common.categoryEmoji
import com.moneyfireworkers.paytrack.feature.common.categoryVector
import com.moneyfireworkers.paytrack.feature.common.pick
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private data class CategoryUiState(
    val categories: List<Category> = emptyList(),
    val message: String? = null,
)

private class CategoryViewModel(
    private val categoryRepository: CategoryRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            categoryRepository.observeAll().collect { categories ->
                _uiState.value = _uiState.value.copy(categories = categories)
            }
        }
    }

    fun createCategory(name: String, successMessage: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val nextOrder = (_uiState.value.categories.maxOfOrNull { it.sortOrder } ?: 0) + 1
            categoryRepository.create(
                Category(
                    name = trimmed,
                    iconToken = "other",
                    colorToken = "category_other",
                    sortOrder = nextOrder,
                    isEnabled = true,
                    isSystem = false,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
            _uiState.update { it.copy(message = successMessage) }
        }
    }

    fun renameCategory(category: Category, newName: String, successMessage: String) {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            categoryRepository.update(category.copy(name = trimmed, updatedAt = System.currentTimeMillis()))
            _uiState.update { it.copy(message = successMessage) }
        }
    }

    fun disableCategory(category: Category, successMessage: String) {
        if (category.isSystem) return
        viewModelScope.launch {
            categoryRepository.update(category.copy(isEnabled = false, updatedAt = System.currentTimeMillis()))
            _uiState.update { it.copy(message = successMessage) }
        }
    }
}

class CategoryViewModelFactory(
    private val categoryRepository: CategoryRepository,
    @Suppress("unused")
    private val settingsRepository: SettingsRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoryViewModel(categoryRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

@Composable
fun CategoryRoute(viewModelFactory: CategoryViewModelFactory) {
    val language = LocalAppLanguage.current
    val viewModel: CategoryViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()
    var creating by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }

    Scaffold(containerColor = Color.Transparent) { innerPadding ->
        GentleScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            palette = CategoryBackdropPalette,
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
                        eyebrow = language.pick("CATEGORY · 生活分区", "CATEGORY · Life buckets"),
                        title = language.pick("给每种花费一个温柔去处 🧺", "Give each expense a softer place 🧺"),
                        subtitle = language.pick(
                            "系统分类负责稳定，自定义分类则更贴近你真实生活里的那些小习惯。",
                            "System categories keep things stable, while custom ones stay closer to your real routines."
                        ),
                    )
                }
                item {
                    GlassCard {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(language.pick("分类小整理", "Category tidy-up"), style = MaterialTheme.typography.titleLarge, color = MoneyTextPrimary)
                                    Text(
                                        language.pick("把常用支出摆放顺手一点，记账时会轻松很多。", "Make your frequent expenses easier to reach so logging feels lighter."),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MoneyTextSecondary,
                                    )
                                }
                                AmbientOrb(emoji = "🪴", color = MoneyButter)
                            }
                            FilledTonalButton(
                                onClick = { creating = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MoneyPrimarySoft,
                                    contentColor = MoneyPrimary,
                                ),
                            ) {
                                Icon(Icons.Outlined.LibraryAdd, contentDescription = null)
                                Text(language.pick("新建一个分类", "Create a category"))
                            }
                            if (!uiState.message.isNullOrBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color.White.copy(alpha = 0.62f),
                                ) {
                                    Text(
                                        text = uiState.message.orEmpty(),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MoneyTextSecondary,
                                    )
                                }
                            }
                        }
                    }
                }
                items(uiState.categories.filter { it.isEnabled }, key = { it.id }) { category ->
                    GlassCard(onClick = { editingCategory = category }) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(
                                            Brush.linearGradient(listOf(MoneyMint.copy(alpha = 0.85f), Color.White.copy(alpha = 0.9f))),
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = categoryVector(category.iconToken),
                                        contentDescription = null,
                                        tint = MoneyPrimary,
                                        modifier = Modifier.padding(12.dp),
                                    )
                                    Text(
                                        text = categoryEmoji(category.iconToken),
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(top = 6.dp, end = 6.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(category.name, style = MaterialTheme.typography.titleMedium, color = MoneyTextPrimary)
                                    Text(
                                        if (category.isSystem) {
                                            language.pick("系统分类，负责稳定识别", "System category for stable recognition")
                                        } else {
                                            language.pick("自定义分类，更贴近你的习惯", "Custom category, closer to your routine")
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MoneyTextSecondary,
                                    )
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = { editingCategory = category }) {
                                    Icon(Icons.Outlined.Edit, contentDescription = null, tint = MoneyTextSecondary)
                                    Text(language.pick("编辑", "Edit"))
                                }
                                if (!category.isSystem) {
                                    TextButton(
                                        onClick = {
                                            viewModel.disableCategory(
                                                category = category,
                                                successMessage = language.pick("分类已隐藏。", "Category hidden."),
                                            )
                                        },
                                    ) {
                                        Icon(Icons.Outlined.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                        Text(language.pick("隐藏", "Hide"))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (creating) {
        CategoryDialog(
            title = language.pick("新建分类", "Create category"),
            initialName = "",
            fieldLabel = language.pick("分类名称", "Category name"),
            confirmLabel = language.pick("保存", "Save"),
            dismissLabel = language.pick("取消", "Cancel"),
            onDismiss = { creating = false },
            onConfirm = {
                viewModel.createCategory(
                    name = it,
                    successMessage = language.pick("新分类已经加进来了。", "Category created."),
                )
                creating = false
            },
        )
    }

    editingCategory?.let { category ->
        CategoryDialog(
            title = language.pick("编辑分类", "Edit category"),
            initialName = category.name,
            fieldLabel = language.pick("分类名称", "Category name"),
            confirmLabel = language.pick("保存", "Save"),
            dismissLabel = language.pick("取消", "Cancel"),
            onDismiss = { editingCategory = null },
            onConfirm = {
                viewModel.renameCategory(
                    category = category,
                    newName = it,
                    successMessage = language.pick("分类名称已更新。", "Category renamed."),
                )
                editingCategory = null
            },
        )
    }
}

@Composable
private fun CategoryDialog(
    title: String,
    initialName: String,
    fieldLabel: String,
    confirmLabel: String,
    dismissLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var text by remember(initialName) { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = { onConfirm(text) }) { Text(confirmLabel) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(dismissLabel) } },
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(fieldLabel) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.8f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.65f),
                ),
            )
        },
        containerColor = MoneyBackground,
        shape = RoundedCornerShape(28.dp),
    )
}
