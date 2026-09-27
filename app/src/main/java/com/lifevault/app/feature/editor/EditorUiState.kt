package com.lifevault.app.feature.editor

import com.lifevault.app.core.database.entity.CategoryEntity
import java.time.LocalDate

data class EditorUiState(
    val documentId: String? = null,
    val isLoading: Boolean = true,
    val title: String = "",
    val categories: List<CategoryEntity> = emptyList(),
    val categoryId: String = "",
    val documentNumber: String = "",
    val issuer: String = "",
    val issueDate: LocalDate? = null,
    val expiryDate: LocalDate? = null,
    val noExpiry: Boolean = true,
    val notes: String = "",
    val today: LocalDate = LocalDate.now(),
    val defaultReminderOffsets: List<Int> = emptyList(),
    val isDirty: Boolean = false,
    val isSaving: Boolean = false,
    val savedDocumentId: String? = null,
) {
    /** S13: "Save" is enabled only when the title is non-blank. */
    val canSave: Boolean get() = title.isNotBlank()

    /** S13: a non-blocking warning, not a validation error — "This document is already expired." */
    val expiryAlreadyPastWarning: Boolean
        get() = !noExpiry && expiryDate != null && expiryDate.isBefore(today)

    val isEditingExisting: Boolean get() = documentId != null
}
