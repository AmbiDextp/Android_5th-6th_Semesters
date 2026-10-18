package com.example.openapitest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.openapitest.data.HistoryRepository
import com.example.openapitest.domain.HistoryType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class HistoryUiItem(
    val id: Long,
    val type: HistoryType,
    val fruitId: Int,
    val fruitName: String?,
    val timestamp: Long
)

data class HistoryGroup(
    val title: String,
    val items: List<HistoryUiItem>
)

data class HistoryUiState(
    val groups: List<HistoryGroup> = emptyList(),
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false
)

class HistoryViewModel(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = historyRepository.entries
        .map { entries ->
            val items = entries.map { entry ->
                HistoryUiItem(
                    id = entry.history.id,
                    type = HistoryType.fromName(entry.history.type),
                    fruitId = entry.history.fruitId,
                    fruitName = entry.fruit?.name,
                    timestamp = entry.history.timestamp
                )
            }
            HistoryUiState(
                groups = groupHistoryByDay(items),
                isLoading = false,
                isEmpty = items.isEmpty()
            )
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, HistoryUiState())

    fun clearHistory() {
        viewModelScope.launch { historyRepository.clear() }
    }
}

fun groupHistoryByDay(
    items: List<HistoryUiItem>,
    zone: ZoneId = ZoneId.systemDefault(),
    today: LocalDate = LocalDate.now(zone)
): List<HistoryGroup> {
    if (items.isEmpty()) return emptyList()
    return items
        .groupBy { Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate() }
        .map { (date, dayItems) -> HistoryGroup(dayTitle(date, today), dayItems) }
}

private val dayFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("ru"))

private fun dayTitle(date: LocalDate, today: LocalDate): String = when (date) {
    today -> "Сегодня"
    today.minusDays(1) -> "Вчера"
    else -> date.format(dayFormatter)
}
