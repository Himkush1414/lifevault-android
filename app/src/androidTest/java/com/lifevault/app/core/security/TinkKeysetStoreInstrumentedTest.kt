package com.lifevault.app.core.security

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream
import java.util.UUID

/**
 * Requires a real Android Keystore — not executed in this build environment (see
 * BUILD_LOG.md Step 4). Written to run via `./gradlew connectedDebugAndroidTest`.
 */
@RunWith(AndroidJUnit4::class)
class TinkKeysetStoreInstrumentedTest {

    private fun newStore(): TinkKeysetStore {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        return TinkKeysetStore(context, KeyManager())
    }

    @Test
    fun prefsAeadRoundTrips() {
        val store = newStore()
        val plaintext = "dbPassphrase-${UUID.randomUUID()}".toByteArray()
        val ciphertext = store.prefsAead().encrypt(plaintext, null)
        assertArrayEquals(plaintext, store.prefsAead().decrypt(ciphertext, null))
    }

    @Test
    fun keysetPersistsAcrossStoreInstances() {
        // Simulates a process restart: a fresh TinkKeysetStore pointed at the same
        // filesDir must be able to decrypt what the first instance encrypted, proving
        // the wrapped keyset (not just the in-memory handle) round-trips through disk.
        val plaintext = "persisted-across-restart".toByteArray()
        val ciphertext = newStore().prefsAead().encrypt(plaintext, null)

        val secondInstance = newStore()
        assertArrayEquals(plaintext, secondInstance.prefsAead().decrypt(ciphertext, null))
    }

    @Test
    fun filesStreamingAeadRoundTrips() {
        val store = newStore()
        val plaintext = ByteArray(50_000) { it.toByte() }
        val aad = "attachment:test".toByteArray()

        val out = ByteArrayOutputStream()
        store.filesStreamingAead().newEncryptingStream(out, aad).use { it.write(plaintext) }

        val decrypted = store.filesStreamingAead()
            .newDecryptingStream(out.toByteArray().inputStream(), aad)
            .use { it.readBytes() }

        assertArrayEquals(plaintext, decrypted)
    }
}
