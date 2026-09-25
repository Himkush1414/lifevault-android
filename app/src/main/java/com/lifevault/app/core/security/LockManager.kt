package com.lifevault.app.core.security

import com.lifevault.app.core.domain.lock.LockoutSchedule
import com.lifevault.app.core.domain.lock.computeLockoutSchedule
import com.lifevault.app.core.domain.lock.remainingLockoutMillis
import com.lifevault.app.core.domain.lock.shouldAutoLock
import com.lifevault.app.core.security.prefs.SecurePrefs
import com.lifevault.app.core.security.prefs.SecurePrefsData
import com.lifevault.app.core.util.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/** Section 3.4's `LockState`, now backed by real PIN/biometric/auto-lock state. */
enum class LockState { LOCKED, UNLOCKED }

enum class VerifyPinResult { SUCCESS, WRONG_PIN, LOCKED_OUT }

private const val SUPPRESS_DURATION_MILLIS = 5 * 60_000L // Section 8.5: suppression expires after 5 minutes.

/**
 * Orchestrates Sections 8.3–8.5: PIN setup/verification, persisted lockout scheduling,
 * and auto-lock. The business rules themselves (lockout duration, dual-clock scheduling,
 * auto-lock timeout) live in `core.domain.lock` as pure, independently-tested functions
 * — this class is the thin, stateful Android-facing wrapper around them.
 */
