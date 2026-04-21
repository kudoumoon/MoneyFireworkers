package com.moneyfireworkers.paytrack.domain.model

data class Category(
    val id: Long = 0L,
    val name: String,
    val iconToken: String? = null,
    val colorToken: String? = null,
    val sortOrder: Int = 0,
    val isEnabled: Boolean = true,
    val isSystem: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long,
)
