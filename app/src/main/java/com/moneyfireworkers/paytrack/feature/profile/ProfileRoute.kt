package com.moneyfireworkers.paytrack.feature.profile

import android.content.Context
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moneyfireworkers.paytrack.core.model.EntryStatus
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyBackground
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyGlow
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyPeach
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyPrimary
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyPrimarySoft
import com.moneyfireworkers.paytrack.core.ui.theme.MoneySurface
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyTextPrimary
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyTextSecondary
import com.moneyfireworkers.paytrack.domain.model.AppLanguage
import com.moneyfireworkers.paytrack.domain.model.AppSettings
import com.moneyfireworkers.paytrack.domain.repository.LedgerRepository
import com.moneyfireworkers.paytrack.domain.repository.NotificationRecognitionLogRepository
import com.moneyfireworkers.paytrack.domain.repository.SettingsRepository
import com.moneyfireworkers.paytrack.feature.common.AmbientOrb
import com.moneyfireworkers.paytrack.feature.common.AppScreenHeader
import com.moneyfireworkers.paytrack.feature.common.GentleScreen
import com.moneyfireworkers.paytrack.feature.common.GlassCard
import com.moneyfireworkers.paytrack.feature.common.LocalAppLanguage
import com.moneyfireworkers.paytrack.feature.common.ProfileBackdropPalette
import com.moneyfireworkers.paytrack.feature.common.formatMoney
import com.moneyfireworkers.paytrack.feature.common.pick
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.math.BigDecimal

private data class ProfileUiState(
    val settings: AppSettings = AppSettings(),
    val budgetInput: String = "",
    val message: String? = null,
    val notificationLogs: List<NotificationLogUi> = emptyList(),
)

private data class NotificationLogUi(
    val status: String,
    val title: String,
    val detail: String,
)

private class ProfileViewModel(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val ledgerRepository: LedgerRepository,
    private val notificationRecognitionLogRepository: NotificationRecognitionLogRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.observeSettings().collect { settings ->
                _uiState.update { current ->
                    current.copy(
                        settings = settings,
                        budgetInput = if (current.budgetInput.isBlank()) {
                            (settings.monthlyExpectedExpenseInCent / 100).toString()
                        } else {
                            current.budgetInput
                        },
                    )
                }
            }
        }
        viewModelScope.launch {
            notificationRecognitionLogRepository.observeRecent(6).collect { logs ->
                _uiState.update { current ->
                    current.copy(
                        notificationLogs = logs.map { log ->
                            NotificationLogUi(
                                status = log.recognitionStatus,
                                title = log.title ?: log.packageName,
                                detail = buildString {
                                    append(log.contentText.ifBlank { "empty" })
                                    log.amountInCent?.let { append("\n金额: ").append(formatMoney(it, current.settings.appLanguage)) }
                                    log.merchantName?.let { append("\n商户: ").append(it) }
                                    log.failureReason?.let { append("\n原因: ").append(it) }
                                },
                            )
                        },
                    )
                }
            }
        }
    }

    fun updateBudgetInput(value: String) {
        _uiState.update { it.copy(budgetInput = value, message = null) }
    }

    fun saveBudget() {
        viewModelScope.launch {
            val language = _uiState.value.settings.appLanguage
            val amountInCent = parseAmountToCent(_uiState.value.budgetInput)
            if (amountInCent == null) {
                _uiState.update { it.copy(message = language.pick("请输入有效预算", "Enter a valid budget")) }
                return@launch
            }
            settingsRepository.updateMonthlyExpectedExpense(amountInCent)
            _uiState.update { it.copy(message = language.pick("本月预算已更新。", "Monthly budget updated.")) }
        }
    }

    fun togglePaymentReminder(enabled: Boolean) {
        viewModelScope.launch {
            val language = _uiState.value.settings.appLanguage
            settingsRepository.updatePaymentReminder(enabled)
            _uiState.update {
                it.copy(
                    message = if (enabled) {
                        language.pick("已开启支付后提醒。", "Payment reminder enabled.")
                    } else {
                        language.pick("已关闭支付后提醒。", "Payment reminder disabled.")
                    },
                )
            }
        }
    }

    fun toggleDailyReminder(enabled: Boolean) {
        viewModelScope.launch {
            val language = _uiState.value.settings.appLanguage
            settingsRepository.updateDailyReminder(enabled)
            _uiState.update {
                it.copy(
                    message = if (enabled) {
                        language.pick("已开启每日提醒。", "Daily reminder enabled.")
                    } else {
                        language.pick("已关闭每日提醒。", "Daily reminder disabled.")
                    },
                )
            }
        }
    }

    fun updateLanguage(language: AppLanguage) {
        viewModelScope.launch {
            settingsRepository.updateAppLanguage(language)
            _uiState.update {
                it.copy(
                    message = language.pick("界面语言已切换为中文。", "App language switched to English."),
                )
            }
        }
    }

    fun exportData() {
        viewModelScope.launch {
            val language = _uiState.value.settings.appLanguage
            val entries = ledgerRepository.getRecent(500)
                .filter { it.entryStatus == EntryStatus.CONFIRMED_AUTO || it.entryStatus == EntryStatus.CONFIRMED_WITH_EDIT }
            val exportDir = File(context.filesDir, "exports").apply { mkdirs() }
            val file = File(exportDir, "expenses-export.csv")
            val content = buildString {
                appendLine("id,amountInCent,merchantName,note,emotion,occurredAt")
                entries.forEach { entry ->
                    appendLine(
                        listOf(
                            entry.id,
                            entry.amountInCent,
                            entry.merchantName.replace(",", " "),
                            (entry.note ?: "").replace(",", " "),
                            entry.emotion?.name ?: "",
                            entry.occurredAt,
                        ).joinToString(","),
                    )
                }
            }
            file.writeText(content)
            _uiState.update {
                it.copy(
                    message = language.pick("导出完成：${file.absolutePath}", "Export complete: ${file.absolutePath}"),
                )
            }
        }
    }

    private fun parseAmountToCent(input: String): Long? {
        val amount = input.trim().toBigDecimalOrNull() ?: return null
        return amount.multiply(BigDecimal(100)).longValueExact()
    }
}

