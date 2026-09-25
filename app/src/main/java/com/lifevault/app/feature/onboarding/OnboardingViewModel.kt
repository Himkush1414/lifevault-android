package com.lifevault.app.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifevault.app.core.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(private val settingsRepository: SettingsRepository) : ViewModel() {
    fun markOnboardingCompleted() {
        viewModelScope.launch { settingsRepository.setOnboardingCompleted(true) }
    }
}
