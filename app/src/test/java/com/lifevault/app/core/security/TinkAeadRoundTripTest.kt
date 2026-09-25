package com.lifevault.app.core.security

import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.security.GeneralSecurityException

/**
 * Exercises the exact Tink primitive [TinkKeysetStore] uses for the "prefs" keyset
 * (AES256_GCM), without going through Android Keystore — that wrapping layer needs a
 * real device and is covered by the (unexecuted, see BUILD_LOG) instrumented tests.
 * This still gives real, running coverage of the crypto behaviour itself.
 */
class TinkAeadRoundTripTest {

    private lateinit var aead: Aead

    @Before
    fun setUp() {
        AeadConfig.register()
        val handle = KeysetHandle.generateNew(KeyTemplates.get("AES256_GCM"))
        aead = handle.getPrimitive(com.google.crypto.tink.RegistryConfiguration.get(), Aead::class.java)
    }

    @Test
    fun `encrypt then decrypt returns the original plaintext`() {
        val plaintext = "the quick brown fox".toByteArray()
        val ciphertext = aead.encrypt(plaintext, null)
        assertArrayEquals(plaintext, aead.decrypt(ciphertext, null))
    }

    @Test
    fun `associated data must match to decrypt`() {
        val plaintext = "dbPassphrase".toByteArray()
        val aad = "field:dbPassphrase".toByteArray()
        val ciphertext = aead.encrypt(plaintext, aad)

        assertArrayEquals(plaintext, aead.decrypt(ciphertext, aad))
        assertThrows(GeneralSecurityException::class.java) {
            aead.decrypt(ciphertext, "field:pinHash".toByteArray())
        }
    }

    @Test
    fun `tampering with a single ciphertext byte is rejected`() {
        val ciphertext = aead.encrypt("secret".toByteArray(), null)
        val tampered = ciphertext.copyOf()
        tampered[tampered.size - 1] = (tampered[tampered.size - 1].toInt() xor 0x01).toByte()

        assertThrows(GeneralSecurityException::class.java) {
            aead.decrypt(tampered, null)
        }
    }

    @Test
    fun `truncated ciphertext is rejected`() {
        val ciphertext = aead.encrypt("secret".toByteArray(), null)
        val truncated = ciphertext.copyOf(ciphertext.size - 1)

        assertThrows(GeneralSecurityException::class.java) {
            aead.decrypt(truncated, null)
        }
    }
}
