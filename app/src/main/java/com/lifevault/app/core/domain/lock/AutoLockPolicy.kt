package com.lifevault.app.core.domain.lock

/** Section 8.5 options (seconds); "Immediately" is 0. */
val AUTO_LOCK_TIMEOUT_OPTIONS_SECONDS = listOf(0, 30, 60, 300)
const val DEFAULT_AUTO_LOCK_TIMEOUT_SECONDS = 30

/**
 * Section 8.5: `ON_STOP` records `backgroundedAtElapsedRealtimeMs`; `ON_START` locks if
 * the elapsed time since then is at least [timeoutSeconds] (0 = lock on any backgrounding).
 */
fun shouldAutoLock(
    backgroundedAtElapsedRealtimeMs: Long,
    resumedAtElapsedRealtimeMs: Long,
    timeoutSeconds: Int,
): Boolean {
    val elapsedMs = resumedAtElapsedRealtimeMs - backgroundedAtElapsedRealtimeMs
    return elapsedMs >= timeoutSeconds * 1000L
}
