package com.lifevault.app.core.domain.reminder

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Section 7.2: overdue nudges stop after this many weeks past expiry. */
const val OVERDUE_NUDGE_MAX_WEEKS = 12

/**
 * Pro overdue nudges (Section 7.2): fire once every 7 days after expiry — the same
 * weekday as the expiry date — until the document is renewed or 12 weeks pass.
 *
 * @param lastOverdueNudgeAt the calendar date of the last nudge, or null if none yet
 * (also null again after the expiry date changes — Section 7.2 reset rule).
 */
fun isOverdueNudgeDue(expiryDate: LocalDate, lastOverdueNudgeAt: LocalDate?, today: LocalDate): Boolean {
    val daysSinceExpiry = ChronoUnit.DAYS.between(expiryDate, today)
    if (daysSinceExpiry <= 0) return false
    if (daysSinceExpiry > OVERDUE_NUDGE_MAX_WEEKS * 7L) return false
    if (daysSinceExpiry % 7L != 0L) return false

    val lastNudgeDaysSinceExpiry = lastOverdueNudgeAt?.let { ChronoUnit.DAYS.between(expiryDate, it) }
    return lastNudgeDaysSinceExpiry == null || lastNudgeDaysSinceExpiry < daysSinceExpiry
}
