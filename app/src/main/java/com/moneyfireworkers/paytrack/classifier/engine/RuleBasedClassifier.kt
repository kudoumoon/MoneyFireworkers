package com.moneyfireworkers.paytrack.classifier.engine

import com.moneyfireworkers.paytrack.classifier.normalization.MerchantTextNormalizer
import com.moneyfireworkers.paytrack.core.model.MatchType
import com.moneyfireworkers.paytrack.domain.model.ClassificationDecision
import com.moneyfireworkers.paytrack.domain.model.ClassificationRule
import com.moneyfireworkers.paytrack.domain.model.ParsedPayment

class RuleBasedClassifier(
    private val normalizer: MerchantTextNormalizer = MerchantTextNormalizer(),
) {
    fun classify(parsedPayment: ParsedPayment, rules: List<ClassificationRule>): ClassificationDecision {
        val merchant = parsedPayment.merchantName?.let(normalizer::normalize).orEmpty()
        val sortedRules = rules.filter { it.isEnabled }.sortedByDescending { it.priority }

        val matchedRule = sortedRules.firstOrNull { rule ->
            val matchMerchant = when (rule.matchType) {
                MatchType.EXACT -> merchant == rule.merchantNormalizedKeyword
                MatchType.CONTAINS -> merchant.contains(rule.merchantNormalizedKeyword)
            }

            val matchMin = rule.amountMinInCent?.let { parsedPayment.amountInCent != null && parsedPayment.amountInCent >= it } ?: true
            val matchMax = rule.amountMaxInCent?.let { parsedPayment.amountInCent != null && parsedPayment.amountInCent <= it } ?: true
            matchMerchant && matchMin && matchMax
        }

        return if (matchedRule != null) {
            ClassificationDecision(
                matchedRuleId = matchedRule.id,
                categoryId = matchedRule.targetCategoryId,
                confidence = 90,
                decisionReason = "RULE_MATCHED",
                explanation = matchedRule.explanationTemplate,
                matchedSignals = mapOf("merchant" to (parsedPayment.merchantName ?: "")),
            )
        } else {
            ClassificationDecision(
                categoryId = null,
                confidence = 30,
                decisionReason = "FALLBACK",
                explanation = "No rule matched. Use fallback category.",
            )
        }
    }
}
