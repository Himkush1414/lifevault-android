package com.lifevault.app.core.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import java.security.KeyStore
import java.util.UUID

/**
 * Requires a real Android Keystore (TEE/StrongBox) — cannot run as a local JVM unit
 * test. Not executed in this build environment (no emulator/device available; see
 * BUILD_LOG.md Step 4). Written to run via `./gradlew connectedDebugAndroidTest`.
 */
@RunWith(AndroidJUnit4::class)
class KeyManagerInstrumentedTest {

    private val keyManager = KeyManager()

    private fun uniqueAlias() = "test_key_${UUID.randomUUID()}"

    @Test
    fun keyCreationIsIdempotent() {
        val alias = uniqueAlias()
        val first = keyManager.getOrCreateAesGcmKey(alias)
        val second = keyManager.getOrCreateAesGcmKey(alias)

        // Same underlying Keystore entry — round-tripping through one must be
        // decryptable by "the other" (they're the same key).
        val plaintext = "idempotency check".toByteArray()
        val ciphertext = keyManager.encrypt(first, plaintext)
        assertArrayEquals(plaintext, keyManager.decrypt(second, ciphertext))

        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        assertArrayEquals(
            "expected exactly one Keystore entry for $alias",
            arrayOf(alias),
            keyStore.aliases().toList().filter { it == alias }.toTypedArray(),
        )
    }

    @Test
    fun encryptThenDecryptRoundTrips() {
        val key = keyManager.getOrCreateAesGcmKey(uniqueAlias())
        val plaintext = "the quick brown fox jumps over the lazy dog".toByteArray()

        val ciphertext = keyManager.encrypt(key, plaintext)
        val decrypted = keyManager.decrypt(key, ciphertext)

        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun associatedDataMustMatchToDecrypt() {
        val key = keyManager.getOrCreateAesGcmKey(uniqueAlias())
        val ciphertext = keyManager.encrypt(key, "payload".toByteArray(), "context-a".toByteArray())

        assertThrows(TamperedDataException::class.java) {
            keyManager.decrypt(key, ciphertext, "context-b".toByteArray())
        }
    }

    @Test
    fun tamperedCiphertextIsRejected() {
        val key = keyManager.getOrCreateAesGcmKey(uniqueAlias())
        val ciphertext = keyManager.encrypt(key, "payload".toByteArray())
        val tampered = ciphertext.copyOf()
        tampered[tampered.size - 1] = (tampered[tampered.size - 1].toInt() xor 0x01).toByte()

        assertThrows(TamperedDataException::class.java) {
            keyManager.decrypt(key, tampered)
        }
    }

    @Test
    fun differentKeysProduceIncompatibleCiphertext() {
        val keyA = keyManager.getOrCreateAesGcmKey(uniqueAlias())
        val keyB = keyManager.getOrCreateAesGcmKey(uniqueAlias())
        val ciphertext = keyManager.encrypt(keyA, "payload".toByteArray())

        assertThrows(TamperedDataException::class.java) {
            keyManager.decrypt(keyB, ciphertext)
        }
    }
}
