package com.lifevault.app.core.security.prefs

import androidx.datastore.core.DataStoreFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lifevault.app.core.security.DbPassphraseProvider
import com.lifevault.app.core.security.KeyManager
import com.lifevault.app.core.security.TinkKeysetStore
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

/**
 * Requires a real Android Keystore — not executed in this build environment (see
 * BUILD_LOG.md Step 4). Written to run via `./gradlew connectedDebugAndroidTest`.
 */
@RunWith(AndroidJUnit4::class)
class SecurePrefsInstrumentedTest {

    private fun newSecurePrefs(): SecurePrefs {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val tinkKeysetStore = TinkKeysetStore(context, KeyManager())
        val dataStore = DataStoreFactory.create(
            serializer = SecurePrefsSerializer(tinkKeysetStore),
            produceFile = { File(context.filesDir, "test_secure_prefs_${UUID.randomUUID()}.bin") },
        )
        return SecurePrefs(dataStore)
    }

    @Test
    fun freshStoreHasNoDbPassphrase() = runBlocking {
        assertNull(newSecurePrefs().getDbPassphrase())
    }

    @Test
    fun dbPassphraseRoundTripsThroughEncryptedDisk() = runBlocking {
        val securePrefs = newSecurePrefs()
        val passphrase = ByteArray(32) { it.toByte() }

        securePrefs.setDbPassphrase(passphrase)

        assertArrayEquals(passphrase, securePrefs.getDbPassphrase())
    }

    @Test
    fun clearResetsToDefaults() = runBlocking {
        val securePrefs = newSecurePrefs()
        securePrefs.setDbPassphrase(ByteArray(32))

        securePrefs.clear()

        assertNull(securePrefs.getDbPassphrase())
    }

    @Test
    fun dbPassphraseProviderGeneratesOnceAndReusesAfter() = runBlocking {
        val provider = DbPassphraseProvider(newSecurePrefs())

        val first = provider.getOrCreatePassphrase()
        val second = provider.getOrCreatePassphrase()

        assertEquals(32, first.size)
        assertArrayEquals(first, second)
    }
}
