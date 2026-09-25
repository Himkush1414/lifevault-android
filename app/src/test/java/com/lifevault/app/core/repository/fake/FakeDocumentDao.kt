package com.lifevault.app.core.repository.fake

import com.lifevault.app.core.database.dao.DocumentDao
import com.lifevault.app.core.database.entity.DocumentEntity
import java.time.Instant
import java.time.LocalDate

/**
 * Only implements the plain-[DocumentEntity] CRUD/trash methods
 * [DocumentRepository][com.lifevault.app.core.repository.DocumentRepository]'s
 * mutation methods use. The `@Relation`-returning query methods aren't exercised by
 * repository-layer tests — those are covered by the Step 5 DAO instrumented tests
 * against a real (SQLCipher) database instead.
 */
class FakeDocumentDao : DocumentDao {
    private val documents = mutableMapOf<String, DocumentEntity>()
    val current: List<DocumentEntity> get() = documents.values.toList()

    override suspend fun insert(document: DocumentEntity) {
        check(document.id !in documents) { "duplicate id ${document.id}" }
        documents[document.id] = document
    }

    override suspend fun update(document: DocumentEntity) {
        documents[document.id] = document
    }

    override suspend fun delete(document: DocumentEntity) {
        documents.remove(document.id)
    }

    override suspend fun softDelete(id: String, deletedAt: Instant) {
        documents[id] = requireNotNull(documents[id]).copy(deletedAt = deletedAt, updatedAt = deletedAt)
    }

    override suspend fun restore(id: String, restoredAt: Instant) {
        documents[id] = requireNotNull(documents[id]).copy(deletedAt = null, updatedAt = restoredAt)
    }

    override suspend fun trashedOlderThan(cutoff: Instant): List<DocumentEntity> =
        documents.values.filter { doc -> doc.deletedAt?.let { it < cutoff } ?: false }

    override suspend fun reassignCategory(oldCategoryId: String, newCategoryId: String, updatedAt: Instant) {
        documents.replaceAll { _, doc ->
            if (doc.categoryId == oldCategoryId) doc.copy(categoryId = newCategoryId, updatedAt = updatedAt) else doc
        }
    }

    override fun observeById(id: String) = error("not exercised by repository-layer tests")
    override fun observeActive() = error("not exercised by repository-layer tests")
    override fun observeActiveByCategory(categoryId: String) = error("not exercised by repository-layer tests")
    override fun observeTrashed() = error("not exercised by repository-layer tests")
    override fun observeActiveCount() = error("not exercised by repository-layer tests")
    override fun upcoming(today: LocalDate, limit: Int) = error("not exercised by repository-layer tests")
    override fun expired(today: LocalDate) = error("not exercised by repository-layer tests")
    override fun noExpiry() = error("not exercised by repository-layer tests")
    override fun search(ftsQuery: String) = error("not exercised by repository-layer tests")
}
