package com.lifevault.app.core.files

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lifevault.app.core.security.KeyManager
import com.lifevault.app.core.security.TinkKeysetStore
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.IOException
import java.util.UUID

/**
 * Requires a real Android Keystore — not executed in this build environment (see
 * BUILD_LOG.md Step 10). Written to run via `./gradlew connectedDebugAndroidTest`.
 * Covers Section 12 step 10's accept check (minus the 30MB-under-budget case, which
 * lives in ImageProcessorInstrumentedTest): tampered ciphertext rejected, no plaintext
 * left in filesDir.
 */
@RunWith(AndroidJUnit4::class)
class VaultFileStoreInstrumentedTest {

    private fun newStore(): VaultFileStore {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        return VaultFileStore(context, TinkKeysetStore(context, KeyManager()))
    }

    @Test
    fun encryptThenDecryptRoundTrips() {
        val store = newStore()
        val id = UUID.randomUUID().toString()
        val plaintext = ByteArray(200_000) { it.toByte() }

        store.writeEncrypted(id, plaintext)
        val decrypted = store.openDecryptingStream(id).use { it.readBytes() }

        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun tamperedCiphertextFailsToDecrypt() {
        val store = newStore()
        val id = UUID.randomUUID().toString()
        store.writeEncrypted(id, "some plaintext bytes".toByteArray())

        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val file = File(File(context.filesDir, "vault"), VaultPaths.relativeFilePath(id))
        val bytes = file.readBytes()
        bytes[bytes.size - 1] = (bytes[bytes.size - 1].toInt() xor 0x01).toByte()
        file.writeBytes(bytes)

        assertThrows(IOException::class.java) {
            store.openDecryptingStream(id).use { it.readBytes() }
        }
    }

    @Test
    fun associatedDataBindsCiphertextToItsOwnAttachmentId() {
        val store = newStore()
        val idA = UUID.randomUUID().toString()
        val idB = UUID.randomUUID().toString()
        store.writeEncrypted(idA, "payload for A".toByteArray())

        // Copy A's ciphertext file onto B's path — simulates a swapped/misattributed file.
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val vaultDir = File(context.filesDir, "vault")
        val fileA = File(vaultDir, VaultPaths.relativeFilePath(idA))
        val fileB = File(vaultDir, VaultPaths.relativeFilePath(idB))
        fileB.parentFile?.mkdirs()
        fileA.copyTo(fileB, overwrite = true)

        assertThrows(IOException::class.java) {
            store.openDecryptingStream(idB).use { it.readBytes() }
        }
    }

    @Test
    fun deleteRemovesBothTheFileAndItsThumbnail() {
        val store = newStore()
        val id = UUID.randomUUID().toString()
        store.writeEncrypted(id, "full".toByteArray())
        store.writeEncryptedThumb(id, "thumb".toByteArray())

        store.delete(id)

        assertFalse(store.exists(id))
    }

    @Test
    fun noPlaintextBytesAppearAnywhereUnderFilesDir() {
        val store = newStore()
        val id = UUID.randomUUID().toString()
        val marker = "UNMISTAKABLE_PLAINTEXT_MARKER_${UUID.randomUUID()}"
        store.writeEncrypted(id, marker.toByteArray())

        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val allBytesConcatenated = context.filesDir.walkTopDown()
            .filter { it.isFile }
            .joinToString("") { it.readText(Charsets.ISO_8859_1) }

        assertFalse(allBytesConcatenated.contains(marker))
    }
}
