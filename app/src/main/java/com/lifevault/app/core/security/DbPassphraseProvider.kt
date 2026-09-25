package com.lifevault.app.core.security

import com.lifevault.app.core.security.prefs.SecurePrefs
import java.security.SecureRandom
import java.util.Arrays
import javax.inject.Inject
import javax.inject.Singleton

private const val PASSPHRASE_LENGTH_BYTES = 32

/**
 * The SQLCipher database passphrase (Section 8.2): 32 random bytes, generated once and
 * persisted encrypted in [SecurePrefs]. Step 5 passes the returned bytes to
 * `SupportOpenHelperFactory` and must zero its own copy immediately after the database
 * opens — this class cannot zero the caller's copy for it, only its own.
 */
@Singleton
class DbPassphraseProvider @Inject constructor(private val securePrefs: SecurePrefs) {

    suspend fun getOrCreatePassphrase(): ByteArray {
        securePrefs.getDbPassphrase()?.let { return it }

        val passphrase = ByteArray(PASSPHRASE_LENGTH_BYTES)
        SecureRandom().nextBytes(passphrase)
        try {
            securePrefs.setDbPassphrase(passphrase)
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            // Wipe on any failure, whatever it is — this is cleanup, not error handling.
            Arrays.fill(passphrase, 0)
            throw e
        }
        return passphrase
    }
}
