package com.lifevault.app.core.util.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

/**
 * The single source of "now" for the whole app (Section 2.1). Every place that needs
 * today's date or the current instant takes a [Clock] parameter instead of calling
 * [java.time.LocalDate.now] / [java.time.Instant.now] directly, so tests can inject a
 * fixed clock.
 */
@Module
@InstallIn(SingletonComponent::class)
object ClockModule {
    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemDefaultZone()
}
