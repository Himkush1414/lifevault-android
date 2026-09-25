package com.lifevault.app.core.domain.lock

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class LockoutPolicyTest {

    // --- lockoutDurationFor: the Section 8.4 table, boundary by boundary ---

    @Test
    fun `1 through 4 failures have no lockout`() {
        for (failures in 1..4) {
            assertEquals("failures=$failures", 0L, lockoutDurationFor(failures).inWholeMilliseconds)
        }
    }

    @Test
    fun `failure count to duration table`() {
        assertEquals(30.seconds, lockoutDurationFor(5))
        assertEquals(1.minutes, lockoutDurationFor(6))
        assertEquals(5.minutes, lockoutDurationFor(7))
        assertEquals(15.minutes, lockoutDurationFor(8))
        assertEquals(30.minutes, lockoutDurationFor(9))
        assertEquals(1.hours, lockoutDurationFor(10))
        assertEquals(1.hours, lockoutDurationFor(11))
        assertEquals(1.hours, lockoutDurationFor(100))
    }

    // --- computeLockoutSchedule / remainingLockoutMillis: normal (non-tampered) clock ---

    @Test
    fun `no schedule is created for fewer than 5 failures`() {
        assertNull(computeLockoutSchedule(4, nowEpochMs = 1_000_000, nowElapsedRealtimeMs = 500_000))
    }

    @Test
    fun `remaining time counts down normally as both clocks advance together`() {
        val schedule = computeLockoutSchedule(5, nowEpochMs = 1_000_000, nowElapsedRealtimeMs = 500_000)
        checkNotNull(schedule)

        // 10 seconds later on both clocks.
        val remaining = remainingLockoutMillis(schedule, nowEpochMs = 1_010_000, nowElapsedRealtimeMs = 510_000)

        assertEquals(20_000L, remaining) // 30s lockout - 10s elapsed
    }

    @Test
    fun `lockout expires when both clocks pass the target`() {
        val schedule = computeLockoutSchedule(5, nowEpochMs = 1_000_000, nowElapsedRealtimeMs = 500_000)
        checkNotNull(schedule)

        val remaining = remainingLockoutMillis(schedule, nowEpochMs = 1_031_000, nowElapsedRealtimeMs = 531_000)

        assertEquals(0L, remaining)
        assertFalse(isLockedOut(schedule, nowEpochMs = 1_031_000, nowElapsedRealtimeMs = 531_000))
    }

    // --- Clock-tamper rule: rolling the wall clock forward must not shorten a lockout ---

    @Test
    fun `rolling the wall clock forward does not bypass the lockout`() {
        val schedule = computeLockoutSchedule(7, nowEpochMs = 1_000_000, nowElapsedRealtimeMs = 500_000) // 5 min
        checkNotNull(schedule)

        // Wall clock jumped forward by an hour, but no real time passed (elapsedRealtime
        // barely moved) — the user tried to skip the lockout by changing the date/time.
        val tamperedEpochMs = 1_000_000 + 60 * 60 * 1000L
        val remaining = remainingLockoutMillis(schedule, nowEpochMs = tamperedEpochMs, nowElapsedRealtimeMs = 500_100)

        // elapsedRealtime says ~5 minutes are still left; that's the stricter (larger) value.
        assertTrue(isLockedOut(schedule, nowEpochMs = tamperedEpochMs, nowElapsedRealtimeMs = 500_100))
        assertEquals(5.minutes.inWholeMilliseconds - 100, remaining)
    }

    @Test
    fun `rolling the wall clock backward makes the epoch clock the stricter (larger) one`() {
        val schedule = computeLockoutSchedule(5, nowEpochMs = 1_000_000, nowElapsedRealtimeMs = 500_000) // 30s
        checkNotNull(schedule)

        // Wall clock rolled backward by 100s; 40 real seconds passed (elapsedRealtime
        // advanced normally, so on its own it would say the lockout is over). "Stricter
        // of the two" means the manipulated epoch clock's larger remaining-time claim
        // wins — clock tampering can only extend a lockout from the app's point of view,
        // never shorten one, regardless of which direction the clock moves.
        val tamperedEpochMs = 900_000L
        val remaining = remainingLockoutMillis(schedule, nowEpochMs = tamperedEpochMs, nowElapsedRealtimeMs = 540_000)

        assertEquals(1_030_000L - 900_000L, remaining)
        assertTrue(isLockedOut(schedule, nowEpochMs = tamperedEpochMs, nowElapsedRealtimeMs = 540_000))
    }

    // --- Reboot: elapsedRealtime resets, falls back to wall-clock ---

    @Test
    fun `a detected reboot falls back to wall-clock only`() {
        val schedule = computeLockoutSchedule(9, nowEpochMs = 10_000_000, nowElapsedRealtimeMs = 5_000_000) // 30 min
        checkNotNull(schedule)

        // Reboot: elapsedRealtime resets near zero, far below setAtElapsedRealtimeMs.
        // Wall clock advanced normally by 10 minutes since the lockout was set.
        val afterReboot = remainingLockoutMillis(
            schedule,
            nowEpochMs = 10_000_000 + 10.minutes.inWholeMilliseconds,
            nowElapsedRealtimeMs = 50_000,
        )

        assertEquals(20.minutes.inWholeMilliseconds, afterReboot)
    }

    @Test
    fun `a detected reboot with wall-clock time already elapsed clears the lockout`() {
        val schedule = computeLockoutSchedule(6, nowEpochMs = 10_000_000, nowElapsedRealtimeMs = 5_000_000) // 1 min
        checkNotNull(schedule)

        val afterReboot = remainingLockoutMillis(
            schedule,
            nowEpochMs = 10_000_000 + 5.minutes.inWholeMilliseconds,
            nowElapsedRealtimeMs = 10_000,
        )

        assertEquals(0L, afterReboot)
    }
}
