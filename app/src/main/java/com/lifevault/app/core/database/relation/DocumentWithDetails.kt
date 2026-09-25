package com.lifevault.app.core.database.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.lifevault.app.core.database.entity.AttachmentEntity
import com.lifevault.app.core.database.entity.CategoryEntity
import com.lifevault.app.core.database.entity.DocumentEntity
import com.lifevault.app.core.database.entity.ReminderEntity

/** Section 5.5: the full shape the document detail screen (S09) queries for. */
data class DocumentWithDetails(
    @Embedded val document: DocumentEntity,
    @Relation(parentColumn = "categoryId", entityColumn = "id")
    val category: CategoryEntity,
    @Relation(parentColumn = "id", entityColumn = "documentId")
    val attachments: List<AttachmentEntity>,
    @Relation(parentColumn = "id", entityColumn = "documentId")
    val reminders: List<ReminderEntity>,
)
