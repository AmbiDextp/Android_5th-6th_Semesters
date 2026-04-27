package com.example.openapitest.model

import com.example.openapitest.ui.viewmodel.FruitFilter

data class FruitListUiState(
    val query: String = "",
    val filter: FruitFilter = FruitFilter.ALL,
    val favourites: Set<Int> = emptySet(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)