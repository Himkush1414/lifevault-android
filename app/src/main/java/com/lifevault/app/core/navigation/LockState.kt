package com.lifevault.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

/** Section 3.4 "Lock gate implementation." Real states land in Step 8 (App lock). */
enum class LockState { LOCKED, UNLOCKED }

// TODO(step 8 - app lock): replace with LockManager.state observing PIN/biometric/
// auto-lock/screen-off signals (Section 8.4, 8.5) instead of a hard-coded value.
@Composable
fun rememberLockState(): State<LockState> = remember { mutableStateOf(LockState.UNLOCKED) }
