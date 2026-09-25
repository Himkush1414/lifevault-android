package com.lifevault.app.core.security

import android.content.Context
import com.google.crypto.tink.Aead
import com.google.crypto.tink.InsecureSecretKeyAccess
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.StreamingAead
import com.google.crypto.tink.TinkProtoKeysetFormat
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.streamingaead.StreamingAeadConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/** Section 8.2 Keystore aliases. */
object KeystoreAliases {
    const val PREFS_MASTER = "lifevault_prefs_master"
    const val FILE_MASTER = "lifevault_file_master"
    const val BIO_UNLOCK = "lifevault_bio_unlock"
}

/**
 * Owns the two Tink keysets from Section 8.2: `"prefs"` (Aead, encrypts
 * [SecurePrefs][com.lifevault.app.core.security.prefs.SecurePrefs] values) and
 * `"files"` (StreamingAead, encrypts vault attachments). Each keyset is generated once,
 * then persisted only in its Keystore-wrapped form — the cleartext keyset never touches
 * disk. Callers get back live [Aead]/[StreamingAead] primitives to use for the process
 * lifetime.
 */
@Singleton
class TinkKeysetStore @Inject constructor(
    @ApplicationContext context: Context,
    private val keyManager: KeyManager,
) {
    private val keysetDir = File(context.filesDir, "security").apply { mkdirs() }
    private val prefsKeysetFile = File(keysetDir, "prefs_keyset.bin")
    private val filesKeysetFile = File(keysetDir, "files_keyset.bin")

    private val prefsAeadAssociatedData = "lifevault:prefs_keyset".toByteArray()
    private val filesKeysetAssociatedData = "lifevault:files_keyset".toByteArray()

    init {
        AeadConfig.register()
        StreamingAeadConfig.register()
    }

    private val prefsHandle: KeysetHandle by lazy {
        loadOrCreateHandle(
            file = prefsKeysetFile,
            keyAlias = KeystoreAliases.PREFS_MASTER,
            associatedData = prefsAeadAssociatedData,
            templateName = "AES256_GCM",
        )
    }

    private val filesHandle: KeysetHandle by lazy {
        loadOrCreateHandle(
            file = filesKeysetFile,
            keyAlias = KeystoreAliases.FILE_MASTER,
            associatedData = filesKeysetAssociatedData,
            templateName = "AES256_GCM_HKDF_1MB",
        )
    }

    /** Encrypts/decrypts [SecurePrefs][com.lifevault.app.core.security.prefs.SecurePrefs] field values. */
    fun prefsAead(): Aead = prefsHandle.getPrimitive(RegistryConfiguration.get(), Aead::class.java)

    /** Streaming-encrypts/decrypts vault attachment files (Step 10). */
    fun filesStreamingAead(): StreamingAead =
        filesHandle.getPrimitive(RegistryConfiguration.get(), StreamingAead::class.java)

    private fun loadOrCreateHandle(
        file: File,
        keyAlias: String,
        associatedData: ByteArray,
        templateName: String,
    ): KeysetHandle {
        val masterKey = keyManager.getOrCreateAesGcmKey(keyAlias)

        if (file.exists()) {
            val wrapped = file.readBytes()
            val cleartext = keyManager.decrypt(masterKey, wrapped, associatedData)
            return TinkProtoKeysetFormat.parseKeyset(cleartext, InsecureSecretKeyAccess.get())
        }

        val handle = KeysetHandle.generateNew(KeyTemplates.get(templateName))
        val cleartext = TinkProtoKeysetFormat.serializeKeyset(handle, InsecureSecretKeyAccess.get())
        val wrapped = keyManager.encrypt(masterKey, cleartext, associatedData)
        writeAtomically(file, wrapped)
        return handle
    }

    private fun writeAtomically(target: File, bytes: ByteArray) {
        val tmp = File(target.parentFile, "${target.name}.tmp")
        tmp.writeBytes(bytes)
        if (!tmp.renameTo(target)) {
            error("Failed to atomically persist ${target.name}")
        }
    }
}
