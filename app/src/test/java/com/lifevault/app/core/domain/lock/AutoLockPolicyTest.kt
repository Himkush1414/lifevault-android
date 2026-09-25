package com.lifevault.app.core.domain.lock

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoLockPolicyTest {

    @Test
    fun `Immediately (0s) locks on any backgrounding`() {
        assertTrue(
            shouldAutoLock(
                backgroundedAtElapsedRealtimeMs = 1000,
                resumedAtElapsedRealtimeMs = 1001,
                timeoutSeconds = 0,
            ),
        )
    }

    @Test
    fun `resuming before the timeout does not lock`() {
        assertFalse(
            shouldAutoLock(
                backgroundedAtElapsedRealtimeMs = 0,
                resumedAtElapsedRealtimeMs = 29_000,
                timeoutSeconds = 30,
            ),
        )
    }

    @Test
    fun `resuming exactly at the timeout locks`() {
        assertTrue(
            shouldAutoLock(
                backgroundedAtElapsedRealtimeMs = 0,
                resumedAtElapsedRealtimeMs = 30_000,
                timeoutSeconds = 30,
            ),
        )
    }

    @Test
    fun `resuming well after the timeout locks`() {
        assertTrue(
            shouldAutoLock(
                backgroundedAtElapsedRealtimeMs = 0,
                resumedAtElapsedRealtimeMs = 300_000,
                timeoutSeconds = 30,
            ),
        )
    }
}
