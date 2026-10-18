package com.example.openapitest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.openapitest.data.CatalogRepository
import com.example.openapitest.data.CatalogTtlPolicy
import com.example.openapitest.data.HistoryRepository
import com.example.openapitest.data.UserdataRepository
import com.example.openapitest.data.settings.AppSettings
import com.example.openapitest.data.settings.SettingsRepository
import com.example.openapitest.data.settings.ThemeMode
import com.example.openapitest.data.settings.TtlOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LocalDataStats(
    val notesCount: Int = 0,
    val collectionsCount: Int = 0,
    val presetsCount: Int = 0,
    val historyCount: Int = 0
)

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val cachedFruits: Int = 0,
    val lastSyncedAt: Long? = null,
    val isStale: Boolean = false,
    val stats: LocalDataStats = LocalDataStats()
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val catalogRepository: CatalogRepository,
    private val userdataRepository: UserdataRepository,
    private val historyRepository: HistoryRepository,
    private val ttlPolicy: CatalogTtlPolicy
) : ViewModel() {

    private val statsFlow = combine(
        userdataRepository.notesCount,
        userdataRepository.collectionsCount,
        userdataRepository.savedFiltersCount,
        historyRepository.entriesCount
    ) { notes, collections, presets, history ->
        LocalDataStats(notes, collections, presets, history)
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.settings,
        catalogRepository.cachedCount,
        catalogRepository.lastCachedAt,
        statsFlow
    ) { settings, cached, lastSynced, stats ->
        SettingsUiState(
            settings = settings,
            cachedFruits = cached,
            lastSyncedAt = lastSynced,
            isStale = ttlPolicy.isStale(lastSynced, settings.ttl.hours),
            stats = stats
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SettingsUiState())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun setTheme(theme: ThemeMode) {
        viewModelScope.launch { settingsRepository.setTheme(theme) }
    }

    fun setTtl(ttl: TtlOption) {
        viewModelScope.launch { settingsRepository.setTtl(ttl) }
    }

    fun setAutoSync(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setAutoSync(enabled) }
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyRepository.clear()
            _message.value = "История очищена"
        }
    }

    fun clearUnreferencedCache() {
        viewModelScope.launch {
            val deleted = catalogRepository.clearUnreferencedCache()
            _message.value = "Из кэша удалено плодов: $deleted"
        }
    }

    fun consumeMessage() {
        _message.value = null
    }
}
