package com.example.openapitest.ui.viewmodel

import com.example.openapitest.MainDispatcherRule
import com.example.openapitest.data.CatalogRepository
import com.example.openapitest.data.CatalogTtlPolicy
import com.example.openapitest.data.HistoryRepository
import com.example.openapitest.data.UserdataRepository
import com.example.openapitest.data.settings.TtlOption
import com.example.openapitest.domain.CatalogStatusFilter
import com.example.openapitest.domain.FruitFilterState
import com.example.openapitest.fakes.FakeOneApi
import com.example.openapitest.fakes.FakeSettingsRepository
import com.example.openapitest.fakes.InMemoryCollectionDao
import com.example.openapitest.fakes.InMemoryFruitDao
import com.example.openapitest.fakes.InMemoryHistoryDao
import com.example.openapitest.fakes.InMemoryNoteDao
import com.example.openapitest.fakes.InMemorySavedFilterDao
import com.example.openapitest.fakes.InMemoryUserStateDao
import com.example.openapitest.fakes.fruitDto
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val api = FakeOneApi()
    private val fruitDao = InMemoryFruitDao()
    private val collectionDao = InMemoryCollectionDao(fruitDao)
    private val noteDao = InMemoryNoteDao()
    private val userStateDao = InMemoryUserStateDao()
    private val savedFilterDao = InMemorySavedFilterDao()
    private val historyDao = InMemoryHistoryDao(fruitDao)
    private val settings = FakeSettingsRepository()
    private var now = 10_000L

    private val userdata = UserdataRepository(
        collectionDao, noteDao, userStateDao, savedFilterDao
    ) { now }

    private val history = HistoryRepository(historyDao) { now }

    private fun createViewModel() = CatalogViewModel(
        catalogRepository = CatalogRepository(
            api, fruitDao, clock = { now }, ioDispatcher = Dispatchers.Unconfined
        ),
        userdataRepository = userdata,
        historyRepository = history,
        settingsRepository = settings,
        ttlPolicy = CatalogTtlPolicy { now }
    )

    @Test
    fun `первый запуск с пустым кэшем грузит каталог из сети`() = runTest {
        api.dtos = listOf(fruitDto(1), fruitDto(2), fruitDto(3))

        val vm = createViewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(3, state.totalFruits)
        assertEquals(3, state.fruits.size)
    }

    @Test
    fun `сетевая ошибка при пустом кэше показывает ошибку`() = runTest {
        api.error = IOException("offline")

        val vm = createViewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertNotNull(state.errorMessage)
        assertTrue(state.fruits.isEmpty())
        assertFalse(state.isLoading)
    }

    @Test
    fun `сетевая ошибка при наличии кэша сохраняет данные и включает offline-режим`() = runTest {
        api.dtos = listOf(fruitDto(1), fruitDto(2))
        val vm = createViewModel()
        advanceUntilIdle()

        api.error = IOException("offline")
        vm.refresh()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(2, state.fruits.size)
        assertTrue(state.offline)
        assertNull(state.errorMessage)
    }

    @Test
    fun `устаревший кэш определяется по TTL и сбрасывается обновлением`() = runTest {
        api.dtos = listOf(fruitDto(1))
        val vm = createViewModel()
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isStale)

        now += TtlOption.HOURS_24.hours!! * CatalogTtlPolicy.MILLIS_PER_HOUR + 1

        vm.onStatusFilterChange(CatalogStatusFilter.WANT)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isStale)

        vm.refresh()
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isStale)
    }

    @Test
    fun `фильтр избранного отражает пользовательские статусы`() = runTest {
        api.dtos = listOf(fruitDto(1), fruitDto(2))
        val vm = createViewModel()
        advanceUntilIdle()

        vm.toggleFavouriteStatus(1)
        advanceUntilIdle()
        vm.onStatusFilterChange(CatalogStatusFilter.FAVOURITES)
        advanceUntilIdle()

        assertEquals(listOf(1), vm.uiState.value.fruits.map { it.id })
    }

    @Test
    fun `скрытый плод исчезает из каталога и возвращается через фильтр скрытых`() = runTest {
        api.dtos = listOf(fruitDto(1), fruitDto(2))
        val vm = createViewModel()
        advanceUntilIdle()

        vm.toggleHidden(1)
        advanceUntilIdle()
        assertEquals(listOf(2), vm.uiState.value.fruits.map { it.id })

        vm.onStatusFilterChange(CatalogStatusFilter.HIDDEN)
        advanceUntilIdle()
        assertEquals(listOf(1), vm.uiState.value.fruits.map { it.id })
    }

    @Test
    fun `применение пресета устанавливает запрос и фильтр`() = runTest {
        api.dtos = listOf(fruitDto(1))
        val vm = createViewModel()
        advanceUntilIdle()

        userdata.saveFilter(
            "Изученные Logia",
            "mera",
            FruitFilterState(status = CatalogStatusFilter.STUDIED, selectedTypes = setOf("Logia"))
        )
        advanceUntilIdle()
        val preset = vm.uiState.value.savedFilters.single()

        vm.applyPreset(preset)
        advanceUntilIdle()

        assertEquals("mera", vm.uiState.value.query)
        assertEquals(CatalogStatusFilter.STUDIED, vm.uiState.value.filter.status)
        assertEquals(setOf("Logia"), vm.uiState.value.filter.selectedTypes)
    }

    @Test
    fun `недавние плоды попадают на главный экран после просмотра`() = runTest {
        api.dtos = listOf(fruitDto(1), fruitDto(2))
        val vm = createViewModel()
        advanceUntilIdle()

        history.recordViewed(2)
        advanceUntilIdle()

        assertEquals(listOf(2), vm.uiState.value.recent.map { it.id })
    }
}
