package com.example.openapitest.data.remote

import com.example.openapitest.model.Fruit

data class OneFruitDto(
    val id: Int,
    val name: String,
    val type: String,
    val description: String,
    val roman_name: String?,
    val filename: String?
)

fun OneFruitDto.toDomainOrNull(): Fruit? {
    return Fruit(
        id = this.id,
        name = this.name,
        type = this.type,
        description = this.description,
        img = this.filename
    )
}