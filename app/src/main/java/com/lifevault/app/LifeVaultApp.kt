package com.lifevault.app

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.lifevault.app.core.security.LockLifecycleObserver
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class LifeVaultApp : Application() {

    @Inject lateinit var lockLifecycleObserver: LockLifecycleObserver

    override fun onCreate() {
        super.onCreate()
        // No explicit SQLCipher native-lib load call needed: net.zetetic:sqlcipher-android
        // (unlike the old deprecated net.zetetic:android-database-sqlcipher) loads its
        // native library automatically on first use of net.zetetic.database.sqlcipher.*.
        ProcessLifecycleOwner.get().lifecycle.addObserver(lockLifecycleObserver)
        // TODO(step 21 - billing): configure WorkManager's HiltWorkerFactory once
        // core/notifications and core/backup workers exist (Section 7.3, Section 9.5).
    }
}
