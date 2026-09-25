package com.lifevault.app.core.security

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Section 8.5's process-lifecycle and screen-off triggers, registered once from
 * [com.lifevault.app.LifeVaultApp.onCreate] against [androidx.lifecycle.ProcessLifecycleOwner].
 */
@Singleton
class LockLifecycleObserver @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lockManager: LockManager,
) : DefaultLifecycleObserver {

    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            lockManager.onScreenOff()
        }
    }
    private var screenOffReceiverRegistered = false

    override fun onStart(owner: LifecycleOwner) {
        lockManager.onAppForegrounded()
        if (!screenOffReceiverRegistered) {
            context.registerReceiver(screenOffReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF))
            screenOffReceiverRegistered = true
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        lockManager.onAppBackgrounded()
        if (screenOffReceiverRegistered) {
            context.unregisterReceiver(screenOffReceiver)
            screenOffReceiverRegistered = false
        }
    }
}
