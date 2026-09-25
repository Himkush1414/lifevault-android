package com.lifevault.app.core.security.fake

import com.lifevault.app.core.util.DispatcherProvider
import kotlinx.coroutines.Dispatchers

/** Unconfined so LockManager's internally-launched coroutines complete deterministically
 * within a test, without needing to advance a virtual-time test scheduler. */
class FakeDispatcherProvider : DispatcherProvider {
    override val main = Dispatchers.Unconfined
    override val io = Dispatchers.Unconfined
    override val default = Dispatchers.Unconfined
}
