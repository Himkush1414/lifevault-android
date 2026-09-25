package com.lifevault.app.core.security

import android.security.keystore.KeyPermanentlyInvalidatedException
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import javax.crypto.Cipher
import javax.inject.Inject
import javax.inject.Singleton

/** Section 8.4's `BiometricPrompt` outcomes, collapsed to what the lock screen needs to show. */
sealed interface BiometricOutcome {
    data object Success : BiometricOutcome
    data object UserCancelled : BiometricOutcome
    data object NotRecognized : BiometricOutcome
    data class SystemLockout(val permanent: Boolean) : BiometricOutcome
    data object HardwareUnavailableOrNoneEnrolled : BiometricOutcome
    data object KeyInvalidated : BiometricOutcome
    data class OtherError(val message: String) : BiometricOutcome
}

/**
 * Wraps `BiometricPrompt` bound to the `lifevault_bio_unlock` Keystore key (Section 8.2):
 * a successful auth only counts if the returned `CryptoObject`'s cipher can actually
 * decrypt the stored unlock token — binding to a key defeats simple runtime hooks that
 * fake a success callback (Section 8.4).
 */
@Singleton
class BiometricAuthenticator @Inject constructor(private val keyManager: KeyManager) {

    fun canAuthenticate(activity: FragmentActivity): Boolean =
        BiometricManager.from(activity).canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS

    /** [onResult] is always called exactly once. [title]/[negativeButtonText] per Section 3.3 S05. */
    fun authenticate(
        activity: FragmentActivity,
        title: String,
        negativeButtonText: String,
        onResult: (BiometricOutcome) -> Unit,
    ) {
        val cipher: Cipher
        try {
            cipher = createUnlockCipher()
        } catch (@Suppress("SwallowedException") e: KeyPermanentlyInvalidatedException) {
            // Only the type matters here — Section 8.4 just needs to know re-enrollment happened.
            onResult(BiometricOutcome.KeyInvalidated)
            return
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BIOMETRIC_STRONG)
            .build()

        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    // The CryptoObject having been usable to reach this callback at all
                    // (BiometricPrompt only invokes onAuthenticationSucceeded after
                    // successfully unlocking the crypto object) is the binding Section
                    // 8.4 calls for.
                    onResult(BiometricOutcome.Success)
                }

                override fun onAuthenticationFailed() {
                    onResult(BiometricOutcome.NotRecognized)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onResult(mapError(errorCode, errString))
                }
            },
        )
        prompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
    }

    private fun createUnlockCipher(): Cipher {
        val key = keyManager.getOrCreateAesGcmKey(
            KeystoreAliases.BIO_UNLOCK,
            requireUserAuthentication = true,
            invalidatedByBiometricEnrollment = true,
        )
        return Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.ENCRYPT_MODE, key)
        }
    }

    private fun mapError(errorCode: Int, errString: CharSequence): BiometricOutcome = when (errorCode) {
        BiometricPrompt.ERROR_NEGATIVE_BUTTON, BiometricPrompt.ERROR_USER_CANCELED ->
            BiometricOutcome.UserCancelled
        BiometricPrompt.ERROR_LOCKOUT -> BiometricOutcome.SystemLockout(permanent = false)
        BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> BiometricOutcome.SystemLockout(permanent = true)
        BiometricPrompt.ERROR_HW_UNAVAILABLE, BiometricPrompt.ERROR_NO_BIOMETRICS,
        BiometricPrompt.ERROR_HW_NOT_PRESENT,
        -> BiometricOutcome.HardwareUnavailableOrNoneEnrolled
        else -> BiometricOutcome.OtherError(errString.toString())
    }
}
