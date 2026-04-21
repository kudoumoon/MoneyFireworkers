package com.moneyfireworkers.paytrack.domain.repository

import com.moneyfireworkers.paytrack.domain.model.AppSettings
import com.moneyfireworkers.paytrack.domain.model.AppLanguage
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSettings(): Flow<AppSettings>
    suspend fun getSettings(): AppSettings
    suspend fun updateMonthlyExpectedExpense(amountInCent: Long)
    suspend fun updatePaymentReminder(enabled: Boolean)
    suspend fun updateDailyReminder(enabled: Boolean)
    suspend fun updateAppLanguage(language: AppLanguage)
}
