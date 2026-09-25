package com.lifevault.app.core.repository

import com.lifevault.app.core.database.dao.DocumentDao
import com.lifevault.app.core.database.entity.DocumentEntity
import com.lifevault.app.core.database.relation.DocumentWithCategory
import com.lifevault.app.core.database.relation.DocumentWithDetails
import kotlinx.coroutines.flow.Flow
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class DocumentRepository @Inject constructor(
    private val documentDao: DocumentDao,
    private val clock: Clock,
) {
    fun observeById(id: String): Flow<DocumentWithDetails?> = documentDao.observeById(id)

    fun observeActive(): Flow<List<DocumentWithCategory>> = documentDao.observeActive()

    fun observeActiveByCategory(categoryId: String): Flow<List<DocumentWithCategory>> =
        documentDao.observeActiveByCategory(categoryId)

    fun observeTrashed(): Flow<List<DocumentWithCategory>> = documentDao.observeTrashed()

    /** For [com.lifevault.app.core.domain.feature.FeatureGate.canAddDocument]. */
    fun observeActiveCount(): Flow<Int> = documentDao.observeActiveCount()

    fun upcoming(today: LocalDate, limit: Int = 5): Flow<List<DocumentWithCategory>> =
        documentDao.upcoming(today, limit)

    fun expired(today: LocalDate): Flow<List<DocumentWithCategory>> = documentDao.expired(today)

    fun noExpiry(): Flow<List<DocumentWithCategory>> = documentDao.noExpiry()

    /** [ftsQuery] must already be tokenised/escaped by the caller (Section 5.5). */
    fun search(ftsQuery: String): Flow<List<DocumentWithCategory>> = documentDao.search(ftsQuery)

    /**
     * Builds a new [DocumentEntity] with a fresh id and timestamps; the caller supplies
     * everything else (title, category, dates, ...) via [populate].
     */
    suspend fun create(populate: (id: String, now: Instant) -> DocumentEntity): DocumentEntity {
        val now = Instant.now(clock)
        val document = populate(UUID.randomUUID().toString(), now)
        documentDao.insert(document)
        return document
    }

    suspend fun update(document: DocumentEntity) {
        documentDao.update(document.copy(updatedAt = Instant.now(clock)))
    }

    /** Section 3.3 S07/S23: move to Trash (soft delete), not a hard delete. */
    suspend fun moveToTrash(id: String) {
        documentDao.softDelete(id, Instant.now(clock))
    }

    suspend fun restoreFromTrash(id: String) {
        documentDao.restore(id, Instant.now(clock))
    }

    /** Only ever called for a document already in Trash (Section 6.9). */
    suspend fun permanentlyDelete(document: DocumentEntity) {
        documentDao.delete(document)
    }

    /** Section 23 daily trash-purge worker: 30-day retention. */
    suspend fun trashedOlderThan(cutoff: Instant): List<DocumentEntity> = documentDao.trashedOlderThan(cutoff)

    /** Used when deleting a custom category — reassign its documents (e.g. to "Other") first. */
    suspend fun reassignCategory(oldCategoryId: String, newCategoryId: String) {
        documentDao.reassignCategory(oldCategoryId, newCategoryId, Instant.now(clock))
    }
}
