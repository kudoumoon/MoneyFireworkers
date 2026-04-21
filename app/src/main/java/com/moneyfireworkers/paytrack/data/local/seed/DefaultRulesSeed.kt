package com.moneyfireworkers.paytrack.data.local.seed

import com.moneyfireworkers.paytrack.core.model.MatchType
import com.moneyfireworkers.paytrack.data.local.entity.ClassificationRuleEntity

object DefaultRulesSeed {
    fun build(now: Long): List<ClassificationRuleEntity> {
        return listOf(
            ClassificationRuleEntity(
                id = 1L,
                name = "luckin_coffee",
                merchantKeyword = "瑞幸",
                merchantNormalizedKeyword = "瑞幸",
                matchType = MatchType.CONTAINS.name,
                targetCategoryId = 2L,
                priority = 100,
                explanationTemplate = "商户名包含“瑞幸”，建议分类为“咖啡茶饮”。",
                isEnabled = true,
                version = 1,
                createdAt = now,
                updatedAt = now,
            ),
            ClassificationRuleEntity(
                id = 2L,
                name = "starbucks_coffee",
                merchantKeyword = "星巴克",
                merchantNormalizedKeyword = "星巴克",
                matchType = MatchType.CONTAINS.name,
                targetCategoryId = 2L,
                priority = 95,
                explanationTemplate = "商户名包含“星巴克”，建议分类为“咖啡茶饮”。",
                isEnabled = true,
                version = 1,
                createdAt = now,
                updatedAt = now,
            ),
            ClassificationRuleEntity(
                id = 3L,
                name = "metro_transport",
                merchantKeyword = "地铁",
                merchantNormalizedKeyword = "地铁",
                matchType = MatchType.CONTAINS.name,
                targetCategoryId = 3L,
                priority = 90,
                explanationTemplate = "商户名包含“地铁”，建议分类为“交通”。",
                isEnabled = true,
                version = 1,
                createdAt = now,
                updatedAt = now,
            ),
            ClassificationRuleEntity(
                id = 4L,
                name = "meituan_food",
                merchantKeyword = "美团",
                merchantNormalizedKeyword = "美团",
                matchType = MatchType.CONTAINS.name,
                targetCategoryId = 1L,
                priority = 80,
                explanationTemplate = "商户名包含“美团”，建议分类为“餐饮”。",
                isEnabled = true,
                version = 1,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }
}
