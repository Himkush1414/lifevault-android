package com.lifevault.app.core.repository.fake

import com.lifevault.app.core.database.dao.CategoryDao
import com.lifevault.app.core.database.entity.CategoryEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeCategoryDao : CategoryDao {
    private val state = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val current: List<CategoryEntity> get() = state.value

    override suspend fun insert(category: CategoryEntity) {
        check(state.value.none { it.id == category.id }) { "duplicate id ${category.id}" }
        state.value = state.value + category
    }

    override suspend fun insertAll(categories: List<CategoryEntity>) {
        categories.forEach { insert(it) }
    }

    override suspend fun update(category: CategoryEntity) {
        state.value = state.value.map { if (it.id == category.id) category else it }
    }

    override suspend fun delete(category: CategoryEntity) {
        state.value = state.value.filterNot { it.id == category.id }
    }

    override fun observeAll() = state.map { it.sortedBy(CategoryEntity::sortOrder) }

    override fun observeById(id: String) = state.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun countByNameIgnoreCase(name: String, excludingId: String): Int =
        state.value.count { it.name.equals(name, ignoreCase = true) && it.id != excludingId }

    override suspend fun maxSortOrder(): Int = state.value.maxOfOrNull { it.sortOrder } ?: -1
}
