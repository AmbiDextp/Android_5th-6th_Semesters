package com.example.openapitest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.openapitest.data.CatalogRepository
import com.example.openapitest.data.CatalogTtlPolicy
import com.example.openapitest.data.HistoryRepository
import com.example.openapitest.data.RefreshResult
import com.example.openapitest.data.UserdataRepository
import com.example.openapitest.data.settings.SettingsRepository
import com.example.openapitest.domain.CatalogStatusFilter
import com.example.openapitest.domain.FruitFilterState
import com.example.openapitest.domain.FruitStatus
import com.example.openapitest.domain.HistoryType
import com.example.openapitest.domain.SavedFilter
import com.example.openapitest.domain.filterAndSortFruits
import com.example.openapitest.model.Fruit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RefreshUiState(
    val isRefreshing: Boolean = false,
    val initialLoading: Boolean = false,
    val offline: Boolean = false,
    val error: String? = null
)

data class CatalogUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isStale: Boolean = false,
    val offline: Boolean = false,
    val errorMessage: String? = null,
    val query: String = "",
    val filter: FruitFilterState = FruitFilterState(),
    val fruits: List<Fruit> = emptyList(),
    val userStates: Map<Int, com.example.openapitest.domain.FruitUserState> = emptyMap(),
    val fruitCollectionCounts: Map<Int, Int> = emptyMap(),
    val recent: List<Fruit> = emptyList(),
    val savedFilters: List<SavedFilter> = emptyList(),
    val availableTypes: List<String> = emptyList(),
    val lastSyncedAt: Long? = null,
    val totalFruits: Int = 0
)

private data class CatalogAux(
    val savedFilters: List<SavedFilter> = emptyList(),
    val fruitCollectionCounts: Map<Int, Int> = emptyMap(),
    val lastCachedAt: Long? = null,
    val ttlHours: Long? = null
)

private data class CatalogData(
    val fruits: List<Fruit> = emptyList(),
    val userStates: Map<Int, com.example.openapitest.domain.FruitUserState> = emptyMap(),
    val recentIds: List<Int> = emptyList(),
    val aux: CatalogAux = CatalogAux()
)

class CatalogViewModel(
    private val catalogRepository: CatalogRepository,
    private val userdataRepository: UserdataRepository,
    private val historyRepository: HistoryRepository,
    private val settingsRepository: SettingsRepository,
    private val ttlPolicy: CatalogTtlPolicy
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val filterState = MutableStateFlow(FruitFilterState())
    private val refreshState = MutableStateFlow(RefreshUiState())

    private val auxFlow = combine(
        userdataRepository.savedFilters,
        userdataRepository.fruitCollectionCounts,
        catalogRepository.lastCachedAt,
        settingsRepository.settings
    ) { filters, counts, lastCached, settings ->
        CatalogAux(filters, counts, lastCached, settings.ttl.hours)
    }

    private val dataFlow = combine(
        catalogRepository.fruits,
        userdataRepository.userStates,
        historyRepository.recentViewedFruitIds,
        auxFlow
    ) { fruits, userStates, recentIds, aux ->
        CatalogData(fruits, userStates, recentIds, aux)
    }

    val uiState: StateFlow<CatalogUiState> = combine(
        dataFlow,
        query,
        filterState,
        refreshState
    ) { data, query, filter, refresh ->
        val visible = filterAndSortFruits(data.fruits, data.userStates, query, filter)
        val byId = data.fruits.associateBy { it.id }
        CatalogUiState(
            isLoading = refresh.initialLoading && data.fruits.isEmpty(),
            isRefreshing = refresh.isRefreshing,
            isStale = ttlPolicy.isStale(data.aux.lastCachedAt, data.aux.ttlHours),
            offline = refresh.offline && data.fruits.isNotEmpty(),
            errorMessage = if (refresh.error != null && data.fruits.isEmpty()) refresh.error else null,
            query = query,
            filter = filter,
            fruits = visible,
            userStates = data.userStates,
            fruitCollectionCounts = data.aux.fruitCollectionCounts,
            recent = data.recentIds.mapNotNull { byId[it] },
            savedFilters = data.aux.savedFilters,
            availableTypes = data.fruits.map { it.type }.distinct().sorted(),
            lastSyncedAt = data.aux.lastCachedAt,
            totalFruits = data.fruits.size
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, CatalogUiState())

    init {
        viewModelScope.launch {
            val settings = settingsRepository.settings.first()
            val lastCached = catalogRepository.lastCachedAt.first()
            val count = catalogRepository.cachedCount.first()
            when {
                count == 0 -> refresh(initial = true)
                ttlPolicy.isStale(lastCached, settings.ttl.hours) -> refresh()
            }
        }
    }

    fun refresh(initial: Boolean = false) {
        refreshState.value = refreshState.value.copy(
            isRefreshing = !initial,
            initialLoading = initial,
            error = null
        )
        viewModelScope.launch {
            when (val result = catalogRepository.refresh()) {
                is RefreshResult.Success -> refreshState.value = refreshState.value.copy(
                    isRefreshing = false,
                    initialLoading = false,
                    offline = false,
                    error = null
                )

                is RefreshResult.Error -> refreshState.value = refreshState.value.copy(
                    isRefreshing = false,
                    initialLoading = false,
                    offline = result.offline,
                    error = if (result.offline) {
                        "Нет сети. Включите интернет и повторите попытку."
                    } else {
                        "Не удалось загрузить каталог. Попробуйте ещё раз."
                    }
                )
            }
        }
    }

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onStatusFilterChange(status: CatalogStatusFilter) {
        filterState.value = filterState.value.copy(status = status)
    }

    fun onToggleType(type: String) {
        val current = filterState.value.selectedTypes
        filterState.value = filterState.value.copy(
            selectedTypes = if (type in current) current - type else current + type
        )
    }

    fun applyPreset(preset: SavedFilter) {
        query.value = preset.query
        filterState.value = preset.filter
    }

    fun saveCurrentFilterAsPreset(name: String) {
        viewModelScope.launch {
            runCatching {
                userdataRepository.saveFilter(name, query.value, filterState.value)
            }
        }
    }

    fun deletePreset(id: Long) {
        viewModelScope.launch { userdataRepository.deleteFilter(id) }
    }

    fun toggleFavouriteStatus(fruitId: Int) {
        viewModelScope.launch {
            val current = userdataRepository.getUserState(fruitId).status
            val newStatus =
                if (current == FruitStatus.FAVOURITE) FruitStatus.NONE else FruitStatus.FAVOURITE
            userdataRepository.setStatus(fruitId, newStatus)
            historyRepository.record(fruitId, HistoryType.STATUS_SET)
        }
    }

    fun toggleHidden(fruitId: Int) {
        viewModelScope.launch {
            val current = userdataRepository.getUserState(fruitId)
            userdataRepository.setHidden(fruitId, !current.isHidden)
        }
    }
}
