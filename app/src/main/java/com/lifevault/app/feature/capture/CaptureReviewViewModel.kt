package com.lifevault.app.feature.capture

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.lifevault.app.core.capture.CaptureContent
import com.lifevault.app.core.capture.CaptureSessionStore
import com.lifevault.app.core.database.entity.AttachmentEntity
import com.lifevault.app.core.database.entity.DocumentEntity
import com.lifevault.app.core.domain.feature.FeatureGate
import com.lifevault.app.core.domain.model.AttachmentKind
import com.lifevault.app.core.domain.reminder.Recurrence
import com.lifevault.app.core.files.ImageProcessor
import com.lifevault.app.core.files.VaultFileStore
import com.lifevault.app.core.navigation.Routes
import com.lifevault.app.core.repository.AttachmentRepository
import com.lifevault.app.core.repository.CategoryRepository
import com.lifevault.app.core.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

data class ReviewPage(val id: String, val file: File, val rotationDegrees: Int = 0)
data class PdfSummary(val file: File, val pageCount: Int, val thumbnailFile: File)

data class CaptureReviewUiState(
    val isLoading: Boolean = true,
    val pages: List<ReviewPage> = emptyList(),
    val pdf: PdfSummary? = null,
    // TODO(step 21 - billing): isPro from the real entitlement instead of always false.
    val isPro: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val savedDocumentId: String? = null,
) {
    val fileCount: Int get() = if (pdf != null) 1 else pages.size
    val overLimit: Boolean get() = !FeatureGate.canAddFile(fileCount - 1, isPro)
    val canSave: Boolean get() = !isLoading && !isSaving && fileCount > 0 && !overLimit
}

/**
 * S12 (Section 6.2–6.4, 6.6, 6.7): review/reorder/rotate/delete captured pages, then the
 * save pipeline — process (images only; a PDF is stored as-is) → encrypt into
 * [VaultFileStore] → one attachment row per page → discard the plaintext capture dir.
 */
@HiltViewModel
class CaptureReviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sessionStore: CaptureSessionStore,
    private val imageProcessor: ImageProcessor,
    private val vaultFileStore: VaultFileStore,
    private val documentRepository: DocumentRepository,
    private val categoryRepository: CategoryRepository,
    private val attachmentRepository: AttachmentRepository,
) : ViewModel() {

    private val sessionId = savedStateHandle.toRoute<Routes.CaptureReview>().sessionId

    private val _state = MutableStateFlow(CaptureReviewUiState())
    val state: StateFlow<CaptureReviewUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val content = withContext(Dispatchers.IO) { sessionStore.loadSession(sessionId) }
            _state.update {
                when (content) {
                    is CaptureContent.Images -> it.copy(
                        isLoading = false,
                        pages = content.pages.map { p -> ReviewPage(p.id, p.file, p.rotationDegrees) },
                    )
                    is CaptureContent.Pdf -> it.copy(
                        isLoading = false,
                        pdf = PdfSummary(content.file, content.pageCount, content.thumbnailFile),
                    )
                    null -> it.copy(isLoading = false, error = "Nothing to review — capture this again.")
                }
            }
        }
    }

    fun rotate(pageId: String) = _state.update { s ->
        s.copy(
            pages = s.pages.map {
                if (it.id == pageId) it.copy(rotationDegrees = (it.rotationDegrees + 90) % 360) else it
            },
        )
    }

    fun delete(pageId: String) = _state.update { s -> s.copy(pages = s.pages.filterNot { it.id == pageId }) }

    fun reorder(fromIndex: Int, toIndex: Int) = _state.update { s ->
        s.copy(pages = s.pages.toMutableList().apply { add(toIndex, removeAt(fromIndex)) })
    }

    /** Section 6.1: cancelling leaves no temp files. */
    fun cancel() {
        sessionStore.discard(sessionId)
    }

    fun save() {
        val current = _state.value
        if (!current.canSave) return
        _state.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val documentId = withContext(Dispatchers.IO) { persist(current) }
            sessionStore.discard(sessionId)
            _state.update { it.copy(isSaving = false, savedDocumentId = documentId) }
        }
    }

    private suspend fun persist(current: CaptureReviewUiState): String {
        val categories = categoryRepository.observeAll().first()
        val defaultCategoryId = categories.firstOrNull { it.id == "cat_other" }?.id ?: categories.first().id

        val document = documentRepository.create { id, now ->
            DocumentEntity(
                id = id,
                title = "Untitled document",
                categoryId = defaultCategoryId,
                documentNumber = null,
                issuer = null,
                issueDate = null,
                expiryDate = null,
                notes = null,
                recurrence = Recurrence.NONE,
                recurrenceIntervalYears = null,
                renewalCostMinor = null,
                renewalCurrency = null,
                overdueNudges = false,
                lastOverdueNudgeAt = null,
                isFavorite = false,
                ocrText = null,
                createdAt = now,
                updatedAt = now,
                deletedAt = null,
            )
        }

        val now = document.createdAt
        val pdf = current.pdf
        if (pdf != null) {
            persistPdf(document.id, pdf, now)
        } else {
            current.pages.forEachIndexed { index, page -> persistImage(document.id, page, index, now) }
        }
        return document.id
    }

    private suspend fun persistImage(documentId: String, page: ReviewPage, sortOrder: Int, now: Instant) {
        val processed = imageProcessor.process(page.file.readBytes())
        val attachmentId = UUID.randomUUID().toString()
        val encryptedSizeBytes = vaultFileStore.writeEncrypted(attachmentId, processed.fullBytes)
        vaultFileStore.writeEncryptedThumb(attachmentId, processed.thumbBytes)
        attachmentRepository.add(
            AttachmentEntity(
                id = attachmentId,
                documentId = documentId,
                kind = AttachmentKind.IMAGE,
                mimeType = processed.mimeType,
                originalFileName = null,
                sizeBytes = processed.fullBytes.size.toLong(),
                encryptedSizeBytes = encryptedSizeBytes,
                sha256 = processed.sha256Hex,
                widthPx = processed.widthPx,
                heightPx = processed.heightPx,
                pageCount = null,
                // Section 6.7: baked-in image bytes are never re-rotated — this is a view-time hint.
                rotationDegrees = page.rotationDegrees,
                sortOrder = sortOrder,
                createdAt = now,
            ),
        )
    }

    private suspend fun persistPdf(documentId: String, pdf: PdfSummary, now: Instant) {
        val pdfBytes = pdf.file.readBytes()
        val thumbBytes = pdf.thumbnailFile.readBytes()
        val attachmentId = UUID.randomUUID().toString()
        val encryptedSizeBytes = vaultFileStore.writeEncrypted(attachmentId, pdfBytes)
        vaultFileStore.writeEncryptedThumb(attachmentId, thumbBytes)
        attachmentRepository.add(
            AttachmentEntity(
                id = attachmentId,
                documentId = documentId,
                kind = AttachmentKind.PDF,
                mimeType = "application/pdf",
                originalFileName = null,
                sizeBytes = pdfBytes.size.toLong(),
                encryptedSizeBytes = encryptedSizeBytes,
                sha256 = sha256Hex(pdfBytes),
                widthPx = null,
                heightPx = null,
                pageCount = pdf.pageCount,
                rotationDegrees = 0,
                sortOrder = 0,
                createdAt = now,
            ),
        )
    }

    private fun sha256Hex(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
}
