package com.lifevault.app.core.security

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * "Forgot PIN?" (Section 8.4): confirm the device's own screen lock, not LifeVault's
 * PIN. `BiometricPrompt` with `DEVICE_CREDENTIAL` covers API 30+; below that, the
 * caller falls back to [confirmDeviceCredentialIntent] + `KeyguardManager` (API 26-29
 * has no `BiometricPrompt` device-credential mode).
 */
@Singleton
class DeviceCredentialAuthenticator @Inject constructor(@ApplicationContext private val context: Context) {

    fun hasDeviceScreenLock(): Boolean {
        val keyguardManager = context.getSystemService(KeyguardManager::class.java)
        return keyguardManager?.isDeviceSecure == true
    }

    fun canUseBiometricPromptPath(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

    /** API 30+ only — check [canUseBiometricPromptPath] first. */
    fun authenticateWithBiometricPrompt(activity: FragmentActivity, onResult: (Boolean) -> Unit) {
        val canAuthenticate = BiometricManager.from(activity).canAuthenticate(DEVICE_CREDENTIAL)
        if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
            onResult(false)
            return
        }
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Confirm it's you")
            .setAllowedAuthenticators(DEVICE_CREDENTIAL)
            .build()
        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) =
                    onResult(true)

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) = onResult(false)
            },
        )
        prompt.authenticate(promptInfo)
    }

    /** API 26-29 fallback: launch with `ActivityResultContracts.StartActivityForResult()`. */
    fun confirmDeviceCredentialIntent(): android.content.Intent? {
        val keyguardManager = context.getSystemService(KeyguardManager::class.java) ?: return null
        return keyguardManager.createConfirmDeviceCredentialIntent("Confirm it's you", null)
    }
}
