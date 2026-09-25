package com.lifevault.app.core.security.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import com.lifevault.app.core.security.TinkKeysetStore
import com.lifevault.app.core.security.prefs.SecurePrefsData
import com.lifevault.app.core.security.prefs.SecurePrefsSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    @Provides
    @Singleton
    fun provideSecurePrefsDataStore(
        @ApplicationContext context: Context,
        tinkKeysetStore: TinkKeysetStore,
    ): DataStore<SecurePrefsData> = DataStoreFactory.create(
        serializer = SecurePrefsSerializer(tinkKeysetStore),
        produceFile = { File(context.filesDir, "security/secure_prefs.bin") },
    )
}
