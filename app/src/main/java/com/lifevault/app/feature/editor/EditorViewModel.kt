package com.lifevault.app.feature.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.lifevault.app.core.database.entity.DocumentEntity
import com.lifevault.app.core.domain.reminder.Recurrence
import com.lifevault.app.core.navigation.Routes
import com.lifevault.app.core.repository.CategoryRepository
import com.lifevault.app.core.repository.DocumentRepository
import com.lifevault.app.core.repository.ReminderRepository
import com.lifevault.app.core.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

/** S13 (Section 3.3): create/edit a document. New docs get default reminders from Settings. */
@HiltViewModel
class EditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val documentRepository: DocumentRepository,
    private val categoryRepository: CategoryRepository,
    private val reminderRepository: ReminderRepository,
    private val settingsRepository: SettingsRepository,
    private val clock: Clock,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Routes.Editor>()

    private val _state = MutableStateFlow(EditorUiState(documentId = route.documentId, today = LocalDate.now(clock)))
    val state: StateFlow<EditorUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val categories = categoryRepository.observeAll().first()
            val defaultOffsets = settingsRepository.observe().first().defaultReminderOffsets

            val existing = route.documentId?.let { documentRepository.observeById(it).first() }
            _state.update {
                if (existing != null) {
                    val doc = existing.document
                    it.copy(
                        isLoading = false,
                        categories = categories,
                        categoryId = doc.categoryId,
                        title = doc.title,
                        documentNumber = doc.documentNumber.orEmpty(),
                        issuer = doc.issuer.orEmpty(),
                        issueDate = doc.issueDate,
                        expiryDate = doc.expiryDate,
                        noExpiry = doc.expiryDate == null,
                        notes = doc.notes.orEmpty(),
                        defaultReminderOffsets = defaultOffsets,
                    )
                } else {
                    it.copy(
                        isLoading = false,
                        categories = categories,
                        categoryId = categories.firstOrNull { c -> c.id == "cat_other" }?.id
                            ?: categories.firstOrNull()?.id.orEmpty(),
                        defaultReminderOffsets = defaultOffsets,
                    )
                }
            }
        }
    }

    fun onTitleChange(value: String) = update { it.copy(title = value.take(80), isDirty = true) }
    fun onCategoryChange(categoryId: String) = update { it.copy(categoryId = categoryId, isDirty = true) }
    fun onDocumentNumberChange(value: String) = update { it.copy(documentNumber = value.take(40), isDirty = true) }
    fun onIssuerChange(value: String) = update { it.copy(issuer = value.take(80), isDirty = true) }
    fun onIssueDateChange(date: LocalDate?) = update { it.copy(issueDate = date, isDirty = true) }
    fun onExpiryDateChange(date: LocalDate?) = update { it.copy(expiryDate = date, isDirty = true) }
    fun onNotesChange(value: String) = update { it.copy(notes = value.take(2000), isDirty = true) }

    fun onNoExpiryToggle(noExpiry: Boolean) = update {
        it.copy(noExpiry = noExpiry, expiryDate = if (noExpiry) null else it.expiryDate, isDirty = true)
    }

    private inline fun update(transform: (EditorUiState) -> EditorUiState) {
        _state.update(transform)
    }

    fun save() {
        val current = _state.value
        if (!current.canSave || current.isSaving) return
        _state.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            if (current.isEditingExisting) {
                saveExisting(current)
            } else {
                saveNew(current)
            }
        }
    }

    private suspend fun saveExisting(current: EditorUiState) {
        val documentId = requireNotNull(current.documentId)
        val existing = documentRepository.observeById(documentId).first()?.document ?: return
        val expiryChanged = existing.expiryDate != current.expiryDate

        documentRepository.update(existing.withEditedFieldsFrom(current))
        if (expiryChanged) {
            reminderRepository.rearmForNewExpiry(documentId, current.expiryDate)
        }
        _state.update { it.copy(isSaving = false, isDirty = false, savedDocumentId = documentId) }
    }

    private suspend fun saveNew(current: EditorUiState) {
        val created = documentRepository.create { id, now ->
            DocumentEntity(
                id = id,
                title = current.title.trim(),
                categoryId = current.categoryId,
                documentNumber = current.documentNumber.trim().ifBlank { null },
                issuer = current.issuer.trim().ifBlank { null },
                issueDate = current.issueDate,
                expiryDate = current.expiryDate,
                notes = current.notes.trim().ifBlank { null },
                recurrence = Recurrence.NONE,
                recurrenceIntervalYears = null,
                renewalCostMinor = null,
                renewalCurrency = null,
                overdueNudges = false,
                lastOverdueNudgeAt = null,
                isFavorite = false,
                ocrText = null,
                createdAt = now,
                updatedAt = now,
                deletedAt = null,
            )
        }

        if (current.expiryDate != null) {
            for (offset in current.defaultReminderOffsets) {
                reminderRepository.addReminder(created.id, offset, null, current.expiryDate)
            }
        }

        _state.update { it.copy(isSaving = false, isDirty = false, savedDocumentId = created.id) }
    }
}

private fun DocumentEntity.withEditedFieldsFrom(state: EditorUiState): DocumentEntity = copy(
    title = state.title.trim(),
    categoryId = state.categoryId,
    documentNumber = state.documentNumber.trim().ifBlank { null },
    issuer = state.issuer.trim().ifBlank { null },
    issueDate = state.issueDate,
    expiryDate = state.expiryDate,
    notes = state.notes.trim().ifBlank { null },
)
