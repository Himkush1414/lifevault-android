package com.lifevault.app.core.files

import android.content.Context
import com.lifevault.app.core.security.TinkKeysetStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Section 6.7: encrypted vault storage in internal app storage. Every attachment
 * (and its thumbnail) is Tink `StreamingAead`-encrypted with the attachment id as
 * associated data — a ciphertext swapped onto another record's row fails to decrypt.
 * Writes are atomic (`.tmp` + `fsync` + `renameTo`); nothing here ever touches
 * `cacheDir` or leaves plaintext on disk.
 */
@Singleton
class VaultFileStore @Inject constructor(
    @ApplicationContext context: Context,
    private val tinkKeysetStore: TinkKeysetStore,
) {
    private val vaultDir = File(context.filesDir, "vault").apply {
        mkdirs()
        File(this, ".nomedia").apply { if (!exists()) createNewFile() }
    }

    fun writeEncrypted(attachmentId: String, plaintext: ByteArray): Long =
        writeEncryptedInternal(VaultPaths.relativeFilePath(attachmentId), attachmentId, plaintext)

    fun writeEncryptedThumb(attachmentId: String, plaintext: ByteArray): Long =
        writeEncryptedInternal(VaultPaths.relativeThumbPath(attachmentId), attachmentId, plaintext)

    /** Caller must close the returned stream. */
    fun openDecryptingStream(attachmentId: String, thumb: Boolean = false): InputStream {
        val file = fileFor(attachmentId, thumb)
        return tinkKeysetStore.filesStreamingAead()
            .newDecryptingStream(FileInputStream(file), associatedData(attachmentId))
    }

    fun delete(attachmentId: String) {
        fileFor(attachmentId, thumb = false).delete()
        fileFor(attachmentId, thumb = true).delete()
    }

    fun exists(attachmentId: String): Boolean = fileFor(attachmentId, thumb = false).exists()

    fun encryptedSizeBytes(attachmentId: String): Long = fileFor(attachmentId, thumb = false).length()

    /** Section 6.7/6.9 orphan sweep and storage-usage accounting. */
    fun allStoredAttachmentIds(): Set<String> {
        val shardDirs = File(vaultDir, VaultPaths.VAULT_VERSION).listFiles() ?: return emptySet()
        return shardDirs
            .flatMap { it.listFiles()?.toList().orEmpty() }
            .mapNotNull { file ->
                file.name.removeSuffix(".bin").removeSuffix("_thumb").takeIf { it.isNotBlank() }
            }
            .toSet()
    }

    private fun writeEncryptedInternal(relativePath: String, attachmentId: String, plaintext: ByteArray): Long {
        val target = File(vaultDir, relativePath)
        target.parentFile?.mkdirs()
        val tmp = File(target.parentFile, "${target.name}.tmp")
        FileOutputStream(tmp).use { fos ->
            tinkKeysetStore.filesStreamingAead()
                .newEncryptingStream(fos, associatedData(attachmentId))
                .use { it.write(plaintext) }
            fos.fd.sync()
        }
        check(tmp.renameTo(target)) { "Failed to atomically persist $relativePath" }
        return target.length()
    }

    private fun fileFor(attachmentId: String, thumb: Boolean): File {
        val relativePath = if (thumb) {
            VaultPaths.relativeThumbPath(attachmentId)
        } else {
            VaultPaths.relativeFilePath(attachmentId)
        }
        return File(vaultDir, relativePath)
    }

    private fun associatedData(attachmentId: String): ByteArray = attachmentId.toByteArray()
}
