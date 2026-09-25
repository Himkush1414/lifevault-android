package com.lifevault.app.core.security

import android.os.SystemClock
import javax.inject.Inject
import javax.inject.Singleton

/** Isolates [System.currentTimeMillis]/[SystemClock.elapsedRealtime] so lockout/auto-lock
 * logic (Section 8.4/8.5) can be tested with fake clocks instead of real wall-clock time. */
interface TimeSource {
    fun epochMillis(): Long
    fun elapsedRealtimeMillis(): Long
}

@Singleton
class SystemTimeSource @Inject constructor() : TimeSource {
    override fun epochMillis(): Long = System.currentTimeMillis()
    override fun elapsedRealtimeMillis(): Long = SystemClock.elapsedRealtime()
}
