package com.lifevault.app.core.files.di

import android.content.Context
import coil3.ImageLoader
import com.lifevault.app.core.files.VaultFileStore
import com.lifevault.app.core.files.VaultImageFetcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VaultImageLoaderModule {

    /**
     * Section 6.7: decrypting `Fetcher`, memory cache only — disk cache stays disabled
     * so plaintext image bytes never touch disk. The memory cache is cleared on lock
     * (Step 8's `LockManager`, wired once a screen actually holds this loader).
     */
    @Provides
    @Singleton
    fun provideVaultImageLoader(
        @ApplicationContext context: Context,
        vaultFileStore: VaultFileStore,
    ): ImageLoader = ImageLoader.Builder(context)
        .components { add(VaultImageFetcher.Factory(vaultFileStore)) }
        .diskCache(null)
        .build()
}
