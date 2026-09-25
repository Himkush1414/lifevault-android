package com.lifevault.app.core.domain.status

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Section 5.4 — always computed from the current date, never stored. */
enum class DocumentStatus { EXPIRED, CRITICAL, DUE_SOON, VALID, NO_EXPIRY }

/**
 * @param expiry null means the document has no expiry date.
 * @param today the caller's current date (never [java.time.LocalDate.now] directly —
 * callers must pass a value derived from an injected `Clock` for testability).
 */
fun documentStatusOf(expiry: LocalDate?, today: LocalDate): DocumentStatus {
    if (expiry == null) return DocumentStatus.NO_EXPIRY
    val days = ChronoUnit.DAYS.between(today, expiry)
    return when {
        days < 0 -> DocumentStatus.EXPIRED
        days <= 7 -> DocumentStatus.CRITICAL
        days <= 30 -> DocumentStatus.DUE_SOON
        else -> DocumentStatus.VALID
    }
}
