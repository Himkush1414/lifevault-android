package com.lifevault.app.core.repository

import com.lifevault.app.core.database.dao.SettingsDao
import com.lifevault.app.core.database.entity.SettingsEntity
import com.lifevault.app.core.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.Instant
import javax.inject.Inject

class SettingsRepository @Inject constructor(private val settingsDao: SettingsDao) {

    fun observe(): Flow<SettingsEntity> = settingsDao.observe()

    suspend fun setThemeMode(themeMode: ThemeMode) = updateSettings { it.copy(themeMode = themeMode) }

    suspend fun setDefaultReminderOffsets(offsets: List<Int>) =
        updateSettings { it.copy(defaultReminderOffsets = offsets) }

    suspend fun setDailyCheckMinutes(minutes: Int) = updateSettings { it.copy(dailyCheckMinutes = minutes) }

    suspend fun setHideTitlesOnLockScreen(hide: Boolean) =
        updateSettings { it.copy(hideTitlesOnLockScreen = hide) }

    suspend fun setOnboardingCompleted(completed: Boolean) =
        updateSettings { it.copy(onboardingCompleted = completed) }

    suspend fun setBackupNudgeDismissed(dismissedAt: Instant) =
        updateSettings { it.copy(backupNudgeDismissedAt = dismissedAt) }

    private suspend fun updateSettings(transform: (SettingsEntity) -> SettingsEntity) {
        settingsDao.update(transform(settingsDao.observe().first()))
    }
}
