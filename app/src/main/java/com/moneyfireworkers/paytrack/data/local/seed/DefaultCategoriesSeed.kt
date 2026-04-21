package com.moneyfireworkers.paytrack.data.local.seed

import com.moneyfireworkers.paytrack.data.local.entity.CategoryEntity

object DefaultCategoriesSeed {
    fun build(now: Long): List<CategoryEntity> {
        return listOf(
            CategoryEntity(
                id = 1L,
                name = "餐饮",
                iconToken = "food",
                colorToken = "category_food",
                sortOrder = 1,
                isEnabled = true,
                isSystem = true,
                createdAt = now,
                updatedAt = now,
            ),
            CategoryEntity(
                id = 2L,
                name = "咖啡茶饮",
                iconToken = "coffee",
                colorToken = "category_coffee",
                sortOrder = 2,
                isEnabled = true,
                isSystem = true,
                createdAt = now,
                updatedAt = now,
            ),
            CategoryEntity(
                id = 3L,
                name = "交通",
                iconToken = "transport",
                colorToken = "category_transport",
                sortOrder = 3,
                isEnabled = true,
                isSystem = true,
                createdAt = now,
                updatedAt = now,
            ),
            CategoryEntity(
                id = 4L,
                name = "日用",
                iconToken = "daily",
                colorToken = "category_daily",
                sortOrder = 4,
                isEnabled = true,
                isSystem = true,
                createdAt = now,
                updatedAt = now,
            ),
            CategoryEntity(
                id = 5L,
                name = "其他",
                iconToken = "other",
                colorToken = "category_other",
                sortOrder = 5,
                isEnabled = true,
                isSystem = true,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }
}
