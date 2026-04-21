package com.moneyfireworkers.paytrack.domain.model

data class AppSettings(
    val monthlyExpectedExpenseInCent: Long = 0L,
    val paymentReminderEnabled: Boolean = true,
    val dailyReminderEnabled: Boolean = false,
    val appLanguage: AppLanguage = AppLanguage.CHINESE,
)
