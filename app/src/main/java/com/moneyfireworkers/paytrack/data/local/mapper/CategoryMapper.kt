package com.moneyfireworkers.paytrack.data.local.mapper

import com.moneyfireworkers.paytrack.data.local.entity.CategoryEntity
import com.moneyfireworkers.paytrack.domain.model.Category

object CategoryMapper {
    fun toEntity(model: Category): CategoryEntity = CategoryEntity(
        id = model.id,
        name = model.name,
        iconToken = model.iconToken,
        colorToken = model.colorToken,
        sortOrder = model.sortOrder,
        isEnabled = model.isEnabled,
        isSystem = model.isSystem,
        createdAt = model.createdAt,
        updatedAt = model.updatedAt,
    )

    fun fromEntity(entity: CategoryEntity): Category = Category(
        id = entity.id,
        name = entity.name,
        iconToken = entity.iconToken,
        colorToken = entity.colorToken,
        sortOrder = entity.sortOrder,
        isEnabled = entity.isEnabled,
        isSystem = entity.isSystem,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt,
    )
}
