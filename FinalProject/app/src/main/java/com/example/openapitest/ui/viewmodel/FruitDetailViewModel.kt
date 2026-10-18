package com.example.openapitest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.openapitest.data.CatalogRepository
import com.example.openapitest.data.HistoryRepository
import com.example.openapitest.data.UserdataRepository
import com.example.openapitest.domain.FruitStatus
import com.example.openapitest.domain.HistoryType
import com.example.openapitest.model.Fruit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FruitDetailUiState(
    val fruit: Fruit? = null,
    val userState: com.example.openapitest.domain.FruitUserState? = null,
    val note: String = "",
    val noteDirty: Boolean = false,
    val collectionIds: Set<Long> = emptySet(),
    val collections: List<CollectionUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val notFound: Boolean = false
)

class FruitDetailViewModel(
    private val fruitId: Int,
    private val catalogRepository: CatalogRepository,
    private val userdataRepository: UserdataRepository,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val noteDraft = MutableStateFlow<String?>(null)

    private val noteInfo = combine(
        userdataRepository.observeNote(fruitId).map { it?.body },
        noteDraft
    ) { savedNote, draft -> (savedNote ?: "") to draft }

    val uiState: StateFlow<FruitDetailUiState> = combine(
        catalogRepository.observeFruit(fruitId),
        userdataRepository.observeUserState(fruitId),
        noteInfo,
        userdataRepository.collections,
        userdataRepository.observeCollectionIdsForFruit(fruitId)
    ) { fruit, userState, (savedNote, draft), collections, collectionIds ->
        FruitDetailUiState(
            fruit = fruit,
            userState = userState,
            note = draft ?: savedNote,
            noteDirty = draft != null && draft.trim() != savedNote.trim(),
            collectionIds = collectionIds,
            collections = collections.map { it.toUiModel() },
            isLoading = false,
            notFound = fruit == null
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, FruitDetailUiState())

    init {
        viewModelScope.launch { historyRepository.recordViewed(fruitId) }
    }

    fun onNoteChange(text: String) {
        noteDraft.value = text
    }

    fun saveNote() {
        val draft = noteDraft.value ?: return
        viewModelScope.launch {
            userdataRepository.saveNote(fruitId, draft)
            historyRepository.record(fruitId, HistoryType.NOTE_SAVED)
        }
    }

    fun setStatus(status: FruitStatus) {
        viewModelScope.launch {
            userdataRepository.setStatus(fruitId, status)
            if (status != FruitStatus.NONE) {
                historyRepository.record(fruitId, HistoryType.STATUS_SET)
            }
        }
    }

    fun setHidden(hidden: Boolean) {
        viewModelScope.launch { userdataRepository.setHidden(fruitId, hidden) }
    }

    fun setPinned(pinned: Boolean) {
        viewModelScope.launch { userdataRepository.setPinned(fruitId, pinned) }
    }

    fun toggleInCollection(collectionId: Long) {
        viewModelScope.launch {
            val inCollection = collectionId in uiState.value.collectionIds
            if (inCollection) {
                userdataRepository.removeFruitFromCollection(collectionId, fruitId)
                historyRepository.record(fruitId, HistoryType.COLLECTION_REMOVE)
            } else {
                userdataRepository.addFruitToCollection(collectionId, fruitId)
                historyRepository.record(fruitId, HistoryType.COLLECTION_ADD)
            }
        }
    }

    fun createCollectionAndAdd(name: String) {
        viewModelScope.launch {
            runCatching {
                val id = userdataRepository.createCollection(name)
                userdataRepository.addFruitToCollection(id, fruitId)
                historyRepository.record(fruitId, HistoryType.COLLECTION_ADD)
            }
        }
    }
}
