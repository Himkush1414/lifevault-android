package com.lifevault.app.core.database.converter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `LocalDate round-trips through epoch day`() {
        val date = LocalDate.of(2026, 9, 25)
        val epochDay = converters.fromLocalDate(date)
        assertEquals(date, converters.toLocalDate(epochDay))
    }

    @Test
    fun `null LocalDate round-trips to null`() {
        assertNull(converters.fromLocalDate(null))
        assertNull(converters.toLocalDate(null))
    }

    @Test
    fun `Instant round-trips through epoch millis`() {
        val instant = Instant.parse("2026-09-25T09:00:00Z")
        val millis = converters.fromInstant(instant)
        assertEquals(instant, converters.toInstant(millis))
    }

    @Test
    fun `null Instant round-trips to null`() {
        assertNull(converters.fromInstant(null))
        assertNull(converters.toInstant(null))
    }

    @Test
    fun `Int list round-trips through JSON`() {
        val list = listOf(90, 30, 7, 0)
        val json = converters.fromIntList(list)
        assertEquals(list, converters.toIntList(json))
    }

    @Test
    fun `empty Int list round-trips`() {
        val json = converters.fromIntList(emptyList())
        assertEquals(emptyList<Int>(), converters.toIntList(json))
    }

    @Test
    fun `null Int list round-trips to null`() {
        assertNull(converters.fromIntList(null))
        assertNull(converters.toIntList(null))
    }
}
