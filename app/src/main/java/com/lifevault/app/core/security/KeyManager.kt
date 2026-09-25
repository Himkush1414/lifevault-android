package com.lifevault.app.core.security

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import java.security.KeyStore
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

private const val ANDROID_KEYSTORE = "AndroidKeyStore"
private const val TRANSFORMATION = "AES/GCM/NoPadding"
private const val GCM_IV_LENGTH_BYTES = 12
private const val GCM_TAG_LENGTH_BITS = 128

/**
 * Android Keystore key lifecycle (Section 8.2): non-exportable AES-256-GCM keys,
 * generated on first use with a StrongBox attempt that falls back to the TEE.
 * `KeyManager` never returns raw key material — only a [SecretKey] handle backed by
 * the keystore, and an [encrypt]/[decrypt] pair operating on opaque blobs.
 */
@Singleton
class KeyManager @Inject constructor() {

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    /**
     * Returns the existing key for [alias], or generates a new one.
     *
     * @param requireUserAuthentication true only for the biometric-unlock key
     * (Section 8.2 `lifevault_bio_unlock`) — every other key must stay usable while the
     * app is locked, so the reminder worker can read expiry dates (Section 8.1's
     * documented limitation).
     * @param invalidatedByBiometricEnrollment Section 8.2: only set for the bio-unlock key.
     */
    fun getOrCreateAesGcmKey(
        alias: String,
        requireUserAuthentication: Boolean = false,
        invalidatedByBiometricEnrollment: Boolean = false,
    ): SecretKey {
        (keyStore.getKey(alias, null) as? SecretKey)?.let { return it }
        return generateAesGcmKey(alias, requireUserAuthentication, invalidatedByBiometricEnrollment)
    }

    private fun generateAesGcmKey(
        alias: String,
        requireUserAuthentication: Boolean,
        invalidatedByBiometricEnrollment: Boolean,
    ): SecretKey {
        fun buildSpec(strongBoxBacked: Boolean): KeyGenParameterSpec {
            val builder = KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)

            if (requireUserAuthentication) {
                builder.setUserAuthenticationRequired(true)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    builder.setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG)
                } else {
                    @Suppress("DEPRECATION")
                    builder.setUserAuthenticationValidityDurationSeconds(-1)
                }
                builder.setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment)
            }
            if (strongBoxBacked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                builder.setIsStrongBoxBacked(true)
            }
            return builder.build()
        }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        // StrongBoxUnavailableException itself is API 28+, so it can only be thrown (and
        // must only be referenced) on API 28+ — buildSpec() never requests StrongBox below
        // that anyway, so there's nothing to catch on API 26/27.
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                keyGenerator.init(buildSpec(strongBoxBacked = true))
                keyGenerator.generateKey()
            } catch (_: StrongBoxUnavailableException) {
                keyGenerator.init(buildSpec(strongBoxBacked = false))
                keyGenerator.generateKey()
            }
        } else {
            keyGenerator.init(buildSpec(strongBoxBacked = false))
            keyGenerator.generateKey()
        }
    }

    /**
     * Encrypts [plaintext] with [key], returning `IV (12 bytes) || ciphertext+tag`.
     * [associatedData] binds the ciphertext to its purpose (e.g. the field name) so a
     * blob can't be silently swapped for another field's ciphertext.
     */
    fun encrypt(key: SecretKey, plaintext: ByteArray, associatedData: ByteArray? = null): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        associatedData?.let { cipher.updateAAD(it) }
        val ciphertext = cipher.doFinal(plaintext)
        return cipher.iv + ciphertext
    }

    /**
     * Inverse of [encrypt].
     *
     * @throws TamperedDataException if the tag doesn't verify (corrupted, tampered, or
     * wrong [associatedData]).
     * @throws KeystoreLostException if this device's copy of [key] is gone (Section 8.2).
     */
    fun decrypt(key: SecretKey, blob: ByteArray, associatedData: ByteArray? = null): ByteArray {
        require(blob.size > GCM_IV_LENGTH_BYTES) { "Blob too short to contain a GCM IV" }
        val iv = blob.copyOfRange(0, GCM_IV_LENGTH_BYTES)
        val ciphertext = blob.copyOfRange(GCM_IV_LENGTH_BYTES, blob.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        try {
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
            associatedData?.let { cipher.updateAAD(it) }
            return cipher.doFinal(ciphertext)
        } catch (e: KeyPermanentlyInvalidatedException) {
            throw KeystoreLostException("Keystore key was permanently invalidated", e)
        } catch (e: AEADBadTagException) {
            throw TamperedDataException("GCM tag verification failed — data is corrupted or tampered", e)
        }
    }
}
