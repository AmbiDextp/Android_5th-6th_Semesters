package com.example.openapitest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.openapitest.data.local.entity.SavedFilterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedFilterDao {

    @Query("SELECT * FROM saved_filters ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<SavedFilterEntity>>

    @Query("SELECT COUNT(*) FROM saved_filters")
    fun observeCount(): Flow<Int>

    @Insert
    suspend fun insert(filter: SavedFilterEntity): Long

    @Query("UPDATE saved_filters SET name = :name WHERE id = :id")
    suspend fun rename(id: Long, name: String)

    @Query("DELETE FROM saved_filters WHERE id = :id")
    suspend fun delete(id: Long)
}
