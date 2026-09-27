package com.lifevault.app.core.files

import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream

/**
 * Requires real `android.graphics.Bitmap`/EXIF decoding — not executed in this build
 * environment (see BUILD_LOG.md Step 10). Written to run via
 * `./gradlew connectedDebugAndroidTest`. Covers Section 12 step 10's accept check: a
 * ~30MB-class image (a large uncompressed bitmap, matching what a high-res scan
 * decodes to) is processed without exceeding a sane memory budget, and the pipeline's
 * downsample/resize/encode math holds end to end.
 */
@RunWith(AndroidJUnit4::class)
class ImageProcessorInstrumentedTest {

    private val processor = ImageProcessor()

    /** A large synthetic JPEG whose *decoded* (uncompressed ARGB_8888) size is ~30MB
     * (8000x1000x4 bytes ≈ 30.5MB) — the class of input Section 12 step 10 calls out. */
    private fun largeTestImageBytes(width: Int = 8000, height: Int = 1000): ByteArray {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(0xFF808080.toInt())
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        bitmap.recycle()
        return out.toByteArray()
    }

    @Test
    fun processesA30MBClassImageWithoutRunningOutOfMemory() = runTest {
        val result = processor.process(largeTestImageBytes())

        assertTrue(result.widthPx <= ImageSizing.MAX_LONG_EDGE_PX)
        assertTrue(result.heightPx <= ImageSizing.MAX_LONG_EDGE_PX)
        assertTrue(result.fullBytes.isNotEmpty())
        assertTrue(result.thumbBytes.isNotEmpty())
        assertEquals(64, result.sha256Hex.length)
    }

    @Test
    fun smallImagesAreNeverUpscaled() = runTest {
        val result = processor.process(largeTestImageBytes(width = 400, height = 300))

        assertEquals(400, result.widthPx)
        assertEquals(300, result.heightPx)
    }

    @Test
    fun processingTheSameBytesTwiceProducesTheSameHash() = runTest {
        val bytes = largeTestImageBytes(width = 1000, height = 800)

        val first = processor.process(bytes)
        val second = processor.process(bytes)

        assertEquals(first.sha256Hex, second.sha256Hex)
    }
}
