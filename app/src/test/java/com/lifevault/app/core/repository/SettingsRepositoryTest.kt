package com.lifevault.app.core.repository

import com.lifevault.app.core.domain.model.ThemeMode
import com.lifevault.app.core.repository.fake.FakeSettingsDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SettingsRepositoryTest {

    private lateinit var dao: FakeSettingsDao
    private lateinit var repository: SettingsRepository

    @Before
    fun setUp() {
        dao = FakeSettingsDao()
        repository = SettingsRepository(dao)
    }

    @Test
    fun `setThemeMode updates only the theme field`() = runTest {
        repository.setThemeMode(ThemeMode.DARK)

        val settings = repository.observe().first()
        assertEquals(ThemeMode.DARK, settings.themeMode)
        assertEquals(listOf(30, 7), settings.defaultReminderOffsets) // untouched
    }

    @Test
    fun `setDefaultReminderOffsets replaces the offset list`() = runTest {
        repository.setDefaultReminderOffsets(listOf(90, 30, 7, 1))

        assertEquals(listOf(90, 30, 7, 1), repository.observe().first().defaultReminderOffsets)
    }

    @Test
    fun `setOnboardingCompleted flips the flag`() = runTest {
        repository.setOnboardingCompleted(true)

        assertTrue(repository.observe().first().onboardingCompleted)
    }
}
