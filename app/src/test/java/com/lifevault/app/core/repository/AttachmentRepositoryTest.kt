package com.lifevault.app.core.repository

import com.lifevault.app.core.database.entity.AttachmentEntity
import com.lifevault.app.core.domain.model.AttachmentKind
import com.lifevault.app.core.repository.fake.FakeAttachmentDao
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

class AttachmentRepositoryTest {

    private lateinit var dao: FakeAttachmentDao
    private lateinit var repository: AttachmentRepository

    @Before
    fun setUp() {
        dao = FakeAttachmentDao()
        repository = AttachmentRepository(dao)
    }

    private fun attachment(id: String, documentId: String, sha256: String, sortOrder: Int = 0) = AttachmentEntity(
        id = id,
        documentId = documentId,
        kind = AttachmentKind.IMAGE,
        mimeType = "image/webp",
        originalFileName = null,
        sizeBytes = 1_000,
        encryptedSizeBytes = 1_050,
        sha256 = sha256,
        widthPx = 100,
        heightPx = 100,
        pageCount = null,
        rotationDegrees = 0,
        sortOrder = sortOrder,
        createdAt = Instant.EPOCH,
    )

    @Test
    fun `adding an attachment assigns the next sort order`() = runTest {
        repository.add(attachment("a1", "doc1", "hash1"))
        repository.add(attachment("a2", "doc1", "hash2"))

        val sortOrders = dao.current.sortedBy { it.id }.map { it.sortOrder }
        assertEquals(listOf(0, 1), sortOrders)
    }

    @Test
    fun `duplicate hash within the same document is detected`() = runTest {
        repository.add(attachment("a1", "doc1", "same-hash"))

        assertTrue(repository.isDuplicate("doc1", "same-hash"))
        assertFalse(repository.isDuplicate("doc1", "different-hash"))
        assertFalse(repository.isDuplicate("doc2", "same-hash")) // different document, not a dupe
    }

    @Test
    fun `count for document only counts that document's attachments`() = runTest {
        repository.add(attachment("a1", "doc1", "h1"))
        repository.add(attachment("a2", "doc1", "h2"))
        repository.add(attachment("a3", "doc2", "h3"))

        assertEquals(2, repository.countForDocument("doc1"))
        assertEquals(1, repository.countForDocument("doc2"))
    }
}
