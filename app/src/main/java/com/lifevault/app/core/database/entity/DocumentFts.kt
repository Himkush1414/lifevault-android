package com.lifevault.app.core.database.entity

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions

@Fts4(contentEntity = DocumentEntity::class, tokenizer = FtsOptions.TOKENIZER_UNICODE61)
@Entity(tableName = "documents_fts")
data class DocumentFts(
    val title: String,
    val documentNumber: String?,
    val issuer: String?,
    val notes: String?,
    val ocrText: String?,
)
