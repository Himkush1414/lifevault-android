package com.lifevault.app.core.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OemReliabilityTest {

    @Test
    fun `known aggressive OEMs are detected regardless of case`() {
        assertTrue(isAggressiveBatteryOem("Xiaomi"))
        assertTrue(isAggressiveBatteryOem("SAMSUNG"))
        assertTrue(isAggressiveBatteryOem("oneplus"))
    }

    @Test
    fun `unlisted manufacturers are not flagged`() {
        assertFalse(isAggressiveBatteryOem("Google"))
        assertFalse(isAggressiveBatteryOem("Sony"))
    }
}
