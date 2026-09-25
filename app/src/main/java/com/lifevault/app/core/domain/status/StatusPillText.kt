package com.lifevault.app.core.domain.status

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToLong

/**
 * The compact StatusPill label used in list rows (Section 4.8's text-rule table):
 * Expired → "Expired"; 0 days → "Today"; 1 → "Tomorrow"; 2–30 → "N days";
 * 31–60 → "N weeks"; > 60 → "MMM yyyy". No expiry → "No expiry".
 *
 * "MMM yyyy" is a deliberate, spec-named exception to the "never hard-code a date
 * pattern" rule (Section 4.8) — it's this one compact pill bucket, not a screen date.
 */
fun statusPillListLabel(expiry: LocalDate?, today: LocalDate, locale: Locale = Locale.getDefault()): String {
    if (expiry == null) return "No expiry"
    val days = ChronoUnit.DAYS.between(today, expiry)
    return when {
        days < 0 -> "Expired"
        days == 0L -> "Today"
        days == 1L -> "Tomorrow"
        days <= 30 -> "$days days"
        days <= 60 -> "${(days / 7.0).roundToLong()} weeks"
        else -> DateTimeFormatter.ofPattern("MMM yyyy", locale).format(expiry)
    }
}

/**
 * The long form used on the detail screen (Section 3.3 S09): "Expires in 12 days",
 * "Expired 3 days ago", "Valid until 14 Mar 2031".
 */
fun statusPillDetailLabel(expiry: LocalDate?, today: LocalDate, locale: Locale = Locale.getDefault()): String {
    if (expiry == null) return "No expiry"
    val days = ChronoUnit.DAYS.between(today, expiry)
    return when {
        days < 0 -> "Expired ${-days} day${if (days == -1L) "" else "s"} ago"
        days == 0L -> "Expires today"
        days == 1L -> "Expires tomorrow"
        days <= 30 -> "Expires in $days days"
        else -> {
            val formatted = expiry.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale))
            "Valid until $formatted"
        }
    }
}
