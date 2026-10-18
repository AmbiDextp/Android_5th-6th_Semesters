package com.example.openapitest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.openapitest.data.UserdataRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CollectionsUiState(
    val collections: List<CollectionUiModel> = emptyList(),
    val isLoading: Boolean = true
)

class CollectionsViewModel(
    private val userdataRepository: UserdataRepository
) : ViewModel() {

    val uiState: StateFlow<CollectionsUiState> = userdataRepository.collections
        .map { list -> CollectionsUiState(list.map { it.toUiModel() }, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, CollectionsUiState())

    fun createCollection(name: String, emoji: String) {
        viewModelScope.launch {
            runCatching { userdataRepository.createCollection(name, emoji) }
        }
    }

    fun renameCollection(id: Long, name: String) {
        viewModelScope.launch {
            runCatching { userdataRepository.renameCollection(id, name) }
        }
    }

    fun deleteCollection(id: Long) {
        viewModelScope.launch { userdataRepository.deleteCollection(id) }
    }

    fun togglePinned(id: Long, isPinned: Boolean) {
        viewModelScope.launch { userdataRepository.setCollectionPinned(id, isPinned) }
    }
}
