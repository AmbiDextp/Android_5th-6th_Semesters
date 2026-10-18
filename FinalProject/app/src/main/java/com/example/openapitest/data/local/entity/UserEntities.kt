package com.example.openapitest.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.openapitest.domain.CatalogStatusFilter
import com.example.openapitest.domain.FruitFilterState
import com.example.openapitest.domain.FruitStatus
import com.example.openapitest.domain.FruitUserState
import com.example.openapitest.domain.SavedFilter

@Entity(tableName = "collections")
data class CollectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String = "\uD83C\uDF4D",
    val isPinned: Boolean = false,
    val isSystem: Boolean = false,
    val createdAt: Long
)

@Entity(
    tableName = "collection_items",
    primaryKeys = ["collectionId", "fruitId"],
    foreignKeys = [
        ForeignKey(
            entity = CollectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("collectionId"), Index("fruitId")]
)
data class CollectionItemEntity(
    val collectionId: Long,
    val fruitId: Int,
    val addedAt: Long
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val fruitId: Int,
    val body: String,
    val updatedAt: Long
)

@Entity(tableName = "fruit_user_state")
data class FruitUserStateEntity(
    @PrimaryKey val fruitId: Int,
    val status: String,
    val isHidden: Boolean = false,
    val isPinned: Boolean = false
)

fun FruitUserStateEntity.toDomain(): FruitUserState = FruitUserState(
    fruitId = fruitId,
    status = FruitStatus.fromName(status),
    isHidden = isHidden,
    isPinned = isPinned
)

fun FruitUserState.toEntity(): FruitUserStateEntity = FruitUserStateEntity(
    fruitId = fruitId,
    status = status.name,
    isHidden = isHidden,
    isPinned = isPinned
)

@Entity(tableName = "saved_filters")
data class SavedFilterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val query: String,
    val status: String,
    val typesCsv: String,
    val createdAt: Long
)

fun SavedFilterEntity.toDomain(): SavedFilter = SavedFilter(
    id = id,
    name = name,
    query = query,
    filter = FruitFilterState(
        status = CatalogStatusFilter.fromName(status),
        selectedTypes = typesCsv.split(',').filter { it.isNotBlank() }.toSet()
    )
)

@Entity(tableName = "history", indices = [Index("fruitId"), Index("timestamp")])
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fruitId: Int,
    val type: String,
    val timestamp: Long
)
