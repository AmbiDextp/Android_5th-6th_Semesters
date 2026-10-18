package com.example.openapitest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.openapitest.data.UserdataRepository
import com.example.openapitest.model.Fruit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CollectionDetailUiState(
    val collection: CollectionUiModel? = null,
    val fruits: List<Fruit> = emptyList(),
    val isLoading: Boolean = true,
    val notFound: Boolean = false
)

class CollectionDetailViewModel(
    private val collectionId: Long,
    private val userdataRepository: UserdataRepository
) : ViewModel() {

    val uiState: StateFlow<CollectionDetailUiState> = combine(
        userdataRepository.collections,
        userdataRepository.observeFruitsInCollection(collectionId)
    ) { all, fruits ->
        val withItems = all.firstOrNull { it.collection.id == collectionId }
        CollectionDetailUiState(
            collection = withItems?.toUiModel(),
            fruits = fruits,
            isLoading = false,
            notFound = withItems == null
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, CollectionDetailUiState())

    fun removeFruit(fruitId: Int) {
        viewModelScope.launch {
            userdataRepository.removeFruitFromCollection(collectionId, fruitId)
        }
    }

    fun rename(name: String) {
        viewModelScope.launch {
            runCatching { userdataRepository.renameCollection(collectionId, name) }
        }
    }

    fun setPinned(pinned: Boolean) {
        viewModelScope.launch {
            userdataRepository.setCollectionPinned(collectionId, pinned)
        }
    }

    fun delete() {
        viewModelScope.launch {
            userdataRepository.deleteCollection(collectionId)
        }
    }
}
