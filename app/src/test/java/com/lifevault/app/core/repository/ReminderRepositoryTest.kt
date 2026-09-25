package com.lifevault.app.core.repository

import com.lifevault.app.core.repository.fake.FakeReminderDao
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class ReminderRepositoryTest {

    private lateinit var dao: FakeReminderDao
    private lateinit var repository: ReminderRepository

    // "Today" is 2026-09-25.
    private val fixedClock = Clock.fixed(Instant.parse("2026-09-25T09:00:00Z"), ZoneOffset.UTC)

    @Before
    fun setUp() {
        dao = FakeReminderDao()
        repository = ReminderRepository(dao, fixedClock)
    }

    @Test
    fun `a reminder whose fire date is still in the future is armed, not suppressed`() = runTest {
        // expiry in 60 days, offset 30 -> fire date is 30 days from now: not suppressed.
        val expiry = LocalDate.of(2026, 11, 24)
        val reminder = repository.addReminder("doc1", offsetDays = 30, timeOfDayMinutes = null, expiryDate = expiry)

        assertNull(reminder.lastFiredForExpiry)
        assertNull(reminder.lastFiredAt)
    }

    @Test
    fun `a reminder whose fire date has already passed is suppressed at save time`() = runTest {
        // expiry in 5 days, offset 30 -> fire date was 25 days ago: suppressed.
        val expiry = LocalDate.of(2026, 9, 30)
        val reminder = repository.addReminder("doc1", offsetDays = 30, timeOfDayMinutes = null, expiryDate = expiry)

        assertEquals(expiry, reminder.lastFiredForExpiry)
        assertEquals(Instant.parse("2026-09-25T09:00:00Z"), reminder.lastFiredAt)
    }

    @Test
    fun `a reminder whose fire date is exactly today is suppressed`() = runTest {
        // offset 0 on a document expiring today -> fire date is today.
        val expiry = LocalDate.of(2026, 9, 25)
        val reminder = repository.addReminder("doc1", offsetDays = 0, timeOfDayMinutes = null, expiryDate = expiry)

        assertEquals(expiry, reminder.lastFiredForExpiry)
    }

    @Test
    fun `a reminder on a document with no expiry is never suppressed`() = runTest {
        val reminder = repository.addReminder("doc1", offsetDays = 30, timeOfDayMinutes = null, expiryDate = null)

        assertNull(reminder.lastFiredForExpiry)
    }

    @Test
    fun `changing the expiry date re-arms a reminder that was previously fired`() = runTest {
        val oldExpiry = LocalDate.of(2026, 9, 1)
        val reminder = repository.addReminder("doc1", offsetDays = 0, timeOfDayMinutes = null, expiryDate = oldExpiry)
        dao.markFired(reminder.id, oldExpiry, Instant.parse("2026-09-01T09:00:00Z"))

        // Renewed far into the future — re-arms.
        val newExpiry = LocalDate.of(2027, 9, 1)
        repository.rearmForNewExpiry("doc1", newExpiry)

        assertNull(dao.current.first().lastFiredForExpiry)
    }

    @Test
    fun `rearming with a new but still-past fire date suppresses again for the new expiry`() = runTest {
        val reminder = repository.addReminder(
            "doc1",
            offsetDays = 30,
            timeOfDayMinutes = null,
            expiryDate = LocalDate.of(2026, 11, 24),
        )
        dao.markFired(reminder.id, LocalDate.of(2026, 11, 24), Instant.EPOCH)

        // New expiry is only 5 days out; a 30-day-before reminder's fire date is already past.
        val newExpiry = LocalDate.of(2026, 9, 30)
        repository.rearmForNewExpiry("doc1", newExpiry)

        assertEquals(newExpiry, dao.current.first().lastFiredForExpiry)
    }
}
