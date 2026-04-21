package com.moneyfireworkers.paytrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val iconToken: String? = null,
    val colorToken: String? = null,
    val sortOrder: Int,
    val isEnabled: Boolean,
    val isSystem: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)
