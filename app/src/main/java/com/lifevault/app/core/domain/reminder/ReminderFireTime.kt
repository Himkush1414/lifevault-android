package com.lifevault.app.core.domain.reminder

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/** How far back a missed reminder still fires normally (Section 7.2 catch-up window). */
const val CATCH_UP_WINDOW_DAYS = 7L

/** The fields Section 7.2's fire-time formula needs, decoupled from the Room entity. */
data class ReminderFireInput(
    val expiryDate: LocalDate?,
    val offsetDays: Int,
    val timeOfDayMinutes: Int?,
    val dailyCheckMinutes: Int,
    val enabled: Boolean,
    val documentDeleted: Boolean,
    val lastFiredForExpiry: LocalDate?,
)

/** `fireDate = expiryDate − offsetDays` (Section 7.2). */
fun fireDateOf(expiryDate: LocalDate, offsetDays: Int): LocalDate = expiryDate.minusDays(offsetDays.toLong())

/** `fireInstant = fireDate.atTime(fireTime).atZone(zone).toInstant()` (Section 7.2). */
fun fireInstantOf(fireDate: LocalDate, fireTimeMinutes: Int, zone: ZoneId): Instant {
    val time = LocalTime.of(fireTimeMinutes / 60, fireTimeMinutes % 60)
    return fireDate.atTime(time).atZone(zone).toInstant()
}

/** `eligible = enabled && document not deleted && expiryDate != null && lastFiredForExpiry != expiryDate`. */
fun isReminderEligible(input: ReminderFireInput): Boolean =
    input.enabled &&
        !input.documentDeleted &&
        input.expiryDate != null &&
        input.lastFiredForExpiry != input.expiryDate

/** `due = eligible && fireInstant <= now` (Section 7.2). */
fun isReminderDue(input: ReminderFireInput, now: Instant, zone: ZoneId): Boolean {
    val expiry = input.expiryDate ?: return false
    if (!isReminderEligible(input)) return false
    val fireDate = fireDateOf(expiry, input.offsetDays)
    val fireTimeMinutes = input.timeOfDayMinutes ?: input.dailyCheckMinutes
    return fireInstantOf(fireDate, fireTimeMinutes, zone) <= now
}

/**
 * Section 7.2 catch-up window: a due reminder whose `fireDate` falls within the last
 * [CATCH_UP_WINDOW_DAYS] still fires normally. Older misses are the caller's job to
 * collapse into a single overdue notification or silently mark fired.
 */
fun isWithinCatchUpWindow(fireDate: LocalDate, today: LocalDate): Boolean {
    val daysSinceFire = ChronoUnit.DAYS.between(fireDate, today)
    return daysSinceFire in 0..CATCH_UP_WINDOW_DAYS
}

/**
 * Section 7.2 suppression-at-save: when a document/reminder is created or edited, a
 * reminder whose `fireDate` already fell on or before today should not fire immediately
 * — the user just looked at this date. Callers set `lastFiredForExpiry = expiryDate` for
 * every reminder where this returns true; future reminders stay armed.
 */
fun shouldSuppressAtSave(fireDate: LocalDate, today: LocalDate): Boolean = !fireDate.isAfter(today)
