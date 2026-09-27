package com.lifevault.app.feature.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.lifevault.app.core.database.entity.ReminderEntity
import com.lifevault.app.core.domain.feature.FeatureGate
import com.lifevault.app.core.navigation.Routes
import com.lifevault.app.core.repository.DocumentRepository
import com.lifevault.app.core.repository.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReminderEditorUiState(
    val reminders: List<ReminderEntity> = emptyList(),
    val isPro: Boolean = false, // TODO(step 21 - billing): read the real entitlement.
    val duplicateOffsetError: Int? = null,
) {
    val canAddMore: Boolean get() = FeatureGate.canAddReminder(reminders.size, isPro)
    val presetOffsets: List<Int> get() = FeatureGate.FREE_REMINDER_OFFSET_PRESETS
}

/** S14 (Section 3.3): add/remove reminders for an existing document. */
@HiltViewModel
class ReminderEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val reminderRepository: ReminderRepository,
    private val documentRepository: DocumentRepository,
) : ViewModel() {

    private val documentId = savedStateHandle.toRoute<Routes.ReminderEditor>().documentId
    private val duplicateError = MutableStateFlow<Int?>(null)

    val state: StateFlow<ReminderEditorUiState> = combine(
        reminderRepository.observeForDocument(documentId),
        duplicateError,
    ) { reminders, error ->
        ReminderEditorUiState(reminders = reminders.sortedBy { it.offsetDays }, duplicateOffsetError = error)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ReminderEditorUiState())

    fun addPreset(offsetDays: Int) {
        viewModelScope.launch {
            if (state.value.reminders.any { it.offsetDays == offsetDays }) {
                duplicateError.value = offsetDays
                return@launch
            }
            duplicateError.value = null
            val expiryDate = documentRepository.observeById(documentId).first()?.document?.expiryDate
            reminderRepository.addReminder(documentId, offsetDays, null, expiryDate)
        }
    }

    fun delete(reminder: ReminderEntity) {
        viewModelScope.launch { reminderRepository.delete(reminder) }
    }

    fun consumeDuplicateError() {
        duplicateError.value = null
    }
}

/** "30 days before", "On expiry day" (Section 3.3 S14). */
fun reminderOffsetLabel(offsetDays: Int): String = if (offsetDays == 0) "On expiry day" else "$offsetDays days before"
