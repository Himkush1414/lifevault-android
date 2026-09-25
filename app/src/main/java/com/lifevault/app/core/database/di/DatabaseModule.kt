package com.lifevault.app.core.database.di

import android.content.Context
import androidx.room.Room
import com.lifevault.app.core.database.DATABASE_NAME
import com.lifevault.app.core.database.LifeVaultDatabase
import com.lifevault.app.core.database.SeedCallback
import com.lifevault.app.core.database.dao.AttachmentDao
import com.lifevault.app.core.database.dao.CategoryDao
import com.lifevault.app.core.database.dao.DocumentDao
import com.lifevault.app.core.database.dao.RecentSearchDao
import com.lifevault.app.core.database.dao.ReminderDao
import com.lifevault.app.core.database.dao.SettingsDao
import com.lifevault.app.core.security.DbPassphraseProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        dbPassphraseProvider: DbPassphraseProvider,
        clock: Clock,
    ): LifeVaultDatabase {
        // Room's Builder needs the passphrase synchronously to construct the
        // SupportOpenHelperFactory; getOrCreatePassphrase() is suspend because it may
        // touch DataStore/Keystore on first launch. This provider only runs once
        // (singleton), off the main thread (Hilt resolves it lazily on first injection).
        val passphrase = runBlocking { dbPassphraseProvider.getOrCreatePassphrase() }
        // clearPassphrase = true: SQLCipher zeroes its copy of the array after opening
        // (Section 8.2 "the in-memory copy is zeroed after the DB opens").
        val factory = SupportOpenHelperFactory(passphrase, null, true)

        return Room.databaseBuilder(context, LifeVaultDatabase::class.java, DATABASE_NAME)
            .openHelperFactory(factory)
            .addCallback(SeedCallback(clock))
            .build()
    }

    @Provides
    fun provideCategoryDao(database: LifeVaultDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideDocumentDao(database: LifeVaultDatabase): DocumentDao = database.documentDao()

    @Provides
    fun provideAttachmentDao(database: LifeVaultDatabase): AttachmentDao = database.attachmentDao()

    @Provides
    fun provideReminderDao(database: LifeVaultDatabase): ReminderDao = database.reminderDao()

    @Provides
    fun provideSettingsDao(database: LifeVaultDatabase): SettingsDao = database.settingsDao()

    @Provides
    fun provideRecentSearchDao(database: LifeVaultDatabase): RecentSearchDao = database.recentSearchDao()
}
