package com.lifevault.app.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lifevault.app.core.database.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate

@Dao
interface ReminderDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(reminder: ReminderEntity)

    @Update
    suspend fun update(reminder: ReminderEntity)

    @Delete
    suspend fun delete(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE documentId = :documentId")
    suspend fun deleteAllForDocument(documentId: String)

    @Query("SELECT * FROM reminders WHERE documentId = :documentId ORDER BY offsetDays ASC")
    fun observeForDocument(documentId: String): Flow<List<ReminderEntity>>

    @Query("SELECT COUNT(*) FROM reminders WHERE documentId = :documentId")
    suspend fun countForDocument(documentId: String): Int

    // Section 7.3 runCheck() step 2's candidate query.
    @Query(
        """
        SELECT r.* FROM reminders r
        JOIN documents d ON d.id = r.documentId
        WHERE r.enabled = 1 AND d.deletedAt IS NULL AND d.expiryDate IS NOT NULL
          AND (d.expiryDate - r.offsetDays) BETWEEN :windowStart AND :today
          AND (r.lastFiredForExpiry IS NULL OR r.lastFiredForExpiry != d.expiryDate)
        """,
    )
    suspend fun candidatesDueForCheck(today: LocalDate, windowStart: LocalDate): List<ReminderEntity>

    @Query("UPDATE reminders SET lastFiredForExpiry = :expiry, lastFiredAt = :firedAt WHERE id = :id")
    suspend fun markFired(id: String, expiry: LocalDate, firedAt: Instant)
}
