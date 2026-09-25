package com.lifevault.app.core.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.lifevault.app.core.security.LockState

/**
 * When [LockState.LOCKED], [content] (the NavHost) is not composed at all — this
 * guarantees no screen content renders, or appears in the recents thumbnail, before
 * authentication (Section 3.4).
 */
@Composable
fun LockGate(content: @Composable () -> Unit) {
    val lockViewModel: LockViewModel = hiltViewModel()
    val lockState by lockViewModel.state.collectAsState()
    when (lockState) {
        LockState.LOCKED -> LockScreenPlaceholder()
        LockState.UNLOCKED -> content()
    }
}

// TODO(step 8 - app lock UI): replace with the real S05 Lock screen (PinDots + NumericKeypad).
@Composable
private fun LockScreenPlaceholder() {
    Surface(modifier = Modifier.fillMaxSize()) {}
}
