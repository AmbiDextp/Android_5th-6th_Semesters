package com.example.openapitest.data

import com.example.openapitest.domain.HistoryType
import com.example.openapitest.fakes.InMemoryFruitDao
import com.example.openapitest.fakes.InMemoryHistoryDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryRepositoryTest {

    private val fruitDao = InMemoryFruitDao()
    private val dao = InMemoryHistoryDao(fruitDao)
    private var now = 1_000L
    private val repository = HistoryRepository(dao) { now }

    @Test
    fun `запись события создаёт элемент`() = runTest {
        repository.recordViewed(1)

        val entries = dao.entries.value
        assertEquals(1, entries.size)
        assertEquals(HistoryType.VIEWED.name, entries.single().type)
        assertEquals(1_000L, entries.single().timestamp)
    }

    @Test
    fun `подряд идущие одинаковые события не дублируются`() = runTest {
        repository.recordViewed(1)
        now = 2_000L
        repository.recordViewed(1)

        assertEquals(1, dao.entries.value.size)
        assertEquals(2_000L, dao.entries.value.single().timestamp)
    }

    @Test
    fun `разные типы событий пишутся отдельно`() = runTest {
        repository.recordViewed(1)
        repository.record(1, HistoryType.STATUS_SET)
        repository.record(1, HistoryType.NOTE_SAVED)

        assertEquals(3, dao.entries.value.size)
    }

    @Test
    fun `недавние отсортированы по последнему просмотру`() = runTest {
        now = 1L
        repository.recordViewed(1)
        now = 2L
        repository.recordViewed(2)
        now = 3L
        repository.recordViewed(1)

        assertEquals(listOf(1, 2), repository.recentViewedFruitIds.first())
    }

    @Test
    fun `очистка удаляет всю историю`() = runTest {
        repository.recordViewed(1)
        repository.record(1, HistoryType.STATUS_SET)

        repository.clear()

        assertTrue(dao.entries.value.isEmpty())
        assertTrue(repository.recentViewedFruitIds.first().isEmpty())
    }
}
