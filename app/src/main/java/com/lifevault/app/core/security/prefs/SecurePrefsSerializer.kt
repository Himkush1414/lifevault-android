package com.lifevault.app.core.security.prefs

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.lifevault.app.core.security.TinkKeysetStore
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import java.security.GeneralSecurityException

private val ASSOCIATED_DATA = "lifevault:secure_prefs".toByteArray()

/**
 * Encrypts the whole `secure_prefs` DataStore payload with the Tink "prefs" [Aead][com.google.crypto.tink.Aead]
 * keyset (Section 8.2) before it ever reaches disk. An empty/missing file decodes to
 * [SecurePrefsData]'s defaults (first launch); a present-but-unreadable file is a real
 * corruption/tamper signal and is surfaced as [CorruptionException] so DataStore's own
 * recovery path can run instead of silently discarding data.
 */
class SecurePrefsSerializer(private val tinkKeysetStore: TinkKeysetStore) : Serializer<SecurePrefsData> {

    override val defaultValue: SecurePrefsData = SecurePrefsData()

    override suspend fun readFrom(input: InputStream): SecurePrefsData {
        val encrypted = input.readBytes()
        if (encrypted.isEmpty()) return defaultValue
        val plaintext = try {
            tinkKeysetStore.prefsAead().decrypt(encrypted, ASSOCIATED_DATA)
        } catch (e: GeneralSecurityException) {
            throw CorruptionException("secure_prefs failed authentication", e)
        }
        return try {
            Json.decodeFromString(SecurePrefsData.serializer(), plaintext.decodeToString())
        } catch (e: SerializationException) {
            throw CorruptionException("secure_prefs decrypted but is not valid JSON", e)
        }
    }

    override suspend fun writeTo(t: SecurePrefsData, output: OutputStream) {
        val plaintext = Json.encodeToString(t).encodeToByteArray()
        val encrypted = tinkKeysetStore.prefsAead().encrypt(plaintext, ASSOCIATED_DATA)
        output.write(encrypted)
    }
}
