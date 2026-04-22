package com.terminplaner.data.repository

import com.terminplaner.data.local.dao.CategoryDao
import com.terminplaner.domain.model.Category
import com.terminplaner.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getCategoryById(id: Long): Category? {
        return categoryDao.getCategoryById(id)?.toDomain()
    }

    override suspend fun insertCategory(category: Category): Long {
        return categoryDao.insertCategory(category.toEntity())
    }

    override suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category.toEntity())
    }

    override suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category.toEntity())
    }

    override suspend fun getAllCategoriesForExport(): List<Category> {
        return categoryDao.getAllCategoriesForExport().map { it.toDomain() }
    }

    override suspend fun importCategories(categories: List<Category>) {
        categoryDao.insertAll(categories.map { it.toEntity() })
    }
}
