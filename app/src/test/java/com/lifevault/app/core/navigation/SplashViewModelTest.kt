package com.lifevault.app.core.navigation

import com.lifevault.app.core.database.entity.SettingsEntity
import com.lifevault.app.core.domain.model.BackupFrequency
import com.lifevault.app.core.domain.model.ThemeMode
import com.lifevault.app.core.repository.SettingsRepository
import com.lifevault.app.core.repository.fake.FakeSettingsDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `first launch (onboarding not completed) routes to Onboarding`() = runTest {
        val dao = FakeSettingsDao(settings(onboardingCompleted = false))
        val viewModel = SplashViewModel(SettingsRepository(dao))

        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(Routes.Onboarding, viewModel.startDestination.value)
    }

    @Test
    fun `relaunch after onboarding routes to Home`() = runTest {
        val dao = FakeSettingsDao(settings(onboardingCompleted = true))
        val viewModel = SplashViewModel(SettingsRepository(dao))

        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(Routes.Home, viewModel.startDestination.value)
    }

    private fun settings(onboardingCompleted: Boolean) = SettingsEntity(
        id = 1,
        themeMode = ThemeMode.SYSTEM,
        dynamicColor = false,
        defaultReminderOffsets = listOf(30, 7),
        dailyCheckMinutes = 540,
        hideTitlesOnLockScreen = true,
        autoBackupEnabled = false,
        autoBackupTreeUri = null,
        autoBackupFrequency = BackupFrequency.WEEKLY,
        autoBackupKeep = 3,
        lastBackupAt = null,
        lastBackupSizeBytes = null,
        backupNudgeDismissedAt = null,
        onboardingCompleted = onboardingCompleted,
        schemaSeedVersion = 1,
    )
}
