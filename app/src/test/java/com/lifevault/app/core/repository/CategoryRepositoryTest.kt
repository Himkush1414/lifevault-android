package com.lifevault.app.core.repository

import com.lifevault.app.core.repository.fake.FakeCategoryDao
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

private suspend inline fun <reified T : Throwable> assertSuspendThrows(crossinline block: suspend () -> Unit): T {
    try {
        block()
    } catch (e: Throwable) {
        if (e is T) return e
        throw e
    }
    throw AssertionError("Expected ${T::class.simpleName} but nothing was thrown")
}

class CategoryRepositoryTest {

    private lateinit var dao: FakeCategoryDao
    private lateinit var repository: CategoryRepository
    private val fixedClock = Clock.fixed(Instant.parse("2026-09-25T09:00:00Z"), ZoneOffset.UTC)

    @Before
    fun setUp() {
        dao = FakeCategoryDao()
        repository = CategoryRepository(dao, fixedClock)
    }

    @Test
    fun `adding a custom category assigns the next sort order and current time`() = runTest {
        dao.insert(builtIn("cat_other", sortOrder = 10))

        val created = repository.addCustomCategory("Pets", "pets", "brown")

        assertEquals(11, created.sortOrder)
        assertFalse(created.isSystem)
        assertEquals(Instant.parse("2026-09-25T09:00:00Z"), created.createdAt)
    }

    @Test
    fun `adding a duplicate name (case-insensitive) is rejected`() = runTest {
        repository.addCustomCategory("Pets", "pets", "brown")

        assertSuspendThrows<DuplicateCategoryNameException> {
            repository.addCustomCategory("PETS", "pets", "brown")
        }
    }

    @Test
    fun `renaming a built-in category is allowed`() = runTest {
        val builtIn = builtIn("cat_identity")
        dao.insert(builtIn)

        repository.rename(builtIn, "My Documents")

        assertEquals("My Documents", dao.current.first().name)
    }

    @Test
    fun `deleting a built-in category is rejected`() = runTest {
        val builtIn = builtIn("cat_identity")
        dao.insert(builtIn)

        assertSuspendThrows<SystemCategoryNotDeletableException> {
            repository.deleteCustomCategory(builtIn)
        }
        assertTrue(dao.current.contains(builtIn))
    }

    @Test
    fun `deleting a custom category succeeds`() = runTest {
        val custom = builtIn("cat_custom", isSystem = false)
        dao.insert(custom)

        repository.deleteCustomCategory(custom)

        assertEquals(emptyList<Any>(), dao.current)
    }

    private fun builtIn(id: String, sortOrder: Int = 0, isSystem: Boolean = true) =
        com.lifevault.app.core.database.entity.CategoryEntity(
            id = id,
            name = id,
            iconKey = "folder",
            colorKey = "grey",
            isSystem = isSystem,
            sortOrder = sortOrder,
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH,
        )
}
