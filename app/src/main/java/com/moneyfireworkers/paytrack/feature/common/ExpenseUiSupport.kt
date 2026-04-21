package com.moneyfireworkers.paytrack.feature.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalMall
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.moneyfireworkers.paytrack.core.ui.theme.EmotionHappy
import com.moneyfireworkers.paytrack.core.ui.theme.EmotionImpulsive
import com.moneyfireworkers.paytrack.core.ui.theme.EmotionNeutral
import com.moneyfireworkers.paytrack.core.ui.theme.EmotionRegretful
import com.moneyfireworkers.paytrack.domain.model.SpendingEmotion

fun categoryVector(iconToken: String?): ImageVector {
    return when (iconToken) {
        "food" -> Icons.Outlined.Fastfood
        "coffee" -> Icons.Outlined.Coffee
        "transport" -> Icons.Outlined.DirectionsBus
        "daily" -> Icons.Outlined.LocalMall
        else -> Icons.Outlined.Inventory2
    }
}

fun categoryEmoji(iconToken: String?): String {
    return when (iconToken) {
        "food" -> "\uD83C\uDF54"
        "coffee" -> "\u2615"
        "transport" -> "\uD83D\uDE8C"
        "daily" -> "\uD83D\uDED2"
        else -> "\uD83D\uDCE6"
    }
}

fun emotionEmoji(emotion: SpendingEmotion): String {
    return when (emotion) {
        SpendingEmotion.HAPPY -> "\uD83D\uDE0A"
        SpendingEmotion.NEUTRAL -> "\uD83D\uDE10"
        SpendingEmotion.REGRETFUL -> "\uD83D\uDE14"
        SpendingEmotion.IMPULSIVE -> "\uD83D\uDE2C"
    }
}

fun emotionColor(emotion: SpendingEmotion): Color {
    return when (emotion) {
        SpendingEmotion.HAPPY -> EmotionHappy
        SpendingEmotion.NEUTRAL -> EmotionNeutral
        SpendingEmotion.REGRETFUL -> EmotionRegretful
        SpendingEmotion.IMPULSIVE -> EmotionImpulsive
    }
}
