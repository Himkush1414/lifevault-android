package com.lifevault.app.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("documentId"), Index(value = ["documentId", "offsetDays"], unique = true)],
)
data class ReminderEntity(
    @PrimaryKey val id: String,
    val documentId: String,
    val offsetDays: Int, // 0..365 days before expiry (0 = on expiry day)
    val timeOfDayMinutes: Int?, // null = use global daily check time; Pro may set
    val enabled: Boolean,
    val lastFiredForExpiry: LocalDate?, // expiry date this reminder last fired for (idempotency)
    val lastFiredAt: Instant?,
    val createdAt: Instant,
)
