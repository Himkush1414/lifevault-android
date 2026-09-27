package com.lifevault.app.core.capture

import java.io.File

/** One page from a scan or gallery import (Section 6.1), still plaintext in `cacheDir`. */
data class CapturePage(val id: String, val file: File, val rotationDegrees: Int = 0)

/**
 * Section 6.1: what a capture session holds — either a set of reorderable image pages
 * (scan/gallery) or a single PDF (PDF import; reorder/delete apply to images only, per
 * Section 6.4).
 */
sealed interface CaptureContent {
    data class Images(val pages: List<CapturePage>) : CaptureContent
    data class Pdf(val file: File, val pageCount: Int, val thumbnailFile: File) : CaptureContent
}

/** In-memory + `cacheDir/capture/<sessionId>/` on disk (Section 6.1). */
data class CaptureSession(val sessionId: String, val sessionDir: File, val content: CaptureContent)
