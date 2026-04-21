package com.moneyfireworkers.paytrack.domain.model

import java.util.Locale

enum class AppLanguage(
    val tag: String,
    val locale: Locale,
) {
    CHINESE(
        tag = "zh",
        locale = Locale.SIMPLIFIED_CHINESE,
    ),
    ENGLISH(
        tag = "en",
        locale = Locale.US,
    );

    val storageValue: String
        get() = tag

    companion object {
        fun fromTag(tag: String?): AppLanguage {
            return entries.firstOrNull { it.tag.equals(tag, ignoreCase = true) } ?: CHINESE
        }

        fun fromStorageValue(value: String?): AppLanguage = fromTag(value)
    }
}
