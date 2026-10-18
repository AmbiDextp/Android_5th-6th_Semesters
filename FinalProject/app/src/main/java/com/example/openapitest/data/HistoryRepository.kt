package com.example.openapitest.data

import com.example.openapitest.data.local.HistoryWithFruit
import com.example.openapitest.data.local.dao.HistoryDao
import com.example.openapitest.data.local.entity.HistoryEntity
import com.example.openapitest.domain.HistoryType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HistoryRepository(
    private val historyDao: HistoryDao,
    private val clock: () -> Long = System::currentTimeMillis
) {

    val entries: Flow<List<HistoryWithFruit>> = historyDao.observeAllWithFruit()

    val entriesCount: Flow<Int> = historyDao.observeCount()

    val recentViewedFruitIds: Flow<List<Int>> =
        historyDao.observeRecentViewed().map { list -> list.map { it.fruitId } }

    suspend fun record(fruitId: Int, type: HistoryType) {
        val last = historyDao.last()
        if (last != null && last.fruitId == fruitId && last.type == type.name) {
            historyDao.update(last.copy(timestamp = clock()))
        } else {
            historyDao.insert(
                HistoryEntity(fruitId = fruitId, type = type.name, timestamp = clock())
            )
        }
    }

    suspend fun recordViewed(fruitId: Int) = record(fruitId, HistoryType.VIEWED)

    suspend fun clear() = historyDao.clear()
}
