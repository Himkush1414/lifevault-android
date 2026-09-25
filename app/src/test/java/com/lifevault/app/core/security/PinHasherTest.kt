package com.lifevault.app.core.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PinHasherTest {

    private val hasher = PinHasher()

    // Low iteration count so the test suite stays fast — calibration itself is exercised
    // separately below.
    private val testIterations = MIN_PBKDF2_ITERATIONS

    @Test
    fun `verify succeeds for the correct PIN`() {
        val hash = hasher.hash("284719".toCharArray(), testIterations)

        assertTrue(hasher.verify("284719".toCharArray(), hash))
    }

    @Test
    fun `verify fails for a wrong PIN`() {
        val hash = hasher.hash("284719".toCharArray(), testIterations)

        assertFalse(hasher.verify("284718".toCharArray(), hash))
    }

    @Test
    fun `hashing the same PIN twice produces different salts and hashes`() {
        val first = hasher.hash("284719".toCharArray(), testIterations)
        val second = hasher.hash("284719".toCharArray(), testIterations)

        assertFalse(first.salt.contentEquals(second.salt))
        assertFalse(first.hash.contentEquals(second.hash))
    }

    @Test
    fun `hash zeroes the caller's PIN array`() {
        val pin = "284719".toCharArray()
        hasher.hash(pin, testIterations)

        assertTrue(pin.all { it == '0' })
    }

    @Test
    fun `verify zeroes the caller's PIN array`() {
        val hash = hasher.hash("284719".toCharArray(), testIterations)
        val attempt = "284719".toCharArray()

        hasher.verify(attempt, hash)

        assertTrue(attempt.all { it == '0' })
    }

    @Test
    fun `calibration never returns fewer than the minimum iteration count`() {
        assertTrue(hasher.calibrateIterations() >= MIN_PBKDF2_ITERATIONS)
    }
}
