package com.moneyfireworkers.paytrack.classifier.normalization

class MerchantTextNormalizer {
    fun normalize(input: String): String {
        return input.trim()
            .lowercase()
            .replace(" ", "")
    }
}
