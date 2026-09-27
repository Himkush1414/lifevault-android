package com.lifevault.app.feature.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifevault.app.core.domain.feature.FeatureGate
import com.lifevault.app.core.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class AddSourceUiState(val documentCount: Int = 0, val isPro: Boolean = false) {
    // TODO(step 21 - billing): isPro from the real entitlement instead of always false.
    val limitReached: Boolean get() = !FeatureGate.canAddDocument(documentCount, isPro)
}

/** S11 (Section 3.3): gates the add-source sheet on the Free document limit. */
@HiltViewModel
class AddSourceViewModel @Inject constructor(documentRepository: DocumentRepository) : ViewModel() {
    val state: StateFlow<AddSourceUiState> = documentRepository.observeActiveCount()
        .map { AddSourceUiState(documentCount = it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AddSourceUiState())
}
