package com.lifevault.app.core.security.di

import com.lifevault.app.core.security.SystemTimeSource
import com.lifevault.app.core.security.TimeSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class TimeSourceModule {
    @Binds
    abstract fun bindTimeSource(impl: SystemTimeSource): TimeSource
}
