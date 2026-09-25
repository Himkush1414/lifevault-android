package com.lifevault.app.core.security

/**
 * The Keystore key for this alias is gone (Section 8.2 "Keystore loss"): OEM bugs, the
 * user removing their screen lock, or a device-image restore. Wraps
 * `KeyPermanentlyInvalidatedException`. Callers (Step 8+) show the recovery screen:
 * "Restore from a backup file" or "Start fresh".
 */
class KeystoreLostException(message: String, cause: Throwable) : Exception(message, cause)

/** Ciphertext failed authentication — tampered, corrupted, or decrypted with the wrong key/AAD. */
class TamperedDataException(message: String, cause: Throwable) : Exception(message, cause)
