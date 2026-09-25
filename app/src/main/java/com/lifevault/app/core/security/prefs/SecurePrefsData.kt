package com.lifevault.app.core.security.prefs

import kotlinx.serialization.Serializable

/**
 * The fields Section 5.3 lists as living outside Room, in encrypted DataStore
 * (`secure_prefs`) — settings that must be readable before the database key is
 * available. Step 10 adds entitlement, Step 9 adds the wrapped auto-backup key.
 */
@Serializable
data class SecurePrefsData(
    val dbPassphraseBase64: String? = null,

    // Section 8.3/8.4 — PIN and lockout state.
    val pinHashBase64: String? = null,
    val pinSaltBase64: String? = null,
    val pinIterations: Int? = null,
    val appLockEnabled: Boolean = false,
    val biometricEnabled: Boolean = false,
    val autoLockTimeoutSec: Int = 30,
    val blockScreenshots: Boolean = true,
    val failedAttempts: Int = 0,
    val lockoutUntilEpochMs: Long? = null,
    val lockoutSetAtElapsedRealtimeMs: Long? = null,
    val lockoutUntilElapsedRealtimeMs: Long? = null,
)
