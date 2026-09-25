package com.lifevault.app.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lifevault.app.core.domain.model.AttachmentKind
import java.time.Instant

@Entity(
    tableName = "attachments",
    foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("documentId"), Index(value = ["documentId", "sortOrder"])],
)
data class AttachmentEntity(
    @PrimaryKey val id: String, // UUID, also the on-disk file stem
    val documentId: String,
    val kind: AttachmentKind,
    val mimeType: String, // image/webp, image/jpeg, application/pdf
    val originalFileName: String?, // display only, sanitised
    val sizeBytes: Long, // plaintext size
    val encryptedSizeBytes: Long,
    val sha256: String, // hex of plaintext; integrity + dedupe
    val widthPx: Int?,
    val heightPx: Int?, // images
    val pageCount: Int?, // PDFs
    val rotationDegrees: Int, // 0/90/180/270 view rotation
    val sortOrder: Int,
    val createdAt: Instant,
)
