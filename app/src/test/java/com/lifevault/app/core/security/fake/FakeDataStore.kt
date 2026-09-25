package com.lifevault.app.core.security.fake

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** An in-memory [DataStore], so tests don't need real file I/O or Android. */
class FakeDataStore<T>(initial: T) : DataStore<T> {
    private val state = MutableStateFlow(initial)
    private val mutex = Mutex()

    override val data = state

    override suspend fun updateData(transform: suspend (t: T) -> T): T = mutex.withLock {
        val updated = transform(state.value)
        state.value = updated
        updated
    }
}
