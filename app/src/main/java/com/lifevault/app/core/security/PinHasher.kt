package com.lifevault.app.core.security

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton

private const val ALGORITHM = "PBKDF2WithHmacSHA256"
private const val SALT_LENGTH_BYTES = 16
private const val KEY_LENGTH_BITS = 256
const val MIN_PBKDF2_ITERATIONS = 100_000
private const val CALIBRATION_TARGET_MILLIS = 250L
private const val CALIBRATION_SAMPLE_ITERATIONS = 50_000

data class PinHash(val hash: ByteArray, val salt: ByteArray, val iterations: Int) {
    override fun equals(other: Any?): Boolean =
        other is PinHash &&
            hash.contentEquals(other.hash) &&
            salt.contentEquals(other.salt) &&
            iterations == other.iterations

    override fun hashCode(): Int = 31 * (31 * hash.contentHashCode() + salt.contentHashCode()) + iterations
}

/**
 * Section 8.3: PBKDF2-HMAC-SHA256 with a 16-byte random salt and an iteration count
 * calibrated per-device so hashing takes ~250ms (never below [MIN_PBKDF2_ITERATIONS]).
 * The PIN itself is never stored, logged, or kept longer than one hash/verify call.
 */
@Singleton
class PinHasher @Inject constructor() {

    /** Times [CALIBRATION_SAMPLE_ITERATIONS] on this device and scales to [CALIBRATION_TARGET_MILLIS]. */
    fun calibrateIterations(): Int {
        val salt = ByteArray(SALT_LENGTH_BYTES)
        val elapsedMs = measureMillis {
            deriveKey("000000".toCharArray(), salt, CALIBRATION_SAMPLE_ITERATIONS)
        }
        if (elapsedMs <= 0) return MIN_PBKDF2_ITERATIONS
        val scaled = (CALIBRATION_SAMPLE_ITERATIONS * (CALIBRATION_TARGET_MILLIS.toDouble() / elapsedMs)).toInt()
        return scaled.coerceAtLeast(MIN_PBKDF2_ITERATIONS)
    }

    fun hash(pin: CharArray, iterations: Int = calibrateIterations()): PinHash {
        val salt = ByteArray(SALT_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        val derived = deriveKey(pin, salt, iterations)
        pin.fill('0')
        return PinHash(derived, salt, iterations)
    }

    /** Constant-time comparison ([MessageDigest.isEqual]) — never short-circuits on the first mismatch. */
    fun verify(pin: CharArray, expected: PinHash): Boolean {
        val derived = deriveKey(pin, expected.salt, expected.iterations)
        pin.fill('0')
        return MessageDigest.isEqual(derived, expected.hash)
    }

    private fun deriveKey(pin: CharArray, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(pin, salt, iterations, KEY_LENGTH_BITS)
        return try {
            SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private inline fun measureMillis(block: () -> Unit): Long {
        val start = System.nanoTime()
        block()
        return (System.nanoTime() - start) / 1_000_000
    }
}
