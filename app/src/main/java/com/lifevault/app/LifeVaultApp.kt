package com.lifevault.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import androidx.work.WorkManager
import com.lifevault.app.core.capture.CaptureSessionStore
import com.lifevault.app.core.security.LockLifecycleObserver
import com.lifevault.app.core.work.TrashPurgeWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class LifeVaultApp : Application(), Configuration.Provider {

    @Inject lateinit var lockLifecycleObserver: LockLifecycleObserver
    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var captureSessionStore: CaptureSessionStore

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        // No explicit SQLCipher native-lib load call needed: net.zetetic:sqlcipher-android
        // (unlike the old deprecated net.zetetic:android-database-sqlcipher) loads its
        // native library automatically on first use of net.zetetic.database.sqlcipher.*.
        ProcessLifecycleOwner.get().lifecycle.addObserver(lockLifecycleObserver)
        TrashPurgeWorker.ensureScheduled(WorkManager.getInstance(this))
        // Section 6.1: capture sessions older than 24h are abandoned, not resumable.
        captureSessionStore.cleanupStaleSessions()
    }
}
