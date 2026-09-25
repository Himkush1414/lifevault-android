package com.lifevault.app.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.lifevault.app.core.database.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {

    /** Single-row table (Section 5.3); the seed callback inserts row id 1 on create. */
    @Query("SELECT * FROM settings WHERE id = 1")
    fun observe(): Flow<SettingsEntity>

    @Update
    suspend fun update(settings: SettingsEntity)
}
