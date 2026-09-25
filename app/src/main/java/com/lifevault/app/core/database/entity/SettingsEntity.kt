package com.lifevault.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lifevault.app.core.domain.model.BackupFrequency
import com.lifevault.app.core.domain.model.ThemeMode
import java.time.Instant

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1, // single-row table
    val themeMode: ThemeMode,
    val dynamicColor: Boolean, // Pro
    val defaultReminderOffsets: List<Int>, // JSON via converter, default [30, 7]
    val dailyCheckMinutes: Int, // default 540 (09:00)
    val hideTitlesOnLockScreen: Boolean, // default true
    val autoBackupEnabled: Boolean, // Pro
    val autoBackupTreeUri: String?, // persisted SAF tree URI
    val autoBackupFrequency: BackupFrequency,
    val autoBackupKeep: Int, // default 3
    val lastBackupAt: Instant?,
    val lastBackupSizeBytes: Long?,
    val backupNudgeDismissedAt: Instant?,
    val onboardingCompleted: Boolean,
    val schemaSeedVersion: Int, // for built-in category seeding updates
)
