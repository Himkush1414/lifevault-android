package com.lifevault.app.feature.lock

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifevault.app.core.domain.lock.PIN_LENGTH
import com.lifevault.app.core.security.BiometricAuthenticator
import com.lifevault.app.core.security.BiometricOutcome
import com.lifevault.app.core.security.LockManager
import com.lifevault.app.core.security.VerifyPinResult
import com.lifevault.app.core.security.prefs.SecurePrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LockScreenUiState(
    val digitsEntered: Int = 0,
    val errorMessage: String? = null,
    val lockoutRemainingMillis: Long = 0,
    val biometricEnabled: Boolean = false,
    /** Section 8.4: auto-launch on appear, but not again after an explicit cancel this session. */
    val biometricAutoLaunchArmed: Boolean = true,
)

/** S05 (Section 3.3): PIN entry, biometric unlock, lockout countdown. */
@HiltViewModel
class LockScreenViewModel @Inject constructor(
    private val lockManager: LockManager,
    private val securePrefs: SecurePrefs,
    private val biometricAuthenticator: BiometricAuthenticator,
) : ViewModel() {

    private val pinBuffer = StringBuilder()

    private val _state = MutableStateFlow(LockScreenUiState())
    val state: StateFlow<LockScreenUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(biometricEnabled = securePrefs.snapshot().biometricEnabled) }
            refreshLockout()
        }
    }

    fun onDigit(digit: Int) {
        if (_state.value.lockoutRemainingMillis > 0 || pinBuffer.length >= PIN_LENGTH) return
        pinBuffer.append(digit)
        _state.update { it.copy(digitsEntered = pinBuffer.length, errorMessage = null) }
        if (pinBuffer.length == PIN_LENGTH) submitPin()
    }

    fun onBackspace() {
        if (pinBuffer.isNotEmpty()) pinBuffer.deleteCharAt(pinBuffer.length - 1)
        _state.update { it.copy(digitsEntered = pinBuffer.length) }
    }

    fun onBiometricPromptDismissedByUser() {
        _state.update { it.copy(biometricAutoLaunchArmed = false) }
    }

    fun attemptBiometric(activity: FragmentActivity) {
        biometricAuthenticator.authenticate(
            activity = activity,
            title = "Unlock LifeVault",
            negativeButtonText = "Use PIN",
        ) { outcome ->
            when (outcome) {
                BiometricOutcome.Success -> viewModelScope.launch { lockManager.unlockViaBiometric() }
                BiometricOutcome.UserCancelled -> onBiometricPromptDismissedByUser()
                is BiometricOutcome.SystemLockout -> {
                    onBiometricPromptDismissedByUser()
                    _state.update {
                        it.copy(
                            errorMessage = if (outcome.permanent) {
                                "Biometrics unavailable until you unlock with your device screen lock."
                            } else {
                                "Too many attempts. Use your PIN."
                            },
                        )
                    }
                }
                BiometricOutcome.KeyInvalidated -> {
                    onBiometricPromptDismissedByUser()
                    viewModelScope.launch { lockManager.setBiometricEnabled(false) }
                    _state.update {
                        it.copy(
                            biometricEnabled = false,
                            errorMessage = "Biometrics changed on this device. Re-enable in Settings.",
                        )
                    }
                }
                BiometricOutcome.HardwareUnavailableOrNoneEnrolled -> onBiometricPromptDismissedByUser()
                // System prompt shows its own "Not recognised"; doesn't count toward PIN lockout.
                BiometricOutcome.NotRecognized -> Unit
                is BiometricOutcome.OtherError -> onBiometricPromptDismissedByUser()
            }
        }
    }

    private fun submitPin() {
        viewModelScope.launch {
            val pinChars = pinBuffer.toString().toCharArray()
            pinBuffer.clear()
            when (lockManager.verifyPin(pinChars)) {
                VerifyPinResult.SUCCESS -> _state.update { it.copy(digitsEntered = 0, errorMessage = null) }
                VerifyPinResult.WRONG_PIN -> {
                    _state.update { it.copy(digitsEntered = 0, errorMessage = "Wrong PIN") }
                    refreshLockout()
                }
                VerifyPinResult.LOCKED_OUT -> refreshLockout()
            }
        }
    }

    suspend fun refreshLockout() {
        _state.update { it.copy(lockoutRemainingMillis = lockManager.lockoutRemainingMillis()) }
    }
}
