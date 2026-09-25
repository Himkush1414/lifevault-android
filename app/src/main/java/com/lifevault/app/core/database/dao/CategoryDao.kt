package com.lifevault.app.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lifevault.app.core.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Update
    suspend fun update(category: CategoryEntity)

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("SELECT * FROM categories ORDER BY sortOrder ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    fun observeById(id: String): Flow<CategoryEntity?>

    @Query("SELECT COUNT(*) FROM categories WHERE LOWER(name) = LOWER(:name) AND id != :excludingId")
    suspend fun countByNameIgnoreCase(name: String, excludingId: String = ""): Int

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM categories")
    suspend fun maxSortOrder(): Int
}
