package com.lifevault.app.core.database

import com.lifevault.app.core.database.entity.CategoryEntity
import com.lifevault.app.core.database.entity.SettingsEntity
import com.lifevault.app.core.domain.model.BackupFrequency
import com.lifevault.app.core.domain.model.ThemeMode
import java.time.Clock
import java.time.Instant

/** Current seed generation (Section 5.3 `settings.schemaSeedVersion`) — bump when built-in categories change. */
const val CURRENT_SEED_VERSION = 1

private data class BuiltInCategory(val id: String, val name: String, val iconKey: String, val colorKey: String)

// Section 4.1.4 "Built-in category defaults" + Section 4.4 category icon map.
private val BUILT_IN_CATEGORIES = listOf(
    BuiltInCategory("cat_identity", "Identity", "badge", "blue"),
    BuiltInCategory("cat_travel", "Travel", "flight", "indigo"),
    BuiltInCategory("cat_vehicle", "Vehicle", "directions_car", "orange"),
    BuiltInCategory("cat_insurance", "Insurance", "health_and_safety", "teal"),
    BuiltInCategory("cat_medical", "Medical", "medical_information", "red"),
    BuiltInCategory("cat_finance_tax", "Finance & Tax", "account_balance", "green"),
    BuiltInCategory("cat_property", "Property", "home_work", "brown"),
    BuiltInCategory("cat_warranty_receipts", "Warranty & Receipts", "receipt_long", "purple"),
    BuiltInCategory("cat_subscriptions", "Subscriptions", "autorenew", "pink"),
    BuiltInCategory("cat_education", "Education", "school", "cyan"),
    BuiltInCategory("cat_other", "Other", "folder", "grey"),
)

/** Section 5.6: on DB create, insert the 11 built-in categories and the single settings row. */
fun seedCategories(clock: Clock): List<CategoryEntity> {
    val now = Instant.now(clock)
    return BUILT_IN_CATEGORIES.mapIndexed { index, builtIn ->
        CategoryEntity(
            id = builtIn.id,
            name = builtIn.name,
            iconKey = builtIn.iconKey,
            colorKey = builtIn.colorKey,
            isSystem = true,
            sortOrder = index,
            createdAt = now,
            updatedAt = now,
        )
    }
}

fun seedSettings(): SettingsEntity = SettingsEntity(
    id = 1,
    themeMode = ThemeMode.SYSTEM,
    dynamicColor = false,
    defaultReminderOffsets = listOf(30, 7),
    dailyCheckMinutes = 9 * 60,
    hideTitlesOnLockScreen = true,
    autoBackupEnabled = false,
    autoBackupTreeUri = null,
    autoBackupFrequency = BackupFrequency.WEEKLY,
    autoBackupKeep = 3,
    lastBackupAt = null,
    lastBackupSizeBytes = null,
    backupNudgeDismissedAt = null,
    onboardingCompleted = false,
    schemaSeedVersion = CURRENT_SEED_VERSION,
)