class ProfileViewModelFactory(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val ledgerRepository: LedgerRepository,
    private val notificationRecognitionLogRepository: NotificationRecognitionLogRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(
                context,
                settingsRepository,
                ledgerRepository,
                notificationRecognitionLogRepository,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

@Composable
fun ProfileRoute(viewModelFactory: ProfileViewModelFactory) {
    val language = LocalAppLanguage.current
    val viewModel: ProfileViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(containerColor = Color.Transparent) { innerPadding ->
        GentleScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            palette = ProfileBackdropPalette,
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
                        eyebrow = language.pick("PROFILE · 我的小设置", "PROFILE · My setup"),
                        title = language.pick("把提醒、预算和导出慢慢安顿好 🫖", "Settle reminders, budget, and export gently 🫖"),
                        subtitle = language.pick(
                            "这里不需要像后台，更像你的记账抽屉，放常用也放安心。",
                            "This does not need to feel like an admin panel. It can feel more like a small drawer for your habits."
                        ),
                    )
                }
                item {
                    ProfileSection(
                        icon = Icons.Outlined.Savings,
                        title = language.pick("本月预算", "Monthly budget"),
                        subtitle = language.pick("首页和统计会用它来计算你的预算呼吸感。", "Home and Stats use this to calculate your remaining room."),
                        emoji = "🌤️",
                    ) {
                        OutlinedTextField(
                            value = uiState.budgetInput,
                            onValueChange = viewModel::updateBudgetInput,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(language.pick("预算金额", "Budget amount")) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.78f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.62f),
                            ),
                        )
                        FilledTonalButton(
                            onClick = viewModel::saveBudget,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MoneyPrimarySoft,
                                contentColor = MoneyPrimary,
                            ),
                        ) {
                            Text(language.pick("保存这个月预算", "Save this month's budget"))
                        }
                    }
                }
                item {
                    ProfileSection(
                        icon = Icons.Outlined.Notifications,
                        title = language.pick("提醒设置", "Reminders"),
                        subtitle = language.pick("先把开关整理好，后续再接系统通知。", "Keep the switches tidy now and connect system notifications later."),
                        emoji = "🔔",
                    ) {
                        ProfileSwitch(
                            title = language.pick("支付后提醒我记账", "Remind me after payment"),
                            checked = uiState.settings.paymentReminderEnabled,
                            onCheckedChange = viewModel::togglePaymentReminder,
                        )
                        ProfileSwitch(
                            title = language.pick("每天轻轻提醒一次", "A gentle daily reminder"),
                            checked = uiState.settings.dailyReminderEnabled,
                            onCheckedChange = viewModel::toggleDailyReminder,
                        )
                    }
                }
                item {
                    ProfileSection(
                        icon = Icons.Outlined.Language,
                        title = language.pick("语言", "Language"),
                        subtitle = language.pick("中英文可以随时切换，主要页面会立刻响应。", "Switch between Chinese and English and the main screens update immediately."),
                        emoji = "🌍",
                    ) {
                        LanguageSelector(
                            currentLanguage = uiState.settings.appLanguage,
                            onSelect = viewModel::updateLanguage,
                        )
                    }
                }
                item {
                    ProfileSection(
                        icon = Icons.Outlined.CloudDownload,
                        title = language.pick("数据导出", "Data export"),
                        subtitle = language.pick("把当前确认过的支出整理成一份本地 CSV。", "Export confirmed expenses into a local CSV file."),
                        emoji = "📦",
                    ) {
                        FilledTonalButton(
                            onClick = viewModel::exportData,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MoneyGlow,
                                contentColor = MoneyTextPrimary,
                            ),
                        ) {
                            Text(language.pick("导出支出数据", "Export expense data"))
                        }
                    }
                }
                item {
                    ProfileSection(
                        icon = Icons.Outlined.Tune,
                        title = language.pick("状态小记", "Status note"),
                        subtitle = language.pick("这里会显示最近一次设置动作的结果。", "This shows the result of your latest settings action."),
                        emoji = "🪄",
                    ) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color.White.copy(alpha = 0.62f),
                        ) {
                            Text(
                                text = uiState.message ?: language.pick("暂时没有新的系统提示。", "No new system messages."),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MoneyTextSecondary,
                            )
                        }
                    }
                }
                item {
                    ProfileSection(
                        icon = Icons.Outlined.Notifications,
                        title = language.pick("通知调试", "Notification debug"),
                        subtitle = language.pick("最近收到的微信通知和识别结果会显示在这里。", "Recent WeChat notifications and recognition results show here."),
                        emoji = "🧪",
                    ) {
                        if (uiState.notificationLogs.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color.White.copy(alpha = 0.62f),
                            ) {
                                Text(
                                    text = language.pick("还没有收到任何可记录的通知。", "No recordable notifications yet."),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MoneyTextSecondary,
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                uiState.notificationLogs.forEach { log ->
                                    Surface(
                                        shape = RoundedCornerShape(18.dp),
                                        color = Color.White.copy(alpha = 0.68f),
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            Text(log.status, style = MaterialTheme.typography.labelLarge, color = MoneyPrimary)
                                            Text(log.title, style = MaterialTheme.typography.bodyMedium, color = MoneyTextPrimary)
                                            Text(log.detail, style = MaterialTheme.typography.bodySmall, color = MoneyTextSecondary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileSection(
    icon: ImageVector,
    title: String,
    subtitle: String,
    emoji: String,
    content: @Composable () -> Unit,
) {
    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(MoneyPeach.copy(alpha = 0.72f), Color.White.copy(alpha = 0.82f)),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = MoneyPrimary, modifier = Modifier.padding(10.dp))
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, color = MoneyTextPrimary)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MoneyTextSecondary)
                }
                AmbientOrb(emoji = emoji, color = MoneyGlow)
            }
            content()
        }
    }
}

@Composable
private fun ProfileSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.56f), RoundedCornerShape(18.dp))
            .toggleable(
                value = checked,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, color = MoneyTextPrimary, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = null)
    }
}

@Composable
private fun LanguageSelector(
    currentLanguage: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LanguageOptionCard(
            modifier = Modifier.weight(1f),
            selected = currentLanguage == AppLanguage.CHINESE,
            label = "中文",
            subLabel = "Chinese",
            onClick = { onSelect(AppLanguage.CHINESE) },
        )
        LanguageOptionCard(
            modifier = Modifier.weight(1f),
            selected = currentLanguage == AppLanguage.ENGLISH,
            label = "English",
            subLabel = "English",
            onClick = { onSelect(AppLanguage.ENGLISH) },
        )
    }
}

@Composable
private fun LanguageOptionCard(
    modifier: Modifier = Modifier,
    selected: Boolean,
    label: String,
    subLabel: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MoneyPrimarySoft else MoneySurface.copy(alpha = 0.86f),
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 6.dp else 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(label, style = MaterialTheme.typography.titleMedium, color = MoneyTextPrimary)
            Text(subLabel, style = MaterialTheme.typography.bodySmall, color = MoneyTextSecondary)
        }
    }
}
