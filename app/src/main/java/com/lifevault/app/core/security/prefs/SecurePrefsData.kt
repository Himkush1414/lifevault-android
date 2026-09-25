package com.lifevault.app.core.security.prefs

import kotlinx.serialization.Serializable

/**
 * The fields Section 5.3 lists as living outside Room, in encrypted DataStore
 * (`secure_prefs`) — settings that must be readable before the database key is
 * available. Only [dbPassphraseBase64] exists yet (Step 4); Step 8 adds the PIN/lock
 * fields, Step 10 adds entitlement, Step 9 adds the wrapped auto-backup key.
 */
@Serializable
data class SecurePrefsData(
    val dbPassphraseBase64: String? = null,
)
