package com.example.openapitest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.openapitest.data.local.HistoryWithFruit
import com.example.openapitest.data.local.RecentFruit
import com.example.openapitest.data.local.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Transaction
    @Query("SELECT * FROM history ORDER BY timestamp DESC LIMIT 500")
    fun observeAllWithFruit(): Flow<List<HistoryWithFruit>>

    @Query("SELECT COUNT(*) FROM history")
    fun observeCount(): Flow<Int>

    @Query(
        """SELECT fruitId, MAX(timestamp) AS lastAt FROM history
           WHERE type = 'VIEWED'
           GROUP BY fruitId
           ORDER BY lastAt DESC
           LIMIT 5"""
    )
    fun observeRecentViewed(): Flow<List<RecentFruit>>

    @Query("SELECT * FROM history ORDER BY timestamp DESC LIMIT 1")
    suspend fun last(): HistoryEntity?

    @Insert
    suspend fun insert(entry: HistoryEntity): Long

    @Update
    suspend fun update(entry: HistoryEntity)

    @Query("DELETE FROM history")
    suspend fun clear()
}
