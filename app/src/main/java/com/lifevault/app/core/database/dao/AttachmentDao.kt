package com.lifevault.app.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lifevault.app.core.database.entity.AttachmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttachmentDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(attachment: AttachmentEntity)

    @Update
    suspend fun update(attachment: AttachmentEntity)

    @Delete
    suspend fun delete(attachment: AttachmentEntity)

    @Query("SELECT * FROM attachments WHERE documentId = :documentId ORDER BY sortOrder ASC")
    fun observeForDocument(documentId: String): Flow<List<AttachmentEntity>>

    @Query("SELECT COUNT(*) FROM attachments WHERE documentId = :documentId")
    suspend fun countForDocument(documentId: String): Int

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM attachments WHERE documentId = :documentId")
    suspend fun maxSortOrder(documentId: String): Int

    @Query("SELECT SUM(encryptedSizeBytes) FROM attachments")
    fun observeTotalEncryptedBytes(): Flow<Long?>

    @Query("SELECT sha256 FROM attachments WHERE documentId = :documentId")
    suspend fun hashesForDocument(documentId: String): List<String>

    /** Section 6.7 orphan sweep: attachment rows whose on-disk file might be missing. */
    @Query("SELECT * FROM attachments")
    suspend fun getAll(): List<AttachmentEntity>
}
