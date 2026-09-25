package com.lifevault.app.core.domain.lock

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Section 8.4's failure-count → lockout-duration table. Returns [Duration.ZERO] for
 * 1–4 failures (no lockout yet, just a warning after the 3rd).
 */
fun lockoutDurationFor(consecutiveFailures: Int): Duration = when {
    consecutiveFailures < 5 -> Duration.ZERO
    consecutiveFailures == 5 -> 30.seconds
    consecutiveFailures == 6 -> 1.minutes
    consecutiveFailures == 7 -> 5.minutes
    consecutiveFailures == 8 -> 15.minutes
    consecutiveFailures == 9 -> 30.minutes
    else -> 1.hours // >= 10
}

/**
 * A lockout end time recorded in two clocks (Section 8.4): wall-clock epoch millis
 * (survives reboot, but can be manipulated by the user) and an `elapsedRealtime`
 * snapshot (immune to clock changes, but resets to near-zero on reboot).
 */
data class LockoutSchedule(
    val lockoutUntilEpochMs: Long,
    val setAtElapsedRealtimeMs: Long,
    val lockoutUntilElapsedRealtimeMs: Long,
)

fun computeLockoutSchedule(
    consecutiveFailures: Int,
    nowEpochMs: Long,
    nowElapsedRealtimeMs: Long,
): LockoutSchedule? {
    val duration = lockoutDurationFor(consecutiveFailures)
    if (duration == Duration.ZERO) return null
    val durationMs = duration.inWholeMilliseconds
    return LockoutSchedule(
        lockoutUntilEpochMs = nowEpochMs + durationMs,
        setAtElapsedRealtimeMs = nowElapsedRealtimeMs,
        lockoutUntilElapsedRealtimeMs = nowElapsedRealtimeMs + durationMs,
    )
}

/**
 * How much lockout time is left, in millis (0 if none/expired). Section 8.4: "the
 * stricter of the two applies" — normally the larger of the two clocks' remaining
 * time, so neither rolling the wall clock forward nor (within a boot session) any
 * elapsed-time trick shortens a lockout. A detected reboot (elapsedRealtime having
 * gone backward since the schedule was set — it only increases within a boot session)
 * falls back to wall-clock only, since the elapsed-realtime snapshot is meaningless
 * after reboot.
 */
fun remainingLockoutMillis(schedule: LockoutSchedule?, nowEpochMs: Long, nowElapsedRealtimeMs: Long): Long {
    if (schedule == null) return 0
    val epochRemaining = schedule.lockoutUntilEpochMs - nowEpochMs

    val rebooted = nowElapsedRealtimeMs < schedule.setAtElapsedRealtimeMs
    if (rebooted) return epochRemaining.coerceAtLeast(0)

    val elapsedRemaining = schedule.lockoutUntilElapsedRealtimeMs - nowElapsedRealtimeMs
    return maxOf(epochRemaining, elapsedRemaining).coerceAtLeast(0)
}

fun isLockedOut(schedule: LockoutSchedule?, nowEpochMs: Long, nowElapsedRealtimeMs: Long): Boolean =
    remainingLockoutMillis(schedule, nowEpochMs, nowElapsedRealtimeMs) > 0
