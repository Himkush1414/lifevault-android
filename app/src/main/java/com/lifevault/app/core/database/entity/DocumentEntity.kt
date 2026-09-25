package com.lifevault.app.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lifevault.app.core.domain.reminder.Recurrence
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "documents",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index("categoryId"), Index("expiryDate"), Index("deletedAt"), Index("updatedAt"),
    ],
)
data class DocumentEntity(
    @PrimaryKey val id: String,
    val title: String, // 1..80
    val categoryId: String,
    val documentNumber: String?, // 0..40
    val issuer: String?, // 0..80
    val issueDate: LocalDate?,
    val expiryDate: LocalDate?, // null = no expiry
    val notes: String?, // 0..2000
    val recurrence: Recurrence,
    val recurrenceIntervalYears: Int?, // for EVERY_N_YEARS, 2..20
    val renewalCostMinor: Long?, // Pro
    val renewalCurrency: String?, // ISO-4217
    val overdueNudges: Boolean, // Pro: weekly after expiry
    val lastOverdueNudgeAt: Instant?, // Pro: weekly nudge bookkeeping
    val isFavorite: Boolean,
    val ocrText: String?, // normalised OCR text for search (max 20k chars)
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant?, // non-null = in Trash
)
