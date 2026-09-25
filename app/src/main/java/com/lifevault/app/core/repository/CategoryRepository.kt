package com.lifevault.app.core.repository

import com.lifevault.app.core.database.dao.CategoryDao
import com.lifevault.app.core.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow
import java.time.Clock
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class DuplicateCategoryNameException(name: String) : Exception("A category named \"$name\" already exists")
class SystemCategoryNotDeletableException(id: String) : Exception("Category \"$id\" is built-in and cannot be deleted")

class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao,
    private val clock: Clock,
) {
    fun observeAll(): Flow<List<CategoryEntity>> = categoryDao.observeAll()

    fun observeById(id: String): Flow<CategoryEntity?> = categoryDao.observeById(id)

    /** Custom categories only (Pro-gated at the call site via FeatureGate). */
    suspend fun addCustomCategory(name: String, iconKey: String, colorKey: String): CategoryEntity {
        requireUniqueName(name)
        val now = Instant.now(clock)
        val category = CategoryEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            iconKey = iconKey,
            colorKey = colorKey,
            isSystem = false,
            sortOrder = categoryDao.maxSortOrder() + 1,
            createdAt = now,
            updatedAt = now,
        )
        categoryDao.insert(category)
        return category
    }

    /** Renaming is allowed for both built-in and custom categories (Section 3.3 S08). */
    suspend fun rename(category: CategoryEntity, newName: String) {
        requireUniqueName(newName, excludingId = category.id)
        categoryDao.update(category.copy(name = newName, updatedAt = Instant.now(clock)))
    }

    suspend fun updateCustomCategory(category: CategoryEntity, name: String, iconKey: String, colorKey: String) {
        check(!category.isSystem) { "Built-in categories can only be renamed, not fully edited" }
        requireUniqueName(name, excludingId = category.id)
        categoryDao.update(
            category.copy(name = name, iconKey = iconKey, colorKey = colorKey, updatedAt = Instant.now(clock)),
        )
    }

    /**
     * Custom categories only (Section 3.3 S08 "delete not allowed" for built-ins).
     * Callers must reassign the category's documents (e.g. to "Other") before calling
     * this — `documents.categoryId`'s `ForeignKey.RESTRICT` will otherwise reject the
     * delete, which is the safety net, not the primary mechanism.
     */
    suspend fun deleteCustomCategory(category: CategoryEntity) {
        if (category.isSystem) throw SystemCategoryNotDeletableException(category.id)
        categoryDao.delete(category)
    }

    private suspend fun requireUniqueName(name: String, excludingId: String = "") {
        if (categoryDao.countByNameIgnoreCase(name, excludingId) > 0) {
            throw DuplicateCategoryNameException(name)
        }
    }
}
