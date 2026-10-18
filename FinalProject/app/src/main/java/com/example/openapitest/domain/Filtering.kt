package com.example.openapitest.domain

import com.example.openapitest.model.Fruit

enum class CatalogStatusFilter(val label: String) {
    ALL("Все"),
    FAVOURITES("Избранные"),
    WANT("Хочу"),
    STUDIED("Изученные"),
    HIDDEN("Скрытые");

    companion object {
        fun fromName(name: String?): CatalogStatusFilter =
            entries.firstOrNull { it.name == name } ?: ALL
    }
}

data class FruitFilterState(
    val status: CatalogStatusFilter = CatalogStatusFilter.ALL,
    val selectedTypes: Set<String> = emptySet()
) {
    val isDefault: Boolean
        get() = status == CatalogStatusFilter.ALL && selectedTypes.isEmpty()
}

data class SavedFilter(
    val id: Long,
    val name: String,
    val query: String,
    val filter: FruitFilterState
)

fun filterAndSortFruits(
    fruits: List<Fruit>,
    userStates: Map<Int, FruitUserState>,
    query: String,
    filter: FruitFilterState
): List<Fruit> {
    val trimmed = query.trim()
    val filtered = fruits.filter { fruit ->
        val state = userStates[fruit.id]
        val statusOk = when (filter.status) {
            CatalogStatusFilter.ALL -> state?.isHidden != true
            CatalogStatusFilter.FAVOURITES -> state?.status == FruitStatus.FAVOURITE
            CatalogStatusFilter.WANT -> state?.status == FruitStatus.WANT
            CatalogStatusFilter.STUDIED -> state?.status == FruitStatus.STUDIED
            CatalogStatusFilter.HIDDEN -> state?.isHidden == true
        }
        val typeOk = filter.selectedTypes.isEmpty() || fruit.type in filter.selectedTypes
        val queryOk = trimmed.isBlank() ||
                fruit.name.contains(trimmed, ignoreCase = true) ||
                fruit.type.contains(trimmed, ignoreCase = true)
        statusOk && typeOk && queryOk
    }
    return filtered.sortedWith(
        compareBy(
            { userStates[it.id]?.isPinned != true },
            { it.name.lowercase() }
        )
    )
}
