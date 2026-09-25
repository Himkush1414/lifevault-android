package com.lifevault.app.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.lifevault.app.core.database.entity.DocumentEntity
import com.lifevault.app.core.database.relation.DocumentWithCategory
import com.lifevault.app.core.database.relation.DocumentWithDetails
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate

@Dao
interface DocumentDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(document: DocumentEntity)

    @Update
    suspend fun update(document: DocumentEntity)

    /** Hard delete — only ever called for a document already in Trash (Section 6.9). */
    @Delete
    suspend fun delete(document: DocumentEntity)

    @Transaction
    @Query("SELECT * FROM documents WHERE id = :id")
    fun observeById(id: String): Flow<DocumentWithDetails?>

    @Transaction
    @Query("SELECT * FROM documents WHERE deletedAt IS NULL ORDER BY updatedAt DESC")
    fun observeActive(): Flow<List<DocumentWithCategory>>

    @Transaction
    @Query("SELECT * FROM documents WHERE deletedAt IS NULL AND categoryId = :categoryId ORDER BY updatedAt DESC")
    fun observeActiveByCategory(categoryId: String): Flow<List<DocumentWithCategory>>

    @Transaction
    @Query("SELECT * FROM documents WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC")
    fun observeTrashed(): Flow<List<DocumentWithCategory>>

    @Query("SELECT COUNT(*) FROM documents WHERE deletedAt IS NULL")
    fun observeActiveCount(): Flow<Int>

    // Section 5.5, example 1 — verbatim.
    @Transaction
    @Query(
        """
        SELECT * FROM documents
        WHERE deletedAt IS NULL AND expiryDate IS NOT NULL AND expiryDate >= :today
        ORDER BY expiryDate ASC LIMIT :limit
        """,
    )
    fun upcoming(today: LocalDate, limit: Int): Flow<List<DocumentWithCategory>>

    @Transaction
    @Query(
        """
        SELECT * FROM documents
        WHERE deletedAt IS NULL AND expiryDate IS NOT NULL AND expiryDate < :today
        ORDER BY expiryDate DESC
        """,
    )
    fun expired(today: LocalDate): Flow<List<DocumentWithCategory>>

    @Transaction
    @Query("SELECT * FROM documents WHERE deletedAt IS NULL AND expiryDate IS NULL ORDER BY updatedAt DESC")
    fun noExpiry(): Flow<List<DocumentWithCategory>>

    // Section 5.5, example 2 — verbatim (ftsQuery is pre-tokenised by the caller, Section 5.5).
    @Transaction
    @Query(
        """
        SELECT d.* FROM documents d JOIN documents_fts f ON d.rowid = f.rowid
        WHERE documents_fts MATCH :ftsQuery AND d.deletedAt IS NULL
        ORDER BY d.updatedAt DESC LIMIT 100
        """,
    )
    fun search(ftsQuery: String): Flow<List<DocumentWithCategory>>

    @Query("UPDATE documents SET deletedAt = :deletedAt, updatedAt = :deletedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: Instant)

    @Query("UPDATE documents SET deletedAt = NULL, updatedAt = :restoredAt WHERE id = :id")
    suspend fun restore(id: String, restoredAt: Instant)

    @Query("SELECT * FROM documents WHERE deletedAt IS NOT NULL AND deletedAt < :cutoff")
    suspend fun trashedOlderThan(cutoff: Instant): List<DocumentEntity>

    @Query("UPDATE documents SET categoryId = :newCategoryId, updatedAt = :updatedAt WHERE categoryId = :oldCategoryId")
    suspend fun reassignCategory(oldCategoryId: String, newCategoryId: String, updatedAt: Instant)
}
