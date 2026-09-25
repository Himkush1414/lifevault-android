package com.lifevault.app.core.repository

import com.lifevault.app.core.database.dao.AttachmentDao
import com.lifevault.app.core.database.entity.AttachmentEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Metadata only for now (Section 12 step 6) — the encrypted file store (`VaultFileStore`,
 * Step 10) owns the actual bytes on disk. This repository just tracks the rows that
 * describe them.
 */
class AttachmentRepository @Inject constructor(private val attachmentDao: AttachmentDao) {

    fun observeForDocument(documentId: String): Flow<List<AttachmentEntity>> =
        attachmentDao.observeForDocument(documentId)

    /** For [com.lifevault.app.core.domain.feature.FeatureGate.canAddFile]. */
    suspend fun countForDocument(documentId: String): Int = attachmentDao.countForDocument(documentId)

    fun observeTotalEncryptedBytes(): Flow<Long?> = attachmentDao.observeTotalEncryptedBytes()

    suspend fun add(attachment: AttachmentEntity) {
        val nextSortOrder = attachmentDao.maxSortOrder(attachment.documentId) + 1
        attachmentDao.insert(attachment.copy(sortOrder = nextSortOrder))
    }

    suspend fun update(attachment: AttachmentEntity) = attachmentDao.update(attachment)

    suspend fun delete(attachment: AttachmentEntity) = attachmentDao.delete(attachment)

    /** Section 6.6 dedupe: is this plaintext hash already attached to this document? */
    suspend fun isDuplicate(documentId: String, sha256: String): Boolean =
        sha256 in attachmentDao.hashesForDocument(documentId)

    /** Section 6.7 weekly orphan sweep. */
    suspend fun getAll(): List<AttachmentEntity> = attachmentDao.getAll()
}
