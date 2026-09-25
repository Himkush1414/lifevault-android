package com.lifevault.app.core.domain.reminder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.LocalDate

class RecurrenceTest {

    private val expiry = LocalDate.of(2026, 9, 25)

    @Test
    fun `NONE does not change the date`() {
        assertEquals(expiry, rollForwardExpiry(expiry, Recurrence.NONE))
    }

    @Test
    fun `MONTHLY adds one month`() {
        assertEquals(LocalDate.of(2026, 10, 25), rollForwardExpiry(expiry, Recurrence.MONTHLY))
    }

    @Test
    fun `QUARTERLY adds three months`() {
        assertEquals(LocalDate.of(2026, 12, 25), rollForwardExpiry(expiry, Recurrence.QUARTERLY))
    }

    @Test
    fun `YEARLY adds one year`() {
        assertEquals(LocalDate.of(2027, 9, 25), rollForwardExpiry(expiry, Recurrence.YEARLY))
    }

    @Test
    fun `EVERY_N_YEARS adds the given interval`() {
        assertEquals(LocalDate.of(2036, 9, 25), rollForwardExpiry(expiry, Recurrence.EVERY_N_YEARS, intervalYears = 10))
    }

    @Test
    fun `EVERY_N_YEARS rejects a null interval`() {
        assertThrows(IllegalArgumentException::class.java) {
            rollForwardExpiry(expiry, Recurrence.EVERY_N_YEARS, intervalYears = null)
        }
    }

    @Test
    fun `EVERY_N_YEARS rejects an out-of-range interval`() {
        assertThrows(IllegalArgumentException::class.java) {
            rollForwardExpiry(expiry, Recurrence.EVERY_N_YEARS, intervalYears = 1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            rollForwardExpiry(expiry, Recurrence.EVERY_N_YEARS, intervalYears = 21)
        }
    }

    @Test
    fun `MONTHLY handles month-end overflow`() {
        val jan31 = LocalDate.of(2026, 1, 31)
        // February has no 31st — java.time clamps to the last valid day.
        assertEquals(LocalDate.of(2026, 2, 28), rollForwardExpiry(jan31, Recurrence.MONTHLY))
    }

    @Test
    fun `rolled forward issue date is old expiry plus one day`() {
        assertEquals(expiry.plusDays(1), rolledForwardIssueDate(expiry))
    }
}
