package com.lifevault.app.core.repository

import com.lifevault.app.core.database.entity.DocumentEntity
import com.lifevault.app.core.domain.reminder.Recurrence
import com.lifevault.app.core.repository.fake.FakeDocumentDao
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class DocumentRepositoryTest {

    private lateinit var dao: FakeDocumentDao
    private lateinit var repository: DocumentRepository
    private val fixedClock = Clock.fixed(Instant.parse("2026-09-25T09:00:00Z"), ZoneOffset.UTC)

    @Before
    fun setUp() {
        dao = FakeDocumentDao()
        repository = DocumentRepository(dao, fixedClock)
    }

    private fun bareDocument(id: String, now: Instant) = DocumentEntity(
        id = id,
        title = "Passport",
        categoryId = "cat_identity",
        documentNumber = null,
        issuer = null,
        issueDate = null,
        expiryDate = null,
        notes = null,
        recurrence = Recurrence.NONE,
        recurrenceIntervalYears = null,
        renewalCostMinor = null,
        renewalCurrency = null,
        overdueNudges = false,
        lastOverdueNudgeAt = null,
        isFavorite = false,
        ocrText = null,
        createdAt = now,
        updatedAt = now,
        deletedAt = null,
    )

    @Test
    fun `create assigns a fresh id and the clock's current time`() = runTest {
        val created = repository.create { id, now -> bareDocument(id, now) }

        assertEquals(Instant.parse("2026-09-25T09:00:00Z"), created.createdAt)
        assertNotNull(dao.current.firstOrNull { it.id == created.id })
    }

    @Test
    fun `two created documents get different ids`() = runTest {
        val first = repository.create { id, now -> bareDocument(id, now) }
        val second = repository.create { id, now -> bareDocument(id, now) }

        assertNotEquals(first.id, second.id)
    }

    @Test
    fun `update refreshes updatedAt to the current clock time`() = runTest {
        val created = repository.create { id, now -> bareDocument(id, now) }

        repository.update(created.copy(title = "Passport (renewed)"))

        val updated = dao.current.first()
        assertEquals("Passport (renewed)", updated.title)
        assertEquals(Instant.parse("2026-09-25T09:00:00Z"), updated.updatedAt)
    }

    @Test
    fun `moving to trash sets deletedAt`() = runTest {
        val created = repository.create { id, now -> bareDocument(id, now) }

        repository.moveToTrash(created.id)

        assertEquals(Instant.parse("2026-09-25T09:00:00Z"), dao.current.first().deletedAt)
    }

    @Test
    fun `restoring from trash clears deletedAt`() = runTest {
        val created = repository.create { id, now -> bareDocument(id, now) }
        repository.moveToTrash(created.id)

        repository.restoreFromTrash(created.id)

        assertNull(dao.current.first().deletedAt)
    }

    @Test
    fun `permanently deleting removes the document`() = runTest {
        val created = repository.create { id, now -> bareDocument(id, now) }
        repository.moveToTrash(created.id)

        repository.permanentlyDelete(dao.current.first())

        assertEquals(emptyList<DocumentEntity>(), dao.current)
    }
}
