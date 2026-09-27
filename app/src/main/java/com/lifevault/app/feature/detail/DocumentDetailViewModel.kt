package com.lifevault.app.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.lifevault.app.core.database.relation.DocumentWithDetails
import com.lifevault.app.core.domain.reminder.rollForwardExpiry
import com.lifevault.app.core.domain.reminder.rolledForwardIssueDate
import com.lifevault.app.core.domain.status.DocumentStatus
import com.lifevault.app.core.domain.status.documentStatusOf
import com.lifevault.app.core.navigation.Routes
import com.lifevault.app.core.repository.DocumentRepository
import com.lifevault.app.core.repository.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

data class DocumentDetailUiState(
    val documentWithDetails: DocumentWithDetails? = null,
    val today: LocalDate = LocalDate.now(),
    val movedToTrash: Boolean = false,
) {
    val status: DocumentStatus
        get() = documentStatusOf(documentWithDetails?.document?.expiryDate, today)
}

/** S09 (Section 3.3): document detail. */
@HiltViewModel
class DocumentDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val documentRepository: DocumentRepository,
    private val reminderRepository: ReminderRepository,
    private val clock: Clock,
) : ViewModel() {

    private val documentId = savedStateHandle.toRoute<Routes.DocumentDetail>().documentId
    private val movedToTrash = MutableStateFlow(false)

    val state: StateFlow<DocumentDetailUiState> = combine(
        documentRepository.observeById(documentId),
        movedToTrash,
    ) { details, trashed ->
        DocumentDetailUiState(documentWithDetails = details, today = LocalDate.now(clock), movedToTrash = trashed)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, DocumentDetailUiState())

    fun moveToTrash() {
        viewModelScope.launch {
            documentRepository.moveToTrash(documentId)
            movedToTrash.value = true
        }
    }

    /** S09 bottom action bar: rolls the expiry (and issue date, if tracked) forward and re-arms reminders. */
    fun markAsRenewed(newExpiryDate: LocalDate) {
        viewModelScope.launch {
            val document = state.value.documentWithDetails?.document ?: return@launch
            val oldExpiry = document.expiryDate
            val newIssueDate = if (document.issueDate != null && oldExpiry != null) {
                rolledForwardIssueDate(oldExpiry)
            } else {
                document.issueDate
            }
            documentRepository.update(
                document.copy(
                    expiryDate = newExpiryDate,
                    issueDate = newIssueDate,
                    lastOverdueNudgeAt = null,
                ),
            )
            reminderRepository.rearmForNewExpiry(documentId, newExpiryDate)
        }
    }

    /** Pre-filled suggestion for the "Mark as renewed" dialog when a recurrence is set. */
    fun suggestedRenewalDate(): LocalDate? {
        val document = state.value.documentWithDetails?.document ?: return null
        val currentExpiry = document.expiryDate ?: return null
        return rollForwardExpiry(currentExpiry, document.recurrence, document.recurrenceIntervalYears)
    }
}
