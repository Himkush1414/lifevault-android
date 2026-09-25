package com.lifevault.app.core.navigation

import androidx.lifecycle.ViewModel
import com.lifevault.app.core.security.LockManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LockViewModel @Inject constructor(lockManager: LockManager) : ViewModel() {
    val state = lockManager.state
}
