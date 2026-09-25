package com.lifevault.app.feature.lock

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifevault.app.core.domain.lock.PIN_LENGTH
import com.lifevault.app.core.domain.lock.PinValidationResult
import com.lifevault.app.core.domain.lock.validatePin
import com.lifevault.app.core.security.BiometricAuthenticator
import com.lifevault.app.core.security.BiometricOutcome
import com.lifevault.app.core.security.LockManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LockSetupStep { CREATE_PIN, CONFIRM_PIN, BIOMETRIC_OFFER, DONE }

data class LockSetupUiState(
    val step: LockSetupStep = LockSetupStep.CREATE_PIN,
    val digitsEntered: Int = 0,
    val errorMessage: String? = null,
    val shake: Boolean = false,
)

/** S04 (Section 3.3): create PIN, confirm PIN, offer biometrics. */
@HiltViewModel
class LockSetupViewModel @Inject constructor(
    private val lockManager: LockManager,
    private val biometricAuthenticator: BiometricAuthenticator,
) : ViewModel() {

    private var firstPin: String? = null
    private val pinBuffer = StringBuilder()

    private val _state = MutableStateFlow(LockSetupUiState())
    val state: StateFlow<LockSetupUiState> = _state.asStateFlow()

    fun onDigit(digit: Int) {
        if (pinBuffer.length >= PIN_LENGTH) return
        pinBuffer.append(digit)
        _state.update { it.copy(digitsEntered = pinBuffer.length, shake = false) }
        if (pinBuffer.length == PIN_LENGTH) onPinComplete()
    }

    fun onBackspace() {
        if (pinBuffer.isNotEmpty()) pinBuffer.deleteCharAt(pinBuffer.length - 1)
        _state.update { it.copy(digitsEntered = pinBuffer.length) }
    }

    fun consumeShake() {
        _state.update { it.copy(shake = false) }
    }

    fun checkBiometricAvailability(activity: FragmentActivity) {
        if (!biometricAuthenticator.canAuthenticate(activity)) skipBiometrics()
    }

    fun attemptEnableBiometrics(activity: FragmentActivity) {
        biometricAuthenticator.authenticate(
            activity = activity,
            title = "Confirm biometric unlock",
            negativeButtonText = "Cancel",
        ) { outcome ->
            if (outcome is BiometricOutcome.Success) enableBiometrics()
        }
    }

    private fun enableBiometrics() {
        viewModelScope.launch {
            lockManager.setBiometricEnabled(true)
            _state.update { it.copy(step = LockSetupStep.DONE) }
        }
    }

    fun skipBiometrics() {
        _state.update { it.copy(step = LockSetupStep.DONE) }
    }

    private fun onPinComplete() {
        val entered = pinBuffer.toString()
        pinBuffer.clear()

        when (_state.value.step) {
            LockSetupStep.CREATE_PIN -> handleCreatePin(entered)
            LockSetupStep.CONFIRM_PIN -> handleConfirmPin(entered)
            else -> Unit
        }
    }

    private fun handleCreatePin(entered: String) {
        val validation = validatePin(entered)
        if (validation != PinValidationResult.Valid) {
            _state.update { it.copy(digitsEntered = 0, errorMessage = validationMessage(validation), shake = true) }
            return
        }
        firstPin = entered
        _state.update {
            it.copy(step = LockSetupStep.CONFIRM_PIN, digitsEntered = 0, errorMessage = null)
        }
    }

    private fun handleConfirmPin(entered: String) {
        if (entered == firstPin) {
            viewModelScope.launch {
                lockManager.setupPin(entered.toCharArray())
                _state.update { it.copy(step = LockSetupStep.BIOMETRIC_OFFER, digitsEntered = 0, errorMessage = null) }
            }
        } else {
            firstPin = null
            _state.update {
                it.copy(
                    step = LockSetupStep.CREATE_PIN,
                    digitsEntered = 0,
                    errorMessage = "PINs didn't match",
                    shake = true,
                )
            }
        }
    }

    private fun validationMessage(result: PinValidationResult): String? = when (result) {
        PinValidationResult.Valid -> null
        PinValidationResult.WrongLength, PinValidationResult.NotAllDigits -> "Enter a 6-digit PIN"
        PinValidationResult.AllSameDigit -> "Choose a PIN that isn't all the same digit"
        PinValidationResult.SequentialDigits -> "Choose a PIN that isn't a straight sequence"
        PinValidationResult.CommonlyUsed -> "That PIN is too common — choose another"
    }
}
