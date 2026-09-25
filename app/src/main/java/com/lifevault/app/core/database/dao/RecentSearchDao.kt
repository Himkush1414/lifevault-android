package com.lifevault.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lifevault.app.core.database.entity.RecentSearchEntity
import kotlinx.coroutines.flow.Flow

private const val MAX_RECENT_SEARCHES = 8

@Dao
interface RecentSearchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(search: RecentSearchEntity)

    @Query("SELECT * FROM recent_searches ORDER BY searchedAt DESC LIMIT $MAX_RECENT_SEARCHES")
    fun observeRecent(): Flow<List<RecentSearchEntity>>

    @Query("DELETE FROM recent_searches")
    suspend fun clear()

    @Query(
        """
        DELETE FROM recent_searches WHERE query NOT IN (
            SELECT query FROM recent_searches ORDER BY searchedAt DESC LIMIT $MAX_RECENT_SEARCHES
        )
        """,
    )
    suspend fun trimToMax()
}
