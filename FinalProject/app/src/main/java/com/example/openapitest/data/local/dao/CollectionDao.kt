package com.example.openapitest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.openapitest.data.local.CollectionWithItems
import com.example.openapitest.data.local.FruitCollectionCount
import com.example.openapitest.data.local.entity.CollectionEntity
import com.example.openapitest.data.local.entity.CollectionItemEntity
import com.example.openapitest.data.local.entity.FruitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {

    @Transaction
    @Query("SELECT * FROM collections ORDER BY isSystem DESC, isPinned DESC, createdAt DESC")
    fun observeCollectionsWithItems(): Flow<List<CollectionWithItems>>

    @Query("SELECT * FROM collections WHERE id = :id")
    fun observeById(id: Long): Flow<CollectionEntity?>

    @Query("SELECT * FROM collections WHERE id = :id")
    suspend fun getById(id: Long): CollectionEntity?

    @Query("SELECT * FROM collections WHERE isSystem = 1 LIMIT 1")
    suspend fun getSystemCollection(): CollectionEntity?

    @Query("SELECT COUNT(*) FROM collections")
    fun observeCount(): Flow<Int>

    @Insert
    suspend fun insert(collection: CollectionEntity): Long

    @Query("UPDATE collections SET name = :name WHERE id = :id")
    suspend fun rename(id: Long, name: String)

    @Query("UPDATE collections SET isPinned = :isPinned WHERE id = :id")
    suspend fun setPinned(id: Long, isPinned: Boolean)

    @Query("DELETE FROM collections WHERE id = :id")
    suspend fun delete(id: Long)

    @Upsert
    suspend fun addItem(item: CollectionItemEntity)

    @Query("DELETE FROM collection_items WHERE collectionId = :collectionId AND fruitId = :fruitId")
    suspend fun removeItem(collectionId: Long, fruitId: Int)

    @Query("SELECT * FROM collection_items WHERE collectionId = :collectionId AND fruitId = :fruitId LIMIT 1")
    suspend fun findItem(collectionId: Long, fruitId: Int): CollectionItemEntity?

    @Query("SELECT * FROM collection_items WHERE fruitId = :fruitId")
    fun observeItemsForFruit(fruitId: Int): Flow<List<CollectionItemEntity>>

    @Query(
        """SELECT f.* FROM fruits f
           INNER JOIN collection_items ci ON ci.fruitId = f.id
           WHERE ci.collectionId = :collectionId
           ORDER BY ci.addedAt DESC"""
    )
    fun observeFruitsInCollection(collectionId: Long): Flow<List<FruitEntity>>

    @Query("SELECT fruitId, COUNT(collectionId) AS count FROM collection_items GROUP BY fruitId")
    fun observeFruitCounts(): Flow<List<FruitCollectionCount>>
}
