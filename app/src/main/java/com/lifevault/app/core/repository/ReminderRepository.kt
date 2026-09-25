package com.lifevault.app.core.repository

import com.lifevault.app.core.database.dao.ReminderDao
import com.lifevault.app.core.database.entity.ReminderEntity
import com.lifevault.app.core.domain.reminder.CATCH_UP_WINDOW_DAYS
import com.lifevault.app.core.domain.reminder.fireDateOf
import com.lifevault.app.core.domain.reminder.shouldSuppressAtSave
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class ReminderRepository @Inject constructor(
    private val reminderDao: ReminderDao,
    private val clock: Clock,
) {
    fun observeForDocument(documentId: String): Flow<List<ReminderEntity>> =
        reminderDao.observeForDocument(documentId)

    /** For [com.lifevault.app.core.domain.feature.FeatureGate.canAddReminder]. */
    suspend fun countForDocument(documentId: String): Int = reminderDao.countForDocument(documentId)

    /**
     * Section 7.2 suppression-at-save: if this reminder's fire date already fell on or
     * before today, mark it fired immediately instead of letting it notify the moment
     * it's saved — the user just looked at this date.
     */
    suspend fun addReminder(
        documentId: String,
        offsetDays: Int,
        timeOfDayMinutes: Int?,
        expiryDate: LocalDate?,
    ): ReminderEntity {
        val now = Instant.now(clock)
        val suppressed = isAlreadyPastFireDate(expiryDate, offsetDays)

        val reminder = ReminderEntity(
            id = UUID.randomUUID().toString(),
            documentId = documentId,
            offsetDays = offsetDays,
            timeOfDayMinutes = timeOfDayMinutes,
            enabled = true,
            lastFiredForExpiry = if (suppressed) expiryDate else null,
            lastFiredAt = if (suppressed) now else null,
            createdAt = now,
        )
        reminderDao.insert(reminder)
        return reminder
    }

    suspend fun update(reminder: ReminderEntity) = reminderDao.update(reminder)

    suspend fun delete(reminder: ReminderEntity) = reminderDao.delete(reminder)

    suspend fun deleteAllForDocument(documentId: String) = reminderDao.deleteAllForDocument(documentId)

    /**
     * Section 7.2: "expiry changes ... naturally re-arm all reminders because
     * lastFiredForExpiry no longer equals the new expiryDate" — except any reminder
     * whose new fire date is itself already past, which is suppressed the same way a
     * freshly created one would be.
     */
    suspend fun rearmForNewExpiry(documentId: String, newExpiryDate: LocalDate?) {
        val now = Instant.now(clock)
        for (reminder in reminderDao.observeForDocument(documentId).first()) {
            val suppressed = isAlreadyPastFireDate(newExpiryDate, reminder.offsetDays)
            reminderDao.update(
                reminder.copy(
                    lastFiredForExpiry = if (suppressed) newExpiryDate else null,
                    lastFiredAt = if (suppressed) now else reminder.lastFiredAt,
                ),
            )
        }
    }

    private fun isAlreadyPastFireDate(expiryDate: LocalDate?, offsetDays: Int): Boolean {
        val expiry = expiryDate ?: return false
        val today = LocalDate.now(clock)
        return shouldSuppressAtSave(fireDateOf(expiry, offsetDays), today)
    }

    /** Section 7.3 `runCheck()` step 2's candidate query, using the Section 7.2 catch-up window. */
    suspend fun candidatesDueForCheck(today: LocalDate): List<ReminderEntity> =
        reminderDao.candidatesDueForCheck(today, today.minusDays(CATCH_UP_WINDOW_DAYS))

    suspend fun markFired(id: String, expiry: LocalDate, firedAt: Instant) =
        reminderDao.markFired(id, expiry, firedAt)
}
