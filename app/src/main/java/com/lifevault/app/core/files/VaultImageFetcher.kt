package com.lifevault.app.core.files

import coil3.ImageLoader
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import okio.buffer
import okio.source

/**
 * Section 6.7's Coil `Fetcher`: decrypts a vault attachment straight into Coil's decode
 * pipeline via `streamingAead.newDecryptingStream(...)` — plaintext bytes only ever
 * exist in memory, never written to `cacheDir` (Coil's disk cache is disabled for this
 * loader in [VaultImageLoaderModule]).
 */
class VaultImageFetcher(
    private val ref: VaultImageRef,
    private val vaultFileStore: VaultFileStore,
) : Fetcher {

    override suspend fun fetch(): FetchResult {
        val decryptingStream = vaultFileStore.openDecryptingStream(ref.attachmentId, ref.thumb)
        return SourceFetchResult(
            source = ImageSource(decryptingStream.source().buffer(), fileSystem = okio.FileSystem.SYSTEM),
            mimeType = "image/webp",
            dataSource = DataSource.DISK,
        )
    }

    class Factory(private val vaultFileStore: VaultFileStore) : Fetcher.Factory<VaultImageRef> {
        override fun create(data: VaultImageRef, options: Options, imageLoader: ImageLoader): Fetcher =
            VaultImageFetcher(data, vaultFileStore)
    }
}
