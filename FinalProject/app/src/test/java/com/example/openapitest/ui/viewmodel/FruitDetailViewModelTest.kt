package com.example.openapitest.ui.viewmodel

import com.example.openapitest.MainDispatcherRule
import com.example.openapitest.data.CatalogRepository
import com.example.openapitest.data.HistoryRepository
import com.example.openapitest.data.UserdataRepository
import com.example.openapitest.domain.FruitStatus
import com.example.openapitest.domain.HistoryType
import com.example.openapitest.fakes.FakeOneApi
import com.example.openapitest.fakes.InMemoryCollectionDao
import com.example.openapitest.fakes.InMemoryFruitDao
import com.example.openapitest.fakes.InMemoryHistoryDao
import com.example.openapitest.fakes.InMemoryNoteDao
import com.example.openapitest.fakes.InMemorySavedFilterDao
import com.example.openapitest.fakes.InMemoryUserStateDao
import com.example.openapitest.fakes.fruitEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FruitDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val api = FakeOneApi()
    private val fruitDao = InMemoryFruitDao()
    private val collectionDao = InMemoryCollectionDao(fruitDao)
    private val noteDao = InMemoryNoteDao()
    private val userStateDao = InMemoryUserStateDao()
    private val savedFilterDao = InMemorySavedFilterDao()
    private val historyDao = InMemoryHistoryDao(fruitDao)
    private var now = 1_000L

    private val catalog = CatalogRepository(
        api, fruitDao, clock = { now }, ioDispatcher = Dispatchers.Unconfined
    )
    private val userdata = UserdataRepository(
        collectionDao, noteDao, userStateDao, savedFilterDao
    ) { now }
    private val history = HistoryRepository(historyDao) { now }

    private fun createViewModel(fruitId: Int) =
        FruitDetailViewModel(fruitId, catalog, userdata, history)

    @Test
    fun `открытие плода записывает просмотр в историю`() = runTest {
        fruitDao.upsertAll(listOf(fruitEntity(7)))

        val vm = createViewModel(7)
        advanceUntilIdle()

        assertEquals("Fruit 7", vm.uiState.value.fruit?.name)
        assertFalse(vm.uiState.value.isLoading)

        val entries = historyDao.entries.value
        assertEquals(1, entries.size)
        assertEquals(HistoryType.VIEWED.name, entries.single().type)
        assertEquals(7, entries.single().fruitId)
    }

    @Test
    fun `несуществующий плод помечается как не найденный`() = runTest {
        val vm = createViewModel(404)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.notFound)
    }

    @Test
    fun `плод добавляется и удаляется из коллекции с записью истории`() = runTest {
        fruitDao.upsertAll(listOf(fruitEntity(1)))
        val vm = createViewModel(1)
        advanceUntilIdle()

        val collectionId = userdata.createCollection("Избранное")
        vm.toggleInCollection(collectionId)
        advanceUntilIdle()
        assertEquals(setOf(collectionId), vm.uiState.value.collectionIds)

        vm.toggleInCollection(collectionId)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.collectionIds.isEmpty())

        val types = historyDao.entries.value.map { it.type }
        assertTrue(HistoryType.COLLECTION_ADD.name in types)
        assertTrue(HistoryType.COLLECTION_REMOVE.name in types)
    }

    @Test
    fun `черновик заметки делает кнопку активной, сохранение пишет в БД`() = runTest {
        fruitDao.upsertAll(listOf(fruitEntity(1)))
        val vm = createViewModel(1)
        advanceUntilIdle()

        vm.onNoteChange("мои мысли")
        advanceUntilIdle()
        assertTrue(vm.uiState.value.noteDirty)

        vm.saveNote()
        advanceUntilIdle()

        assertEquals("мои мысли", noteDao.notes.value.single().body)
        assertFalse(vm.uiState.value.noteDirty)
        assertEquals("мои мысли", vm.uiState.value.note)
        assertEquals(
            HistoryType.NOTE_SAVED.name,
            historyDao.entries.value.lastOrNull { it.fruitId == 1 }?.type
        )
    }

    @Test
    fun `смена статуса обновляет состояние плода`() = runTest {
        fruitDao.upsertAll(listOf(fruitEntity(1)))
        val vm = createViewModel(1)
        advanceUntilIdle()

        vm.setStatus(FruitStatus.WANT)
        advanceUntilIdle()

        assertEquals(FruitStatus.WANT, vm.uiState.value.userState?.status)
    }
}
