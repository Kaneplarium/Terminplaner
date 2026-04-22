package com.terminplaner.domain.repository

import com.terminplaner.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAllCategories(): Flow<List<Category>>
    suspend fun getCategoryById(id: Long): Category?
    suspend fun insertCategory(category: Category): Long
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(category: Category)
    suspend fun getAllCategoriesForExport(): List<Category>
    suspend fun importCategories(categories: List<Category>)
}