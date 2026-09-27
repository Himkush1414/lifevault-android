package com.lifevault.app.core.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.lifevault.app.core.files.VaultFileStore
import com.lifevault.app.core.repository.AttachmentRepository
import com.lifevault.app.core.repository.DocumentRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

private const val TRASH_RETENTION_DAYS = 30L

/** Section 3.3 S23 / Section 12 step 11: permanently deletes documents 30+ days in Trash. */
@HiltWorker
class TrashPurgeWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val documentRepository: DocumentRepository,
    private val attachmentRepository: AttachmentRepository,
    private val vaultFileStore: VaultFileStore,
    private val clock: Clock,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val cutoff = Instant.now(clock).minus(TRASH_RETENTION_DAYS, ChronoUnit.DAYS)
        val toPurge = documentRepository.trashedOlderThan(cutoff)

        for (document in toPurge) {
            val attachments = attachmentRepository.observeForDocument(document.id).first()
            for (attachment in attachments) {
                vaultFileStore.delete(attachment.id)
                attachmentRepository.delete(attachment)
            }
            documentRepository.permanentlyDelete(document)
        }
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "trash_purge"

        fun ensureScheduled(workManager: WorkManager) {
            val request = PeriodicWorkRequestBuilder<TrashPurgeWorker>(1, TimeUnit.DAYS).build()
            workManager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}
