package com.moneyfireworkers.paytrack.data.local.mapper

import com.moneyfireworkers.paytrack.core.model.MatchType
import com.moneyfireworkers.paytrack.data.local.entity.ClassificationRuleEntity
import com.moneyfireworkers.paytrack.domain.model.ClassificationRule

object ClassificationRuleMapper {
    fun fromEntity(entity: ClassificationRuleEntity): ClassificationRule = ClassificationRule(
        id = entity.id,
        name = entity.name,
        merchantKeyword = entity.merchantKeyword,
        merchantNormalizedKeyword = entity.merchantNormalizedKeyword,
        matchType = MatchType.valueOf(entity.matchType),
        amountMinInCent = entity.amountMinInCent,
        amountMaxInCent = entity.amountMaxInCent,
        targetCategoryId = entity.targetCategoryId,
        priority = entity.priority,
        explanationTemplate = entity.explanationTemplate,
        isEnabled = entity.isEnabled,
        version = entity.version,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt,
    )
}
