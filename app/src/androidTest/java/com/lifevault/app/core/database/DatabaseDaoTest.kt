package com.lifevault.app.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lifevault.app.core.database.entity.AttachmentEntity
import com.lifevault.app.core.database.entity.CategoryEntity
import com.lifevault.app.core.database.entity.DocumentEntity
import com.lifevault.app.core.database.entity.ReminderEntity
import com.lifevault.app.core.domain.model.AttachmentKind
import com.lifevault.app.core.domain.reminder.Recurrence
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Requires SQLCipher's native library — not executed in this build environment (no
 * device/emulator; see BUILD_LOG.md Step 5). Written to run via
 * `./gradlew connectedDebugAndroidTest`. Covers Section 12 step 5's accept check: CRUD,
 * cascades, the `upcoming` query, and FTS search.
 */
@RunWith(AndroidJUnit4::class)
class DatabaseDaoTest {

    private lateinit var db: LifeVaultDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val passphrase = "test-passphrase".toByteArray()
        db = Room.inMemoryDatabaseBuilder(context, LifeVaultDatabase::class.java)
            .openHelperFactory(SupportOpenHelperFactory(passphrase, null, true))
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun category(id: String = "cat_test") = CategoryEntity(
        id = id,
        name = "Test Category $id",
        iconKey = "folder",
        colorKey = "grey",
        isSystem = false,
        sortOrder = 0,
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH,
    )

    private fun document(id: String = UUID.randomUUID().toString(), categoryId: String, expiry: LocalDate? = null) =
        DocumentEntity(
            id = id,
            title = "Test Document",
            categoryId = categoryId,
            documentNumber = null,
            issuer = null,
            issueDate = null,
            expiryDate = expiry,
            notes = null,
            recurrence = Recurrence.NONE,
            recurrenceIntervalYears = null,
            renewalCostMinor = null,
            renewalCurrency = null,
            overdueNudges = false,
            lastOverdueNudgeAt = null,
            isFavorite = false,
            ocrText = null,
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH,
            deletedAt = null,
        )

    @Test
    fun categoryInsertAndRead() = runBlocking {
        db.categoryDao().insert(category())
        val categories = db.categoryDao().observeAll().first()
        assertEquals(1, categories.size)
        assertEquals("cat_test", categories.first().id)
    }

    @Test
    fun documentInsertAndReadWithCategory() = runBlocking {
        db.categoryDao().insert(category())
        val doc = document(categoryId = "cat_test")
        db.documentDao().insert(doc)

        val loaded = db.documentDao().observeById(doc.id).first()
        assertNotNull(loaded)
        assertEquals("Test Document", loaded?.document?.title)
        assertEquals("cat_test", loaded?.category?.id)
    }

    @Test
    fun deletingDocumentCascadesToAttachmentsAndReminders() = runBlocking {
        db.categoryDao().insert(category())
        val doc = document(categoryId = "cat_test")
        db.documentDao().insert(doc)

        val attachment = AttachmentEntity(
            id = UUID.randomUUID().toString(),
            documentId = doc.id,
            kind = AttachmentKind.IMAGE,
            mimeType = "image/webp",
            originalFileName = null,
            sizeBytes = 100,
            encryptedSizeBytes = 128,
            sha256 = "deadbeef",
            widthPx = 100,
            heightPx = 100,
            pageCount = null,
            rotationDegrees = 0,
            sortOrder = 0,
            createdAt = Instant.EPOCH,
        )
        db.attachmentDao().insert(attachment)

        val reminder = ReminderEntity(
            id = UUID.randomUUID().toString(),
            documentId = doc.id,
            offsetDays = 30,
            timeOfDayMinutes = null,
            enabled = true,
            lastFiredForExpiry = null,
            lastFiredAt = null,
            createdAt = Instant.EPOCH,
        )
        db.reminderDao().insert(reminder)

        db.documentDao().delete(doc)

        assertTrue(db.attachmentDao().observeForDocument(doc.id).first().isEmpty())
        assertTrue(db.reminderDao().observeForDocument(doc.id).first().isEmpty())
    }

    @Test
    fun upcomingQueryOnlyReturnsFutureNonDeletedDocumentsSortedByExpiry() = runBlocking {
        db.categoryDao().insert(category())
        val today = LocalDate.of(2026, 9, 25)

        val soon = document(categoryId = "cat_test", expiry = today.plusDays(5))
        val later = document(categoryId = "cat_test", expiry = today.plusDays(20))
        val past = document(categoryId = "cat_test", expiry = today.minusDays(1))
        val noExpiry = document(categoryId = "cat_test", expiry = null)
        listOf(soon, later, past, noExpiry).forEach { db.documentDao().insert(it) }

        val deleted = document(categoryId = "cat_test", expiry = today.plusDays(1)).copy(deletedAt = Instant.EPOCH)
        db.documentDao().insert(deleted)

        val upcoming = db.documentDao().upcoming(today, limit = 10).first()

        assertEquals(listOf(soon.id, later.id), upcoming.map { it.document.id })
    }

    @Test
    fun softDeleteThenRestoreRoundTrips() = runBlocking {
        db.categoryDao().insert(category())
        val doc = document(categoryId = "cat_test")
        db.documentDao().insert(doc)

        db.documentDao().softDelete(doc.id, Instant.EPOCH.plusSeconds(1))
        assertEquals(1, db.documentDao().observeTrashed().first().size)
        assertTrue(db.documentDao().observeActive().first().isEmpty())

        db.documentDao().restore(doc.id, Instant.EPOCH.plusSeconds(2))
        assertEquals(1, db.documentDao().observeActive().first().size)
        assertNull(db.documentDao().observeById(doc.id).first()?.document?.deletedAt)
    }

    @Test
    fun ftsSearchMatchesTitlePrefix() = runBlocking {
        db.categoryDao().insert(category())
        db.documentDao().insert(document(categoryId = "cat_test").copy(title = "Passport Renewal"))
        db.documentDao().insert(document(categoryId = "cat_test").copy(title = "Car Insurance"))

        val results = db.documentDao().search("passp*").first()

        assertEquals(1, results.size)
        assertEquals("Passport Renewal", results.first().document.title)
    }
}
