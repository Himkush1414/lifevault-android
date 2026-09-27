package com.lifevault.app.core.files

import kotlin.math.max

/** Section 6.6 image-processing budgets, as pure math so the logic is testable off-device. */
object ImageSizing {

    const val MAX_LONG_EDGE_PX = 2480 // ~A4 at 300dpi
    const val THUMBNAIL_LONG_EDGE_PX = 480
    const val MAX_PROCESSED_BYTES = 1_500_000L
    const val INITIAL_WEBP_QUALITY = 82
    const val THUMBNAIL_WEBP_QUALITY = 70
    const val BUDGET_RETRY_WEBP_QUALITY = 72

    /**
     * `BitmapFactory.Options.inSampleSize`: the largest power-of-two downsample factor
     * that still leaves the image at least [targetLongEdge] on its long side — never
     * decode a huge image at full resolution just to immediately downscale it.
     */
    fun computeInSampleSize(originalWidth: Int, originalHeight: Int, targetLongEdge: Int): Int {
        val longEdge = max(originalWidth, originalHeight)
        var sampleSize = 1
        while (longEdge / (sampleSize * 2) >= targetLongEdge) {
            sampleSize *= 2
        }
        return sampleSize
    }

    /** Section 6.6: long edge capped at [maxLongEdge]; smaller images are never upscaled. */
    fun targetDimensions(width: Int, height: Int, maxLongEdge: Int): Pair<Int, Int> {
        val longEdge = max(width, height)
        if (longEdge <= maxLongEdge) return width to height
        val scale = maxLongEdge.toDouble() / longEdge
        val scaledWidth = (width * scale).toInt().coerceAtLeast(1)
        val scaledHeight = (height * scale).toInt().coerceAtLeast(1)
        return scaledWidth to scaledHeight
    }

    /** Section 6.6 budget check: over budget once, retry at a lower quality; otherwise keep as-is. */
    fun shouldRetryAtLowerQuality(encodedSizeBytes: Long): Boolean = encodedSizeBytes > MAX_PROCESSED_BYTES
}
