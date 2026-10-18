package com.example.openapitest.data.local

import androidx.room.Embedded
import androidx.room.Relation
import com.example.openapitest.data.local.entity.CollectionEntity
import com.example.openapitest.data.local.entity.CollectionItemEntity
import com.example.openapitest.data.local.entity.FruitEntity
import com.example.openapitest.data.local.entity.HistoryEntity

data class CollectionWithItems(
    @Embedded val collection: CollectionEntity,
    @Relation(parentColumn = "id", entityColumn = "collectionId")
    val items: List<CollectionItemEntity>
)

data class HistoryWithFruit(
    @Embedded val history: HistoryEntity,
    @Relation(parentColumn = "fruitId", entityColumn = "id")
    val fruit: FruitEntity?
)

data class RecentFruit(
    val fruitId: Int,
    val lastAt: Long
)

data class FruitCollectionCount(
    val fruitId: Int,
    val count: Int
)
