package com.lifevault.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LifeVaultApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // No explicit SQLCipher native-lib load call needed: net.zetetic:sqlcipher-android
        // (unlike the old deprecated net.zetetic:android-database-sqlcipher) loads its
        // native library automatically on first use of net.zetetic.database.sqlcipher.*.
        // TODO(step 21 - billing): configure WorkManager's HiltWorkerFactory once
        // core/notifications and core/backup workers exist (Section 7.3, Section 9.5).
    }
}
