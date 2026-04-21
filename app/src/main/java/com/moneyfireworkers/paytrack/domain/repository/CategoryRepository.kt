package com.moneyfireworkers.paytrack.domain.repository

import com.moneyfireworkers.paytrack.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    suspend fun getAllEnabled(): List<Category>
    fun observeAllEnabled(): Flow<List<Category>>
    fun observeAll(): Flow<List<Category>>
    suspend fun getById(id: Long): Category?
    suspend fun create(category: Category): Long
    suspend fun update(category: Category)
}
