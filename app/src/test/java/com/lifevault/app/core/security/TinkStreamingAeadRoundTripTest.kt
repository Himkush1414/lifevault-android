package com.lifevault.app.core.security

import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.StreamingAead
import com.google.crypto.tink.streamingaead.StreamingAeadConfig
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * Exercises the exact Tink primitive [TinkKeysetStore] uses for the "files" keyset
 * (AES256_GCM_HKDF_1MB), the same template the real vault file store (Step 10) will use.
 */
class TinkStreamingAeadRoundTripTest {

    private lateinit var streamingAead: StreamingAead

    @Before
    fun setUp() {
        StreamingAeadConfig.register()
        val handle = KeysetHandle.generateNew(KeyTemplates.get("AES256_GCM_HKDF_1MB"))
        streamingAead = handle.getPrimitive(RegistryConfiguration.get(), StreamingAead::class.java)
    }

    private fun encrypt(plaintext: ByteArray, aad: ByteArray): ByteArray {
        val out = ByteArrayOutputStream()
        streamingAead.newEncryptingStream(out, aad).use { it.write(plaintext) }
        return out.toByteArray()
    }

    private fun decrypt(ciphertext: ByteArray, aad: ByteArray): ByteArray =
        streamingAead.newDecryptingStream(ciphertext.inputStream(), aad).use { it.readBytes() }

    @Test
    fun `encrypt then decrypt a large file returns the original bytes`() {
        val aad = "attachment:test-id".toByteArray()
        // Larger than the 1MB segment size so more than one segment is exercised.
        val plaintext = ByteArray(2_500_000) { (it % 251).toByte() }

        val ciphertext = encrypt(plaintext, aad)
        assertArrayEquals(plaintext, decrypt(ciphertext, aad))
    }

    @Test
    fun `wrong associated data is rejected`() {
        val ciphertext = encrypt("image bytes".toByteArray(), "attachment:a".toByteArray())
        // InputStream#read() can only declare IOException, so Tink wraps the underlying
        // GeneralSecurityException in one when a decrypting stream is actually read.
        assertThrows(IOException::class.java) {
            decrypt(ciphertext, "attachment:b".toByteArray())
        }
    }

    @Test
    fun `tampering with the ciphertext is rejected`() {
        val aad = "attachment:c".toByteArray()
        val ciphertext = encrypt("image bytes".toByteArray(), aad)
        val tampered = ciphertext.copyOf()
        tampered[tampered.size - 1] = (tampered[tampered.size - 1].toInt() xor 0x01).toByte()

        assertThrows(IOException::class.java) {
            decrypt(tampered, aad)
        }
    }
}
