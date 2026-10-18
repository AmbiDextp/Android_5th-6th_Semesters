package com.example.openapitest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.openapitest.model.Fruit

@Entity(tableName = "fruits")
data class FruitEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val type: String,
    val description: String,
    val romanName: String? = null,
    val img: String? = null,
    val cachedAt: Long
)

fun FruitEntity.toDomain(): Fruit = Fruit(
    id = id,
    name = name,
    type = type,
    description = description,
    img = img
)
