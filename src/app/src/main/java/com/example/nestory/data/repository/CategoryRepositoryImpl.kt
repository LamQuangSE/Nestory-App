package com.example.nestory.data.repository

import com.example.nestory.data.dao.CategoryDao
import com.example.nestory.data.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

class CategoryRepositoryImpl(
    private val categoryDao: CategoryDao
) : CategoryRepository {
    
    override fun getAllCategories(): Flow<List<CategoryEntity>> {
        return categoryDao.getAllCategories()
    }

    override suspend fun insertCategory(category: CategoryEntity) {
        categoryDao.insertCategory(category)
    }

    override suspend fun updateCategory(category: CategoryEntity) {
        categoryDao.updateCategory(category)
    }

    override suspend fun deleteCategory(categoryId: String) {
        categoryDao.deleteCategoryById(categoryId)
    }
}