package com.lifevault.app.core.repository.fake

import com.lifevault.app.core.database.dao.SettingsDao
import com.lifevault.app.core.database.entity.SettingsEntity
import com.lifevault.app.core.domain.model.BackupFrequency
import com.lifevault.app.core.domain.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSettingsDao(
    initial: SettingsEntity = SettingsEntity(
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
        schemaSeedVersion = 1,
    ),
) : SettingsDao {
    private val state = MutableStateFlow(initial)

    override fun observe() = state

    override suspend fun update(settings: SettingsEntity) {
        state.value = settings
    }
}
