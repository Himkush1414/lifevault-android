package com.lifevault.app.core.domain.reminder

import java.time.LocalDate

/** Section 5.3 `documents.recurrence`. `EVERY_N_YEARS` is Pro-only (Section 1.1). */
enum class Recurrence { NONE, MONTHLY, QUARTERLY, YEARLY, EVERY_N_YEARS }

/**
 * "Mark renewed" (Section 7.2): rolls `expiryDate` forward by the recurrence period.
 * @param intervalYears required (2..20) when [recurrence] is [Recurrence.EVERY_N_YEARS].
 */
fun rollForwardExpiry(currentExpiry: LocalDate, recurrence: Recurrence, intervalYears: Int? = null): LocalDate =
    when (recurrence) {
        Recurrence.NONE -> currentExpiry
        Recurrence.MONTHLY -> currentExpiry.plusMonths(1)
        Recurrence.QUARTERLY -> currentExpiry.plusMonths(3)
        Recurrence.YEARLY -> currentExpiry.plusYears(1)
        Recurrence.EVERY_N_YEARS -> {
            require(intervalYears != null && intervalYears in 2..20) {
                "intervalYears must be 2..20 for EVERY_N_YEARS, was $intervalYears"
            }
            currentExpiry.plusYears(intervalYears.toLong())
        }
    }

/** Section 7.2: `issueDate` rolls to the old expiry + 1 day, if the document has one set. */
fun rolledForwardIssueDate(oldExpiry: LocalDate): LocalDate = oldExpiry.plusDays(1)
