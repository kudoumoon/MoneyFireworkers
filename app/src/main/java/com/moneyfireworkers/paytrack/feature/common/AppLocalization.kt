package com.moneyfireworkers.paytrack.feature.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.moneyfireworkers.paytrack.core.model.EntryStatus
import com.moneyfireworkers.paytrack.domain.model.AppLanguage
import com.moneyfireworkers.paytrack.domain.model.SpendingEmotion
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.CHINESE }

fun AppLanguage.pick(chinese: String, english: String): String {
    return when (this) {
        AppLanguage.CHINESE -> chinese
        AppLanguage.ENGLISH -> english
    }
}

@Composable
fun ProvideAppLocalization(
    language: AppLanguage,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalAppLanguage provides language, content = content)
}

@Composable
@ReadOnlyComposable
fun localizedText(chinese: String, english: String): String {
    return LocalAppLanguage.current.pick(chinese, english)
}

fun formatMoney(
    amountInCent: Long,
    language: AppLanguage = AppLanguage.CHINESE,
): String {
    return NumberFormat.getCurrencyInstance(language.locale).format(amountInCent / 100.0)
}

fun formatShortDate(
    timestamp: Long,
    language: AppLanguage = AppLanguage.CHINESE,
    zoneId: ZoneId = ZoneId.systemDefault(),
): String {
    val pattern = language.pick("MM-dd", "MMM d")
    return Instant.ofEpochMilli(timestamp)
        .atZone(zoneId)
        .format(DateTimeFormatter.ofPattern(pattern, language.locale))
}

fun formatTime(
    timestamp: Long,
    language: AppLanguage = AppLanguage.CHINESE,
    zoneId: ZoneId = ZoneId.systemDefault(),
): String {
    return Instant.ofEpochMilli(timestamp)
        .atZone(zoneId)
        .format(DateTimeFormatter.ofPattern("HH:mm", language.locale))
}

fun formatDateTime(
    timestamp: Long,
    language: AppLanguage = AppLanguage.CHINESE,
    zoneId: ZoneId = ZoneId.systemDefault(),
): String {
    val pattern = language.pick("yyyy-MM-dd HH:mm", "MMM d, yyyy HH:mm")
    return Instant.ofEpochMilli(timestamp)
        .atZone(zoneId)
        .format(DateTimeFormatter.ofPattern(pattern, language.locale))
}

fun dayGroupLabel(
    timestamp: Long,
    language: AppLanguage = AppLanguage.CHINESE,
    nowMillis: Long = System.currentTimeMillis(),
    zoneId: ZoneId = ZoneId.systemDefault(),
): String {
    val date = Instant.ofEpochMilli(timestamp).atZone(zoneId).toLocalDate()
    val today = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()
    return when {
        date == today -> language.pick("今天", "Today")
        date == today.minusDays(1) -> language.pick("昨天", "Yesterday")
        else -> formatShortDate(timestamp = timestamp, language = language, zoneId = zoneId)
    }
}

fun emotionLabel(
    emotion: SpendingEmotion,
    language: AppLanguage = AppLanguage.CHINESE,
): String {
    return when (emotion) {
        SpendingEmotion.HAPPY -> language.pick("开心", "Happy")
        SpendingEmotion.NEUTRAL -> language.pick("一般", "Neutral")
        SpendingEmotion.REGRETFUL -> language.pick("后悔", "Regretful")
        SpendingEmotion.IMPULSIVE -> language.pick("冲动", "Impulsive")
    }
}

fun breathingStatusLabel(
    breathingRoom: Long,
    expectedExpense: Long,
    language: AppLanguage = AppLanguage.CHINESE,
): String {
    return when {
        breathingRoom < 0L -> language.pick("本月已经超预算", "You are over budget this month")
        expectedExpense == 0L -> language.pick("先设置本月预算", "Set your monthly budget first")
        breathingRoom <= expectedExpense / 4L -> language.pick("预算空间已经很紧", "Your budget room is getting tight")
        else -> language.pick("还有舒服的预算空间", "You still have comfortable budget room")
    }
}

fun entryStatusLabel(
    status: EntryStatus,
    language: AppLanguage = AppLanguage.CHINESE,
): String {
    return when (status) {
        EntryStatus.DRAFT -> language.pick("草稿", "Draft")
        EntryStatus.PENDING_CONFIRMATION -> language.pick("待确认", "Pending")
        EntryStatus.CONFIRMED_AUTO -> language.pick("已自动确认", "Auto confirmed")
        EntryStatus.CONFIRMED_WITH_EDIT -> language.pick("已编辑确认", "Confirmed with edits")
        EntryStatus.CANCELLED -> language.pick("已取消", "Cancelled")
    }
}
