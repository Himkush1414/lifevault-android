package com.lifevault.app.feature.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifevault.app.core.database.entity.DocumentEntity
import com.lifevault.app.core.database.relation.DocumentWithCategory
import com.lifevault.app.core.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrashUiState(
    val documents: List<DocumentWithCategory> = emptyList(),
    val selectedIds: Set<String> = emptySet(),
) {
    val isSelectionMode: Boolean get() = selectedIds.isNotEmpty()
}

/** S23 (Section 3.3): restore or permanently delete trashed documents. */
@HiltViewModel
class TrashViewModel @Inject constructor(private val documentRepository: DocumentRepository) : ViewModel() {

    private val selectedIds = MutableStateFlow<Set<String>>(emptySet())

    val state: StateFlow<TrashUiState> = combine(
        documentRepository.observeTrashed(),
        selectedIds,
    ) { documents, selected ->
        TrashUiState(documents = documents, selectedIds = selected.intersect(documents.map { it.document.id }.toSet()))
    }.stateIn(viewModelScope, SharingStarted.Eagerly, TrashUiState())

    fun restore(documentId: String) {
        viewModelScope.launch { documentRepository.restoreFromTrash(documentId) }
    }

    fun toggleSelection(documentId: String) {
        selectedIds.value = if (documentId in selectedIds.value) {
            selectedIds.value - documentId
        } else {
            selectedIds.value + documentId
        }
    }

    fun clearSelection() {
        selectedIds.value = emptySet()
    }

    fun permanentlyDeleteSelected() {
        val toDelete = state.value.documents.filter { it.document.id in selectedIds.value }.map { it.document }
        viewModelScope.launch {
            toDelete.forEach { documentRepository.permanentlyDelete(it) }
            selectedIds.value = emptySet()
        }
    }

    fun permanentlyDelete(document: DocumentEntity) {
        viewModelScope.launch { documentRepository.permanentlyDelete(document) }
    }
}
