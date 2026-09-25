package com.lifevault.app.core.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifevault.app.core.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * S01 (Section 3.3): held on screen (via `setKeepOnScreenCondition`) until this resolves.
 * The Onboarding-vs-Home decision; the Home-vs-Lock decision is [LockManager]'s job via
 * [LockGate], which already defaults safely to Locked.
 */
@HiltViewModel
class SplashViewModel @Inject constructor(settingsRepository: SettingsRepository) : ViewModel() {

    private val _startDestination = MutableStateFlow<Any?>(null)
    val startDestination: StateFlow<Any?> = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            val onboardingCompleted = settingsRepository.observe().first().onboardingCompleted
            _startDestination.value = if (onboardingCompleted) Routes.Home else Routes.Onboarding
        }
    }
}
