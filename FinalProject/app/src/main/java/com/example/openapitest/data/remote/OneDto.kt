package com.example.openapitest.data.remote

import com.example.openapitest.data.local.entity.FruitEntity

data class OneFruitDto(
    val id: Int?,
    val name: String?,
    val type: String?,
    val description: String?,
    val roman_name: String?,
    val filename: String?
)

fun OneFruitDto.toEntity(cachedAt: Long): FruitEntity? {
    val safeId = id ?: return null
    val safeName = name?.trim().orEmpty()
    if (safeName.isEmpty()) return null
    return FruitEntity(
        id = safeId,
        name = safeName,
        type = type?.trim().orEmpty().ifEmpty { "Unknown" },
        description = description.orEmpty(),
        romanName = roman_name,
        img = filename,
        cachedAt = cachedAt
    )
}
