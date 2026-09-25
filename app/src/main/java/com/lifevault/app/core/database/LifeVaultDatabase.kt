package com.lifevault.app.core.database

import android.content.ContentValues
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lifevault.app.core.database.converter.Converters
import com.lifevault.app.core.database.dao.AttachmentDao
import com.lifevault.app.core.database.dao.CategoryDao
import com.lifevault.app.core.database.dao.DocumentDao
import com.lifevault.app.core.database.dao.RecentSearchDao
import com.lifevault.app.core.database.dao.ReminderDao
import com.lifevault.app.core.database.dao.SettingsDao
import com.lifevault.app.core.database.entity.AttachmentEntity
import com.lifevault.app.core.database.entity.CategoryEntity
import com.lifevault.app.core.database.entity.DocumentEntity
import com.lifevault.app.core.database.entity.DocumentFts
import com.lifevault.app.core.database.entity.RecentSearchEntity
import com.lifevault.app.core.database.entity.ReminderEntity
import com.lifevault.app.core.database.entity.SettingsEntity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Clock

const val DATABASE_NAME = "lifevault.db"

@Database(
    entities = [
        CategoryEntity::class,
        DocumentEntity::class,
        AttachmentEntity::class,
        ReminderEntity::class,
        SettingsEntity::class,
        DocumentFts::class,
        RecentSearchEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class LifeVaultDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun documentDao(): DocumentDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun reminderDao(): ReminderDao
    abstract fun settingsDao(): SettingsDao
    abstract fun recentSearchDao(): RecentSearchDao
}

/**
 * Section 5.6: seeds the 11 built-in categories and the single settings row on first
 * create. Runs as raw SQL against [SupportSQLiteDatabase] — Room's generated DAOs
 * aren't available yet inside `onCreate`, only the underlying connection is.
 */
class SeedCallback(private val clock: Clock) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        seedCategories(clock).forEach { category ->
            db.insert("categories", android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE, category.toContentValues())
        }
        db.insert(
            "settings",
            android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE,
            seedSettings().toContentValues(),
        )
    }

    private fun CategoryEntity.toContentValues() = ContentValues().apply {
        put("id", id)
        put("name", name)
        put("iconKey", iconKey)
        put("colorKey", colorKey)
        put("isSystem", isSystem)
        put("sortOrder", sortOrder)
        put("createdAt", createdAt.toEpochMilli())
        put("updatedAt", updatedAt.toEpochMilli())
    }

    private fun SettingsEntity.toContentValues() = ContentValues().apply {
        put("id", id)
        put("themeMode", themeMode.name)
        put("dynamicColor", dynamicColor)
        put("defaultReminderOffsets", Json.encodeToString(defaultReminderOffsets))
        put("dailyCheckMinutes", dailyCheckMinutes)
        put("hideTitlesOnLockScreen", hideTitlesOnLockScreen)
        put("autoBackupEnabled", autoBackupEnabled)
        put("autoBackupTreeUri", autoBackupTreeUri)
        put("autoBackupFrequency", autoBackupFrequency.name)
        put("autoBackupKeep", autoBackupKeep)
        put("lastBackupAt", lastBackupAt?.toEpochMilli())
        put("lastBackupSizeBytes", lastBackupSizeBytes)
        put("backupNudgeDismissedAt", backupNudgeDismissedAt?.toEpochMilli())
        put("onboardingCompleted", onboardingCompleted)
        put("schemaSeedVersion", schemaSeedVersion)
    }
}
