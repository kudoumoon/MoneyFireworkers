package com.moneyfireworkers.paytrack.data.repository

import com.moneyfireworkers.paytrack.data.local.dao.CategoryDao
import com.moneyfireworkers.paytrack.data.local.mapper.CategoryMapper
import com.moneyfireworkers.paytrack.domain.model.Category
import com.moneyfireworkers.paytrack.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl(
    private val categoryDao: CategoryDao,
) : CategoryRepository {
    override suspend fun getAllEnabled(): List<Category> {
        return categoryDao.getAllEnabled().map(CategoryMapper::fromEntity)
    }

    override fun observeAllEnabled(): Flow<List<Category>> {
        return categoryDao.observeAllEnabled()
            .map { categories -> categories.map(CategoryMapper::fromEntity) }
    }

    override fun observeAll(): Flow<List<Category>> {
        return categoryDao.observeAll()
            .map { categories -> categories.map(CategoryMapper::fromEntity) }
    }

    override suspend fun getById(id: Long): Category? {
        return categoryDao.getById(id)?.let(CategoryMapper::fromEntity)
    }

    override suspend fun create(category: Category): Long {
        return categoryDao.insert(CategoryMapper.toEntity(category))
    }

    override suspend fun update(category: Category) {
        categoryDao.update(CategoryMapper.toEntity(category))
    }
}