@Singleton
class LockManager @Inject constructor(
    private val securePrefs: SecurePrefs,
    private val pinHasher: PinHasher,
    private val timeSource: TimeSource,
    dispatcherProvider: DispatcherProvider,
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatcherProvider.default)

    // Locked by default until the initial read of secure_prefs completes — never risk a
    // frame of unlocked content before we actually know app-lock is off (Section 3.4).
    private val _state = MutableStateFlow(LockState.LOCKED)
    val state: StateFlow<LockState> = _state.asStateFlow()

    private var backgroundedAtElapsedRealtimeMs: Long? = null
    private var suppressUntilElapsedRealtimeMs: Long = 0

    init {
        scope.launch {
            val prefs = securePrefs.snapshot()
            _state.value = if (prefs.appLockEnabled) LockState.LOCKED else LockState.UNLOCKED
        }
    }

    suspend fun isAppLockEnabled(): Boolean = securePrefs.snapshot().appLockEnabled

    /** S04 step 1/2: creates the PIN and unlocks (the user just proved they know it). */
    suspend fun setupPin(pin: CharArray) {
        val hash = pinHasher.hash(pin)
        securePrefs.update {
            it.copy(
                pinHashBase64 = securePrefs.encodeBytes(hash.hash),
                pinSaltBase64 = securePrefs.encodeBytes(hash.salt),
                pinIterations = hash.iterations,
                appLockEnabled = true,
                failedAttempts = 0,
                lockoutUntilEpochMs = null,
                lockoutSetAtElapsedRealtimeMs = null,
                lockoutUntilElapsedRealtimeMs = null,
            )
        }
        _state.value = LockState.UNLOCKED
    }

    suspend fun disableAppLock() {
        securePrefs.update { it.copy(appLockEnabled = false, biometricEnabled = false) }
        _state.value = LockState.UNLOCKED
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        securePrefs.update { it.copy(biometricEnabled = enabled) }
    }

    suspend fun setAutoLockTimeoutSeconds(seconds: Int) {
        securePrefs.update { it.copy(autoLockTimeoutSec = seconds) }
    }

    suspend fun setBlockScreenshots(blocked: Boolean) {
        securePrefs.update { it.copy(blockScreenshots = blocked) }
    }

    suspend fun lockoutRemainingMillis(): Long {
        val prefs = securePrefs.snapshot()
        return remainingLockoutMillis(
            prefs.toLockoutSchedule(),
            timeSource.epochMillis(),
            timeSource.elapsedRealtimeMillis(),
        )
    }

    /** Section 8.4: PIN failures persist across process death via [SecurePrefs]. */
    suspend fun verifyPin(pin: CharArray): VerifyPinResult {
        val prefs = securePrefs.snapshot()

        if (lockoutRemainingMillis() > 0) {
            pin.fill('0')
            return VerifyPinResult.LOCKED_OUT
        }

        val hashBase64 = prefs.pinHashBase64
        val saltBase64 = prefs.pinSaltBase64
        val iterations = prefs.pinIterations
        if (hashBase64 == null || saltBase64 == null || iterations == null) {
            pin.fill('0')
            return VerifyPinResult.WRONG_PIN
        }

        val hash = PinHash(securePrefs.decodeBytes(hashBase64), securePrefs.decodeBytes(saltBase64), iterations)
        val correct = pinHasher.verify(pin, hash)

        return if (correct) {
            onCorrectPin()
            VerifyPinResult.SUCCESS
        } else {
            onWrongPin(prefs.failedAttempts + 1)
            VerifyPinResult.WRONG_PIN
        }
    }

    /** Called after `BiometricPrompt`'s callback decrypts the stored unlock token successfully. */
    suspend fun unlockViaBiometric() {
        onCorrectPin()
    }

    private suspend fun onCorrectPin() {
        securePrefs.update {
            it.copy(
                failedAttempts = 0,
                lockoutUntilEpochMs = null,
                lockoutSetAtElapsedRealtimeMs = null,
                lockoutUntilElapsedRealtimeMs = null,
            )
        }
        _state.value = LockState.UNLOCKED
    }

    private suspend fun onWrongPin(newFailureCount: Int) {
        val schedule = computeLockoutSchedule(
            newFailureCount,
            timeSource.epochMillis(),
            timeSource.elapsedRealtimeMillis(),
        )
        securePrefs.update {
            it.copy(
                failedAttempts = newFailureCount,
                lockoutUntilEpochMs = schedule?.lockoutUntilEpochMs,
                lockoutSetAtElapsedRealtimeMs = schedule?.setAtElapsedRealtimeMs,
                lockoutUntilElapsedRealtimeMs = schedule?.lockoutUntilElapsedRealtimeMs,
            )
        }
    }

    fun lock() {
        _state.value = LockState.LOCKED
    }

    /** Section 8.5: don't lock when returning from our own external flows (scanner, SAF, ...). */
    fun suppressNextBackgroundLock() {
        suppressUntilElapsedRealtimeMs = timeSource.elapsedRealtimeMillis() + SUPPRESS_DURATION_MILLIS
    }

    fun onAppBackgrounded() {
        backgroundedAtElapsedRealtimeMs = timeSource.elapsedRealtimeMillis()
    }

    fun onAppForegrounded() {
        val backgroundedAt = backgroundedAtElapsedRealtimeMs
        backgroundedAtElapsedRealtimeMs = null
        if (backgroundedAt == null) return
        val now = timeSource.elapsedRealtimeMillis()
        if (now < suppressUntilElapsedRealtimeMs) return

        scope.launch {
            val prefs = securePrefs.snapshot()
            if (prefs.appLockEnabled && shouldAutoLock(backgroundedAt, now, prefs.autoLockTimeoutSec)) {
                _state.value = LockState.LOCKED
            }
        }
    }

    /** Section 8.5: screen off locks immediately, regardless of the configured timeout. */
    fun onScreenOff() {
        scope.launch {
            if (securePrefs.snapshot().appLockEnabled) {
                _state.value = LockState.LOCKED
            }
        }
    }
}

private fun SecurePrefsData.toLockoutSchedule(): LockoutSchedule? {
    val until = lockoutUntilEpochMs ?: return null
    val setAt = lockoutSetAtElapsedRealtimeMs ?: return null
    val untilElapsed = lockoutUntilElapsedRealtimeMs ?: return null
    return LockoutSchedule(until, setAt, untilElapsed)
}
