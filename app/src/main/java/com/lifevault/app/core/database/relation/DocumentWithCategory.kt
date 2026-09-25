package com.lifevault.app.core.database.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.lifevault.app.core.database.entity.CategoryEntity
import com.lifevault.app.core.database.entity.DocumentEntity

/** Section 5.5: the shape list rows (Home, Documents, Deadlines, Search) query for. */
data class DocumentWithCategory(
    @Embedded val document: DocumentEntity,
    @Relation(parentColumn = "categoryId", entityColumn = "id")
    val category: CategoryEntity,
)
