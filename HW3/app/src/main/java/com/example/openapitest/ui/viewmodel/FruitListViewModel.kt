package com.example.openapitest.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.openapitest.data.FruitRepository
import com.example.openapitest.model.Fruit
import com.example.openapitest.model.FruitListUiState
import kotlinx.coroutines.launch

enum class FruitFilter {
    ALL, FAVOURITES
}

class FruitListViewModel : ViewModel() {

    private val repository = FruitRepository()

    var uiState by mutableStateOf(FruitListUiState())
        private set

    private var allFruits by mutableStateOf(emptyList<Fruit>())

    init {
        loadFruits()
    }

    fun fruitById(id: Int): Fruit? = allFruits.firstOrNull { it.id == id }

    fun loadFruits() {
        uiState = uiState.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                allFruits = repository.fruits()
                uiState = uiState.copy(isLoading = false)
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Что-то пошло не так"
                )
            }
        }
    }

    fun onQueryChange(query: String) {
        uiState = uiState.copy(query = query, errorMessage = null)
    }

    fun onFilterChange(filter: FruitFilter) {
        uiState = uiState.copy(filter = filter)
    }

    fun onToggleFavourite(id: Int) {
        val favourites = uiState.favourites
        uiState = uiState.copy(
            favourites = if (id in favourites) favourites-id else favourites+id
        )
    }

    val visibleFruits: List<Fruit>
        get() {
            val filtered = when (uiState.filter) {
                FruitFilter.ALL -> allFruits
                FruitFilter.FAVOURITES -> allFruits.filter { it.id in uiState.favourites }
            }

            return if (uiState.query.isBlank()) filtered
            else filtered.filter { fruit ->
                    fruit.name.contains(uiState.query, ignoreCase = true) }
        }
}