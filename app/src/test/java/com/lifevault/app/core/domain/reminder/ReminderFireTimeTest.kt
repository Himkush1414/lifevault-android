package com.lifevault.app.core.domain.reminder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

class ReminderFireTimeTest {

    private val utc: ZoneId = ZoneOffset.UTC
    private val expiry = LocalDate.of(2026, 9, 30)

    @Suppress("LongParameterList") // test fixture builder, one param per ReminderFireInput field
    private fun baseInput(
        expiryDate: LocalDate? = expiry,
        offsetDays: Int = 7,
        timeOfDayMinutes: Int? = null,
        dailyCheckMinutes: Int = 9 * 60,
        enabled: Boolean = true,
        documentDeleted: Boolean = false,
        lastFiredForExpiry: LocalDate? = null,
    ) = ReminderFireInput(
        expiryDate, offsetDays, timeOfDayMinutes, dailyCheckMinutes, enabled, documentDeleted, lastFiredForExpiry,
    )

    // --- fireDateOf / fireInstantOf ---

    @Test
    fun `fire date is expiry minus offset`() {
        assertEquals(LocalDate.of(2026, 9, 23), fireDateOf(expiry, 7))
    }

    @Test
    fun `fire date with zero offset is expiry itself`() {
        assertEquals(expiry, fireDateOf(expiry, 0))
    }

    @Test
    fun `fire instant combines date, time and zone`() {
        val instant = fireInstantOf(LocalDate.of(2026, 9, 23), 9 * 60, utc)
        assertEquals(Instant.parse("2026-09-23T09:00:00Z"), instant)
    }

    @Test
    fun `fire instant across a DST spring-forward transition in London`() {
        val london = ZoneId.of("Europe/London")
        // 2026-03-29 is BST's spring-forward date in the UK.
        val instant = fireInstantOf(LocalDate.of(2026, 3, 29), 9 * 60, london)
        // 09:00 local on the transition day is unambiguous (clocks jump at 01:00), so
        // this should resolve to 08:00 UTC (BST = UTC+1).
        assertEquals(Instant.parse("2026-03-29T08:00:00Z"), instant)
    }

    // --- isReminderEligible ---

    @Test
    fun `eligible when enabled, not deleted, has expiry, not already fired for it`() {
        assertTrue(isReminderEligible(baseInput()))
    }

    @Test
    fun `not eligible when disabled`() {
        assertFalse(isReminderEligible(baseInput(enabled = false)))
    }

    @Test
    fun `not eligible when document deleted`() {
        assertFalse(isReminderEligible(baseInput(documentDeleted = true)))
    }

    @Test
    fun `not eligible when expiry is null`() {
        assertFalse(isReminderEligible(baseInput(expiryDate = null)))
    }

    @Test
    fun `not eligible when already fired for this exact expiry`() {
        assertFalse(isReminderEligible(baseInput(lastFiredForExpiry = expiry)))
    }

    @Test
    fun `eligible again when expiry changed since last fire`() {
        assertTrue(isReminderEligible(baseInput(lastFiredForExpiry = expiry.minusDays(1))))
    }

    // --- isReminderDue ---

    @Test
    fun `due when fire instant has passed`() {
        val input = baseInput(offsetDays = 7) // fires 2026-09-23 09:00
        val now = Instant.parse("2026-09-23T09:00:01Z")
        assertTrue(isReminderDue(input, now, utc))
    }

    @Test
    fun `not due when fire instant is in the future`() {
        val input = baseInput(offsetDays = 7)
        val now = Instant.parse("2026-09-23T08:59:59Z")
        assertFalse(isReminderDue(input, now, utc))
    }

    @Test
    fun `due exactly at the fire instant`() {
        val input = baseInput(offsetDays = 7)
        val now = Instant.parse("2026-09-23T09:00:00Z")
        assertTrue(isReminderDue(input, now, utc))
    }

    @Test
    fun `not due when not eligible even if time has passed`() {
        val input = baseInput(offsetDays = 7, enabled = false)
        val now = Instant.parse("2030-01-01T00:00:00Z")
        assertFalse(isReminderDue(input, now, utc))
    }

    @Test
    fun `reminder-specific time of day overrides the daily check time`() {
        val input = baseInput(offsetDays = 0, timeOfDayMinutes = 18 * 60, dailyCheckMinutes = 9 * 60)
        val atDailyCheckTime = Instant.parse("2026-09-30T09:00:00Z")
        val atReminderTime = Instant.parse("2026-09-30T18:00:00Z")
        assertFalse(isReminderDue(input, atDailyCheckTime, utc))
        assertTrue(isReminderDue(input, atReminderTime, utc))
    }

    @Test
    fun `offset 0 and offset 365 both compute without error`() {
        assertTrue(isReminderDue(baseInput(offsetDays = 0), Instant.parse("2026-09-30T09:00:00Z"), utc))
        val farExpiry = LocalDate.of(2027, 9, 30)
        val input = baseInput(expiryDate = farExpiry, offsetDays = 365)
        assertEquals(farExpiry.minusDays(365), fireDateOf(farExpiry, 365))
        assertTrue(isReminderDue(input, Instant.parse("2026-09-30T09:00:00Z"), utc))
    }

    // --- isWithinCatchUpWindow ---

    @Test
    fun `within catch-up window at the boundary (7 days)`() {
        val today = LocalDate.of(2026, 9, 30)
        assertTrue(isWithinCatchUpWindow(today.minusDays(7), today))
    }

    @Test
    fun `outside catch-up window past 7 days`() {
        val today = LocalDate.of(2026, 9, 30)
        assertFalse(isWithinCatchUpWindow(today.minusDays(8), today))
    }

    @Test
    fun `fire date today is within the catch-up window`() {
        val today = LocalDate.of(2026, 9, 30)
        assertTrue(isWithinCatchUpWindow(today, today))
    }

    // --- shouldSuppressAtSave ---

    @Test
    fun `suppress when fire date is in the past`() {
        val today = LocalDate.of(2026, 9, 30)
        assertTrue(shouldSuppressAtSave(today.minusDays(1), today))
    }

    @Test
    fun `suppress when fire date is today`() {
        val today = LocalDate.of(2026, 9, 30)
        assertTrue(shouldSuppressAtSave(today, today))
    }

    @Test
    fun `do not suppress a future fire date`() {
        val today = LocalDate.of(2026, 9, 30)
        assertFalse(shouldSuppressAtSave(today.plusDays(1), today))
    }
}
