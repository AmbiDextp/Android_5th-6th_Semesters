package com.example.openapitest.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.openapitest.data.local.entity.FruitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FruitDao {

    @Query("SELECT * FROM fruits ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<FruitEntity>>

    @Query("SELECT * FROM fruits WHERE id = :id")
    fun observeById(id: Int): Flow<FruitEntity?>

    @Query("SELECT MAX(cachedAt) FROM fruits")
    fun observeLastCachedAt(): Flow<Long?>

    @Query("SELECT COUNT(*) FROM fruits")
    fun observeCount(): Flow<Int>

    @Upsert
    suspend fun upsertAll(fruits: List<FruitEntity>)

    @Query("SELECT COUNT(*) FROM fruits")
    suspend fun count(): Int

    @Query("SELECT MAX(cachedAt) FROM fruits")
    suspend fun lastCachedAt(): Long?

    @Query(
        """DELETE FROM fruits WHERE id NOT IN (
            SELECT fruitId FROM collection_items
            UNION SELECT fruitId FROM notes
            UNION SELECT fruitId FROM fruit_user_state
            UNION SELECT fruitId FROM history)"""
    )
    suspend fun deleteUnreferenced(): Int
}
