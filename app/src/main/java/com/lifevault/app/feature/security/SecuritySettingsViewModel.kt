package com.lifevault.app.feature.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifevault.app.core.security.LockManager
import com.lifevault.app.core.security.prefs.SecurePrefs
import com.lifevault.app.core.security.prefs.SecurePrefsData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs S19 (Security & app lock) and — via just [securityState] — the `FLAG_SECURE`
 * side effect in `MainActivity` (Section 8.6: default ON, user-toggleable).
 */
@HiltViewModel
class SecuritySettingsViewModel @Inject constructor(
    private val lockManager: LockManager,
    securePrefs: SecurePrefs,
) : ViewModel() {

    val securityState: StateFlow<SecurePrefsData> = securePrefs.data
        .stateIn(viewModelScope, SharingStarted.Eagerly, SecurePrefsData())

    fun disableAppLock() {
        viewModelScope.launch { lockManager.disableAppLock() }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch { lockManager.setBiometricEnabled(enabled) }
    }

    fun setAutoLockTimeoutSeconds(seconds: Int) {
        viewModelScope.launch { lockManager.setAutoLockTimeoutSeconds(seconds) }
    }

    fun setBlockScreenshots(blocked: Boolean) {
        viewModelScope.launch { lockManager.setBlockScreenshots(blocked) }
    }
}
