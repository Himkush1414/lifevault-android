package com.lifevault.app.feature.capture

import android.content.ContentResolver
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.core.graphics.createBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifevault.app.core.capture.CaptureContent
import com.lifevault.app.core.capture.CapturePage
import com.lifevault.app.core.capture.CaptureSession
import com.lifevault.app.core.capture.CaptureSessionStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

private const val MAX_IMPORT_SIZE_BYTES = 50 * 1024 * 1024L
private const val PDF_THUMBNAIL_WIDTH_PX = 1024
private const val PDF_UNREADABLE_MESSAGE =
    "This PDF is password-protected or damaged. Remove the password in the source app and try again."

sealed interface CaptureImportResult {
    data class Success(val session: CaptureSession) : CaptureImportResult
    data class Failure(val message: String) : CaptureImportResult
}

/**
 * Section 6.2–6.4: turns scanner/gallery/PDF results into a [CaptureSession] — copying
 * (scanner/gallery) or streaming (PDF) into `cacheDir/capture/<sessionId>/` so the
 * originals never need to stay reachable after the picker/scanner activity finishes.
 */
@HiltViewModel
class CaptureCoordinatorViewModel @Inject constructor(
    private val sessionStore: CaptureSessionStore,
) : ViewModel() {

    private val _result = MutableStateFlow<CaptureImportResult?>(null)
    val result: StateFlow<CaptureImportResult?> = _result.asStateFlow()

    fun consumeResult() {
        _result.value = null
    }

    /** Section 6.2 step 6 / 6.3 step 2-3: scanner and gallery results both land here as image URIs. */
    fun importImages(contentResolver: ContentResolver, uris: List<Uri>) {
        viewModelScope.launch {
            _result.value = withContext(Dispatchers.IO) { tryImportImages(contentResolver, uris) }
        }
    }

    // A bad content:// URI from a third-party app/scanner surfaces as IOException,
    // SecurityException or (via the `error(...)` below) IllegalStateException — all of
    // them collapse to the same user-facing message, so one broad catch is deliberate.
    @Suppress("TooGenericExceptionCaught")
    private fun tryImportImages(contentResolver: ContentResolver, uris: List<Uri>): CaptureImportResult = try {
        val sessionId = sessionStore.newSessionId()
        val sessionDir = sessionStore.sessionDir(sessionId)
        val pages = uris.mapIndexed { index, uri ->
            val dest = File(sessionDir, "page_$index.jpg")
            contentResolver.openInputStream(uri)?.use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            } ?: error("Unable to read $uri")
            CapturePage(id = dest.name, file = dest)
        }
        CaptureImportResult.Success(CaptureSession(sessionId, sessionDir, CaptureContent.Images(pages)))
    } catch (e: Exception) {
        CaptureImportResult.Failure(e.message ?: "Unable to import the selected images")
    }

    /** Section 6.4: validates size and openability, then streams the PDF as-is (no re-compression). */
    fun importPdf(contentResolver: ContentResolver, uri: Uri) {
        viewModelScope.launch {
            _result.value = withContext(Dispatchers.IO) {
                val size = contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
                if (size > MAX_IMPORT_SIZE_BYTES) {
                    CaptureImportResult.Failure("This PDF is larger than 50 MB.")
                } else {
                    tryImportValidPdf(contentResolver, uri)
                }
            }
        }
    }

    // Both a locked and a corrupt PDF read back as "unreadable" to the user — there's no
    // more specific action either exception enables, so neither is worth surfacing.
    @Suppress("SwallowedException")
    private fun tryImportValidPdf(contentResolver: ContentResolver, uri: Uri): CaptureImportResult =
        try {
            importValidPdf(contentResolver, uri)
        } catch (e: SecurityException) {
            CaptureImportResult.Failure(PDF_UNREADABLE_MESSAGE)
        } catch (e: java.io.IOException) {
            CaptureImportResult.Failure(PDF_UNREADABLE_MESSAGE)
        }

    private fun importValidPdf(contentResolver: ContentResolver, uri: Uri): CaptureImportResult {
        val sessionId = sessionStore.newSessionId()
        val sessionDir = sessionStore.sessionDir(sessionId)
        val pdfFile = File(sessionDir, "document.pdf")
        contentResolver.openInputStream(uri)?.use { input ->
            pdfFile.outputStream().use { output -> input.copyTo(output) }
        } ?: return CaptureImportResult.Failure("Unable to read the selected PDF.")

        val (pageCount, thumbnailFile) = renderPdfThumbnail(pdfFile, sessionDir)
        return CaptureImportResult.Success(
            CaptureSession(sessionId, sessionDir, CaptureContent.Pdf(pdfFile, pageCount, thumbnailFile)),
        )
    }

    private fun renderPdfThumbnail(pdfFile: File, sessionDir: File): Pair<Int, File> {
        val pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
        return pfd.use {
            PdfRenderer(pfd).use { renderer ->
                val thumbFile = File(sessionDir, "thumb.jpg")
                renderer.openPage(0).use { page -> renderPageToJpeg(page, thumbFile) }
                renderer.pageCount to thumbFile
            }
        }
    }

    private fun renderPageToJpeg(page: PdfRenderer.Page, outFile: File) {
        val scale = PDF_THUMBNAIL_WIDTH_PX.toFloat() / page.width
        val bitmap = createBitmap(PDF_THUMBNAIL_WIDTH_PX, (page.height * scale).toInt())
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        outFile.outputStream().use { out -> bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, out) }
        bitmap.recycle()
    }
}
