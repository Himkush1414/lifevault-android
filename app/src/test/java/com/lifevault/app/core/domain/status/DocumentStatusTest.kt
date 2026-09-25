package com.lifevault.app.core.domain.status

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DocumentStatusTest {

    private val today = LocalDate.of(2026, 9, 25)

    @Test
    fun `null expiry is NO_EXPIRY`() {
        assertEquals(DocumentStatus.NO_EXPIRY, documentStatusOf(null, today))
    }

    @Test
    fun `one day past is EXPIRED`() {
        assertEquals(DocumentStatus.EXPIRED, documentStatusOf(today.minusDays(1), today))
    }

    @Test
    fun `far past is EXPIRED`() {
        assertEquals(DocumentStatus.EXPIRED, documentStatusOf(today.minusYears(3), today))
    }

    @Test
    fun `today (0 days) is CRITICAL`() {
        assertEquals(DocumentStatus.CRITICAL, documentStatusOf(today, today))
    }

    @Test
    fun `7 days is CRITICAL`() {
        assertEquals(DocumentStatus.CRITICAL, documentStatusOf(today.plusDays(7), today))
    }

    @Test
    fun `8 days is DUE_SOON`() {
        assertEquals(DocumentStatus.DUE_SOON, documentStatusOf(today.plusDays(8), today))
    }

    @Test
    fun `30 days is DUE_SOON`() {
        assertEquals(DocumentStatus.DUE_SOON, documentStatusOf(today.plusDays(30), today))
    }

    @Test
    fun `31 days is VALID`() {
        assertEquals(DocumentStatus.VALID, documentStatusOf(today.plusDays(31), today))
    }

    @Test
    fun `far future is VALID`() {
        assertEquals(DocumentStatus.VALID, documentStatusOf(today.plusYears(5), today))
    }
}
