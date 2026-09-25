package com.lifevault.app.core.security.prefs

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

/** Typed access to the encrypted `secure_prefs` store (Section 5.3). */
@Singleton
class SecurePrefs @Inject constructor(private val dataStore: DataStore<SecurePrefsData>) {

    val data: Flow<SecurePrefsData> = dataStore.data

    val dbPassphrase: Flow<ByteArray?> = data.map { it.dbPassphraseBase64?.let(::decodeBase64) }

    suspend fun getDbPassphrase(): ByteArray? = dbPassphrase.first()

    suspend fun setDbPassphrase(passphrase: ByteArray) {
        dataStore.updateData { it.copy(dbPassphraseBase64 = encodeBase64(passphrase)) }
    }

    /** Section 8.7 "Erase all data": drops every field back to defaults. */
    suspend fun clear() {
        dataStore.updateData { SecurePrefsData() }
    }

    private fun encodeBase64(bytes: ByteArray) = Base64.getEncoder().encodeToString(bytes)
    private fun decodeBase64(value: String) = Base64.getDecoder().decode(value)
}
