package com.lifevault.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LifeVaultApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // TODO(step 4 - security core): load the SQLCipher native library here once the
        // encrypted database module exists, before any Room access is attempted.
        // TODO(step 21 - billing): configure WorkManager's HiltWorkerFactory once
        // core/notifications and core/backup workers exist (Section 7.3, Section 9.5).
    }
}
