package com.lifevault.app.core.repository.fake

import com.lifevault.app.core.database.dao.ReminderDao
import com.lifevault.app.core.database.entity.ReminderEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate

class FakeReminderDao : ReminderDao {
    private val state = MutableStateFlow<List<ReminderEntity>>(emptyList())
    val current: List<ReminderEntity> get() = state.value

    override suspend fun insert(reminder: ReminderEntity) {
        state.value = state.value + reminder
    }

    override suspend fun update(reminder: ReminderEntity) {
        state.value = state.value.map { if (it.id == reminder.id) reminder else it }
    }

    override suspend fun delete(reminder: ReminderEntity) {
        state.value = state.value.filterNot { it.id == reminder.id }
    }

    override suspend fun deleteAllForDocument(documentId: String) {
        state.value = state.value.filterNot { it.documentId == documentId }
    }

    override fun observeForDocument(documentId: String) =
        state.map { list -> list.filter { it.documentId == documentId }.sortedBy(ReminderEntity::offsetDays) }

    override suspend fun countForDocument(documentId: String): Int =
        state.value.count { it.documentId == documentId }

    override suspend fun candidatesDueForCheck(today: LocalDate, windowStart: LocalDate): List<ReminderEntity> =
        error("not needed by repository-layer tests")

    override suspend fun markFired(id: String, expiry: LocalDate, firedAt: Instant) {
        state.value = state.value.map {
            if (it.id == id) it.copy(lastFiredForExpiry = expiry, lastFiredAt = firedAt) else it
        }
    }
}
