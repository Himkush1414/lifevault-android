package com.lifevault.app.core.repository.fake

import com.lifevault.app.core.database.dao.AttachmentDao
import com.lifevault.app.core.database.entity.AttachmentEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeAttachmentDao : AttachmentDao {
    private val state = MutableStateFlow<List<AttachmentEntity>>(emptyList())
    val current: List<AttachmentEntity> get() = state.value

    override suspend fun insert(attachment: AttachmentEntity) {
        state.value = state.value + attachment
    }

    override suspend fun update(attachment: AttachmentEntity) {
        state.value = state.value.map { if (it.id == attachment.id) attachment else it }
    }

    override suspend fun delete(attachment: AttachmentEntity) {
        state.value = state.value.filterNot { it.id == attachment.id }
    }

    override fun observeForDocument(documentId: String) =
        state.map { list -> list.filter { it.documentId == documentId }.sortedBy(AttachmentEntity::sortOrder) }

    override suspend fun countForDocument(documentId: String): Int =
        state.value.count { it.documentId == documentId }

    override suspend fun maxSortOrder(documentId: String): Int =
        state.value.filter { it.documentId == documentId }.maxOfOrNull { it.sortOrder } ?: -1

    override fun observeTotalEncryptedBytes() = state.map { list -> list.sumOf { it.encryptedSizeBytes } }

    override suspend fun hashesForDocument(documentId: String): List<String> =
        state.value.filter { it.documentId == documentId }.map { it.sha256 }

    override suspend fun getAll(): List<AttachmentEntity> = state.value
}
