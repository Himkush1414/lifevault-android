package com.lifevault.app.core.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

/**
 * When [LockState.LOCKED], [content] (the NavHost) is not composed at all — this
 * guarantees no screen content renders, or appears in the recents thumbnail, before
 * authentication (Section 3.4).
 */
@Composable
fun LockGate(content: @Composable () -> Unit) {
    val lockState by rememberLockState()
    when (lockState) {
        LockState.LOCKED -> LockScreenPlaceholder()
        LockState.UNLOCKED -> content()
    }
}

// TODO(step 8 - app lock): replace with the real S05 Lock screen.
@Composable
private fun LockScreenPlaceholder() {
    Surface(modifier = Modifier.fillMaxSize()) {}
}
