package com.lifevault.app.core.domain.status

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

class StatusPillTextTest {

    private val today = LocalDate.of(2026, 9, 25)
    private val locale = Locale.forLanguageTag("en-US")

    // --- statusPillListLabel ---

    @Test
    fun `list label no expiry`() {
        assertEquals("No expiry", statusPillListLabel(null, today, locale))
    }

    @Test
    fun `list label expired`() {
        assertEquals("Expired", statusPillListLabel(today.minusDays(3), today, locale))
    }

    @Test
    fun `list label today`() {
        assertEquals("Today", statusPillListLabel(today, today, locale))
    }

    @Test
    fun `list label tomorrow`() {
        assertEquals("Tomorrow", statusPillListLabel(today.plusDays(1), today, locale))
    }

    @Test
    fun `list label 2 days`() {
        assertEquals("2 days", statusPillListLabel(today.plusDays(2), today, locale))
    }

    @Test
    fun `list label 30 days`() {
        assertEquals("30 days", statusPillListLabel(today.plusDays(30), today, locale))
    }

    @Test
    fun `list label 31 days rounds to weeks`() {
        assertEquals("4 weeks", statusPillListLabel(today.plusDays(31), today, locale))
    }

    @Test
    fun `list label 60 days rounds to weeks`() {
        assertEquals("9 weeks", statusPillListLabel(today.plusDays(60), today, locale))
    }

    @Test
    fun `list label 61 days is short date`() {
        // 2026-09-25 + 61 days = 2026-11-25.
        val expiry = today.plusDays(61)
        val label = statusPillListLabel(expiry, today, locale)
        assertEquals("Nov 2026", label)
    }

    // --- statusPillDetailLabel ---

    @Test
    fun `detail label no expiry`() {
        assertEquals("No expiry", statusPillDetailLabel(null, today, locale))
    }

    @Test
    fun `detail label expired one day`() {
        assertEquals("Expired 1 day ago", statusPillDetailLabel(today.minusDays(1), today, locale))
    }

    @Test
    fun `detail label expired several days`() {
        assertEquals("Expired 3 days ago", statusPillDetailLabel(today.minusDays(3), today, locale))
    }

    @Test
    fun `detail label expires today`() {
        assertEquals("Expires today", statusPillDetailLabel(today, today, locale))
    }

    @Test
    fun `detail label expires tomorrow`() {
        assertEquals("Expires tomorrow", statusPillDetailLabel(today.plusDays(1), today, locale))
    }

    @Test
    fun `detail label expires in 12 days`() {
        assertEquals("Expires in 12 days", statusPillDetailLabel(today.plusDays(12), today, locale))
    }

    @Test
    fun `detail label expires in 30 days`() {
        assertEquals("Expires in 30 days", statusPillDetailLabel(today.plusDays(30), today, locale))
    }

    @Test
    fun `detail label valid until long date`() {
        val expiry = LocalDate.of(2031, 3, 14)
        val farToday = LocalDate.of(2026, 1, 1)
        val label = statusPillDetailLabel(expiry, farToday, locale)
        assertEquals("Valid until Mar 14, 2031", label)
    }
}
