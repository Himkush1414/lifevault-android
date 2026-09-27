package com.lifevault.app.core.capture

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Section 6.1: manages `cacheDir/capture/<sessionId>/` — plaintext capture temp dirs,
 * discarded on cancel, on successful save, and (via [cleanupStaleSessions]) at app
 * start for anything older than 24 hours.
 */
@Singleton
class CaptureSessionStore @Inject constructor(@ApplicationContext context: Context) {

    private val captureRoot = File(context.cacheDir, "capture").apply { mkdirs() }

    fun newSessionId(): String = UUID.randomUUID().toString()

    fun sessionDir(sessionId: String): File = File(captureRoot, sessionId).apply { mkdirs() }

    fun discard(sessionId: String) {
        File(captureRoot, sessionId).deleteRecursively()
    }

    /**
     * Rebuilds a [CaptureContent] from what's on disk in `sessionDir` — the coordinator
     * writes `page_<n>.jpg` files for a scan/gallery import, or `document.pdf` +
     * `thumb.jpg` for a PDF import, and this is the inverse used by the review screen
     * after crossing the nav boundary.
     */
    fun loadSession(sessionId: String): CaptureContent? {
        val dir = File(captureRoot, sessionId)
        val pdfFile = File(dir, "document.pdf")
        if (pdfFile.exists()) {
            val thumbFile = File(dir, "thumb.jpg")
            val pageCount = android.os.ParcelFileDescriptor
                .open(pdfFile, android.os.ParcelFileDescriptor.MODE_READ_ONLY)
                .use { pfd -> android.graphics.pdf.PdfRenderer(pfd).use { it.pageCount } }
            return CaptureContent.Pdf(pdfFile, pageCount, thumbFile)
        }
        val pages = dir.listFiles { f -> f.name.startsWith("page_") }
            ?.sortedBy { it.name }
            ?.map { CapturePage(id = it.name, file = it) }
            .orEmpty()
        return if (pages.isEmpty()) null else CaptureContent.Images(pages)
    }

    fun cleanupStaleSessions(maxAge: Long = TimeUnit.HOURS.toMillis(24)) {
        val cutoff = System.currentTimeMillis() - maxAge
        captureRoot.listFiles()?.forEach { dir ->
            if (dir.lastModified() < cutoff) dir.deleteRecursively()
        }
    }
}
