package com.lifevault.app.core.security.fake

import com.lifevault.app.core.security.TimeSource

class FakeTimeSource(
    var epoch: Long = 1_700_000_000_000,
    var elapsedRealtime: Long = 1_000_000,
) : TimeSource {
    override fun epochMillis(): Long = epoch
    override fun elapsedRealtimeMillis(): Long = elapsedRealtime

    fun advanceBothBy(millis: Long) {
        epoch += millis
        elapsedRealtime += millis
    }
}
