package com.moneyfireworkers.paytrack.domain.model

enum class SpendingEmotion(
    val label: String,
    val amountColorToken: EmotionAmountColorToken,
) {
    HAPPY(
        label = "开心",
        amountColorToken = EmotionAmountColorToken.HAPPY_AMOUNT,
    ),
    NEUTRAL(
        label = "一般",
        amountColorToken = EmotionAmountColorToken.NEUTRAL_AMOUNT,
    ),
    REGRETFUL(
        label = "后悔",
        amountColorToken = EmotionAmountColorToken.REGRETFUL_AMOUNT,
    ),
    IMPULSIVE(
        label = "冲动",
        amountColorToken = EmotionAmountColorToken.IMPULSIVE_AMOUNT,
    ),
}

enum class EmotionAmountColorToken(val tokenName: String) {
    HAPPY_AMOUNT("emotion_happy_amount"),
    NEUTRAL_AMOUNT("emotion_neutral_amount"),
    REGRETFUL_AMOUNT("emotion_regretful_amount"),
    IMPULSIVE_AMOUNT("emotion_impulsive_amount"),
}
