package com.lifevault.app.feature.lock

import androidx.lifecycle.ViewModel
import com.lifevault.app.core.security.DeviceCredentialAuthenticator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/** Exposes [DeviceCredentialAuthenticator] to the Lock route (Section 8.4 "Forgot PIN?"). */
@HiltViewModel
class ForgotPinViewModel @Inject constructor(val authenticator: DeviceCredentialAuthenticator) : ViewModel()
