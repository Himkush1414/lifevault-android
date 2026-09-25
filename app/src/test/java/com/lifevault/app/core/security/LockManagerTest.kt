package com.lifevault.app.core.security

import com.lifevault.app.core.security.fake.FakeDataStore
import com.lifevault.app.core.security.fake.FakeDispatcherProvider
import com.lifevault.app.core.security.fake.FakeTimeSource
import com.lifevault.app.core.security.prefs.SecurePrefs
import com.lifevault.app.core.security.prefs.SecurePrefsData
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LockManagerTest {

    private lateinit var securePrefs: SecurePrefs
    private lateinit var timeSource: FakeTimeSource
    private lateinit var lockManager: LockManager

    @Before
    fun setUp() {
        securePrefs = SecurePrefs(FakeDataStore(SecurePrefsData()))
        timeSource = FakeTimeSource()
        lockManager = LockManager(securePrefs, PinHasher(), timeSource, FakeDispatcherProvider())
    }

    @Test
    fun `with no PIN set up, state starts Unlocked`() = runTest {
        assertEquals(LockState.UNLOCKED, lockManager.state.value)
    }

    @Test
    fun `setting up a PIN enables app lock and unlocks immediately`() = runTest {
        lockManager.setupPin("284719".toCharArray())

        assertTrue(lockManager.isAppLockEnabled())
        assertEquals(LockState.UNLOCKED, lockManager.state.value)
    }

    @Test
    fun `correct PIN after locking unlocks`() = runTest {
        lockManager.setupPin("284719".toCharArray())
        lockManager.lock()
        assertEquals(LockState.LOCKED, lockManager.state.value)

        val result = lockManager.verifyPin("284719".toCharArray())

        assertEquals(VerifyPinResult.SUCCESS, result)
        assertEquals(LockState.UNLOCKED, lockManager.state.value)
    }

    @Test
    fun `wrong PIN does not unlock and stays Locked`() = runTest {
        lockManager.setupPin("284719".toCharArray())
        lockManager.lock()

        val result = lockManager.verifyPin("000001".toCharArray())

        assertEquals(VerifyPinResult.WRONG_PIN, result)
        assertEquals(LockState.LOCKED, lockManager.state.value)
    }

    @Test
    fun `a correct PIN resets the failure counter`() = runTest {
        lockManager.setupPin("284719".toCharArray())
        repeat(3) { lockManager.verifyPin("000000".toCharArray()) }

        lockManager.verifyPin("284719".toCharArray())

        assertEquals(0, securePrefs.snapshot().failedAttempts)
    }

    @Test
    fun `the 5th consecutive wrong PIN triggers a lockout`() = runTest {
        lockManager.setupPin("284719".toCharArray())

        repeat(4) { assertEquals(VerifyPinResult.WRONG_PIN, lockManager.verifyPin("000000".toCharArray())) }
        assertEquals(VerifyPinResult.WRONG_PIN, lockManager.verifyPin("000000".toCharArray())) // 5th failure

        assertTrue(lockManager.lockoutRemainingMillis() > 0)
    }

    @Test
    fun `a correct PIN is rejected as LOCKED_OUT while a lockout is active`() = runTest {
        lockManager.setupPin("284719".toCharArray())
        repeat(5) { lockManager.verifyPin("000000".toCharArray()) }

        val result = lockManager.verifyPin("284719".toCharArray())

        assertEquals(VerifyPinResult.LOCKED_OUT, result)
    }

    @Test
    fun `verifying the correct PIN again after the lockout window passes succeeds`() = runTest {
        lockManager.setupPin("284719".toCharArray())
        repeat(5) { lockManager.verifyPin("000000".toCharArray()) } // 30s lockout

        timeSource.advanceBothBy(31_000)

        assertEquals(VerifyPinResult.SUCCESS, lockManager.verifyPin("284719".toCharArray()))
    }

    @Test
    fun `unlockViaBiometric unlocks without touching the PIN failure counter`() = runTest {
        lockManager.setupPin("284719".toCharArray())
        lockManager.verifyPin("000000".toCharArray()) // 1 failure
        lockManager.lock()

        lockManager.unlockViaBiometric()

        assertEquals(LockState.UNLOCKED, lockManager.state.value)
        assertEquals(0, securePrefs.snapshot().failedAttempts) // biometric success also clears it
    }

    @Test
    fun `suppressNextBackgroundLock prevents the next auto-lock check`() = runTest {
        lockManager.setupPin("284719".toCharArray())
        lockManager.suppressNextBackgroundLock()

        lockManager.onAppBackgrounded()
        timeSource.advanceBothBy(60_000) // well past the 30s default timeout
        lockManager.onAppForegrounded()

        assertEquals(LockState.UNLOCKED, lockManager.state.value)
    }

    @Test
    fun `backgrounding past the auto-lock timeout locks on foreground`() = runTest {
        lockManager.setupPin("284719".toCharArray())

        lockManager.onAppBackgrounded()
        timeSource.advanceBothBy(60_000) // default timeout is 30s
        lockManager.onAppForegrounded()

        assertEquals(LockState.LOCKED, lockManager.state.value)
    }

    @Test
    fun `backgrounding for less than the timeout does not lock on foreground`() = runTest {
        lockManager.setupPin("284719".toCharArray())

        lockManager.onAppBackgrounded()
        timeSource.advanceBothBy(5_000) // well under the 30s default timeout
        lockManager.onAppForegrounded()

        assertEquals(LockState.UNLOCKED, lockManager.state.value)
    }

    @Test
    fun `disableAppLock unlocks and clears the enabled flag`() = runTest {
        lockManager.setupPin("284719".toCharArray())
        lockManager.lock()

        lockManager.disableAppLock()

        assertFalse(lockManager.isAppLockEnabled())
        assertEquals(LockState.UNLOCKED, lockManager.state.value)
    }
}
