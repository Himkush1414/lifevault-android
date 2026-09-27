package com.lifevault.app.core.files

/**
 * Section 6.7's on-disk layout: `vault/v1/<shard>/<id>.bin` (+ `_thumb.bin`), where the
 * shard is the first 2 hex characters of the attachment id — keeps any one directory
 * from growing huge. Filenames are the attachment UUID only: no titles, categories or
 * original filenames ever touch disk.
 */
object VaultPaths {
    const val VAULT_VERSION = "v1"

    fun shardOf(attachmentId: String): String {
        require(attachmentId.length >= 2) { "attachmentId must be at least 2 characters: $attachmentId" }
        return attachmentId.take(2).lowercase()
    }

    fun relativeFilePath(attachmentId: String): String = "$VAULT_VERSION/${shardOf(attachmentId)}/$attachmentId.bin"

    fun relativeThumbPath(attachmentId: String): String =
        "$VAULT_VERSION/${shardOf(attachmentId)}/${attachmentId}_thumb.bin"
}
