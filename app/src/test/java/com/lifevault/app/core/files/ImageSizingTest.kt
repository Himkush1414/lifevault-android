package com.lifevault.app.core.files

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageSizingTest {

    // --- computeInSampleSize ---

    @Test
    fun `no downsampling needed when already smaller than target`() {
        assertEquals(1, ImageSizing.computeInSampleSize(1000, 800, 2480))
    }

    @Test
    fun `downsamples by the largest power of two that still exceeds the target`() {
        // 10000 long edge, target 2480: 10000/2=5000 (>=2480), /4=2500 (>=2480), /8=1250 (<2480) -> 4
        assertEquals(4, ImageSizing.computeInSampleSize(10000, 6000, 2480))
    }

    @Test
    fun `a 50MP-class image is downsampled, not decoded at full size`() {
        // Guards the OOM concern from Section 6.6 directly.
        val sampleSize = ImageSizing.computeInSampleSize(8000, 6000, 1600)
        assertTrue(sampleSize >= 4)
    }

    // --- targetDimensions ---

    @Test
    fun `smaller-than-max images are never upscaled`() {
        assertEquals(1000 to 800, ImageSizing.targetDimensions(1000, 800, 2480))
    }

    @Test
    fun `larger images are scaled down to the max long edge, preserving aspect ratio`() {
        val (width, height) = ImageSizing.targetDimensions(4960, 3720, 2480) // 4:3 at 2x the cap
        assertEquals(2480, width)
        assertEquals(1860, height)
    }

    @Test
    fun `a portrait image's height is capped, not its width`() {
        // 4960 = 2 * 2480 exactly, so the halved width is an exact integer too.
        val (width, height) = ImageSizing.targetDimensions(1000, 4960, 2480)
        assertEquals(2480, height)
        assertEquals(500, width)
    }

    // --- shouldRetryAtLowerQuality ---

    @Test
    fun `under the 1point5MB budget does not retry`() {
        assertFalse(ImageSizing.shouldRetryAtLowerQuality(1_000_000))
    }

    @Test
    fun `over the 1point5MB budget retries`() {
        assertTrue(ImageSizing.shouldRetryAtLowerQuality(2_000_000))
    }
}
