package com.moneyfireworkers.paytrack.data.repository

import android.content.Context
import com.moneyfireworkers.paytrack.domain.model.AppLanguage
import com.moneyfireworkers.paytrack.domain.model.AppSettings
import com.moneyfireworkers.paytrack.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepositoryImpl(
    context: Context,
) : SettingsRepository {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val settingsState = MutableStateFlow(readSettings())

    override fun observeSettings(): Flow<AppSettings> = settingsState.asStateFlow()

    override suspend fun getSettings(): AppSettings = settingsState.value

    override suspend fun updateMonthlyExpectedExpense(amountInCent: Long) {
        preferences.edit()
            .putLong(KEY_MONTHLY_EXPECTED_EXPENSE, amountInCent)
            .apply()
        settingsState.value = settingsState.value.copy(monthlyExpectedExpenseInCent = amountInCent)
    }

    override suspend fun updatePaymentReminder(enabled: Boolean) {
        preferences.edit()
            .putBoolean(KEY_PAYMENT_REMINDER, enabled)
            .apply()
        settingsState.value = settingsState.value.copy(paymentReminderEnabled = enabled)
    }

    override suspend fun updateDailyReminder(enabled: Boolean) {
        preferences.edit()
            .putBoolean(KEY_DAILY_REMINDER, enabled)
            .apply()
        settingsState.value = settingsState.value.copy(dailyReminderEnabled = enabled)
    }

    override suspend fun updateAppLanguage(language: AppLanguage) {
        preferences.edit()
            .putString(KEY_APP_LANGUAGE, language.storageValue)
            .apply()
        settingsState.value = settingsState.value.copy(appLanguage = language)
    }

    private fun readSettings(): AppSettings {
        return AppSettings(
            monthlyExpectedExpenseInCent = preferences.getLong(KEY_MONTHLY_EXPECTED_EXPENSE, 0L),
            paymentReminderEnabled = preferences.getBoolean(KEY_PAYMENT_REMINDER, true),
            dailyReminderEnabled = preferences.getBoolean(KEY_DAILY_REMINDER, false),
            appLanguage = AppLanguage.fromStorageValue(preferences.getString(KEY_APP_LANGUAGE, null)),
        )
    }

    private companion object {
        const val PREFS_NAME = "paytrack_settings"
        const val KEY_MONTHLY_EXPECTED_EXPENSE = "monthly_expected_expense"
        const val KEY_PAYMENT_REMINDER = "payment_reminder"
        const val KEY_DAILY_REMINDER = "daily_reminder"
        const val KEY_APP_LANGUAGE = "app_language"
    }
}
