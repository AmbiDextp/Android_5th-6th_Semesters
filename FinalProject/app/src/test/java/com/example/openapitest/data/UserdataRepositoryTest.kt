package com.example.openapitest.data

import com.example.openapitest.domain.CatalogStatusFilter
import com.example.openapitest.domain.FruitFilterState
import com.example.openapitest.domain.FruitStatus
import com.example.openapitest.fakes.InMemoryCollectionDao
import com.example.openapitest.fakes.InMemoryFruitDao
import com.example.openapitest.fakes.InMemoryNoteDao
import com.example.openapitest.fakes.InMemorySavedFilterDao
import com.example.openapitest.fakes.InMemoryUserStateDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.test.assertFailsWith

class UserdataRepositoryTest {

    private val fruitDao = InMemoryFruitDao()
    private val collectionDao = InMemoryCollectionDao(fruitDao)
    private val noteDao = InMemoryNoteDao()
    private val userStateDao = InMemoryUserStateDao()
    private val savedFilterDao = InMemorySavedFilterDao()
    private var now = 1L

    private val repository = UserdataRepository(
        collectionDao, noteDao, userStateDao, savedFilterDao
    ) { now }

    @Test
    fun `системная коллекция создаётся один раз и не удаляется`() = runTest {
        repository.ensureSystemCollections()
        repository.ensureSystemCollections()

        val favourites = repository.collections.first().single()
        assertTrue(favourites.collection.isSystem)
        assertEquals("Любимые", favourites.collection.name)

        assertFailsWith<IllegalArgumentException> {
            repository.deleteCollection(favourites.collection.id)
        }
        assertFailsWith<IllegalArgumentException> {
            repository.renameCollection(favourites.collection.id, "X")
        }
    }

    @Test
    fun `статус избранного и системная коллекция синхронизированы`() = runTest {
        repository.ensureSystemCollections()
        val favouritesId = repository.collections.first().single().collection.id

        repository.setStatus(1, FruitStatus.FAVOURITE)
        assertEquals(setOf(favouritesId), repository.observeCollectionIdsForFruit(1).first())

        repository.setStatus(1, FruitStatus.NONE)
        assertTrue(repository.observeCollectionIdsForFruit(1).first().isEmpty())

        repository.addFruitToCollection(favouritesId, 2)
        assertEquals(FruitStatus.FAVOURITE, repository.getUserState(2).status)
    }

    @Test
    fun `создание коллекции обрезает пробелы и отклоняет пустое имя`() = runTest {
        val id = repository.createCollection("  Моя коллекция  ")

        val collections = repository.collections.first()
        assertEquals("Моя коллекция", collections.single().collection.name)
        assertEquals(id, collections.single().collection.id)

        assertFailsWith<IllegalArgumentException> {
            repository.createCollection("   ")
        }
    }

    @Test
    fun `удаление коллекции каскадно удаляет её элементы`() = runTest {
        val id = repository.createCollection("C")
        repository.addFruitToCollection(id, 1)
        repository.addFruitToCollection(id, 2)

        repository.deleteCollection(id)

        assertTrue(collectionDao.items.value.isEmpty())
        assertTrue(repository.collections.first().isEmpty())
    }

    @Test
    fun `закреплённые коллекции идут первыми`() = runTest {
        val first = repository.createCollection("Первая")
        val second = repository.createCollection("Вторая")
        repository.setCollectionPinned(second, true)

        val names = repository.collections.first().map { it.collection.name }

        assertEquals(listOf("Вторая", "Первая"), names)
        assertEquals(setOf(first, second), repository.collections.first().map { it.collection.id }.toSet())
    }

    @Test
    fun `добавление и удаление плода из коллекции`() = runTest {
        val id = repository.createCollection("C")

        repository.addFruitToCollection(id, 5)
        assertEquals(setOf(id), repository.observeCollectionIdsForFruit(5).first())

        repository.removeFruitFromCollection(id, 5)
        assertTrue(repository.observeCollectionIdsForFruit(5).first().isEmpty())
    }

    @Test
    fun `счётчик коллекций на плод`() = runTest {
        val a = repository.createCollection("A")
        val b = repository.createCollection("B")
        repository.addFruitToCollection(a, 1)
        repository.addFruitToCollection(b, 1)
        repository.addFruitToCollection(a, 2)

        assertEquals(mapOf(1 to 2, 2 to 1), repository.fruitCollectionCounts.first())
    }

    @Test
    fun `статус сохраняется и сброс в NONE удаляет строку`() = runTest {
        repository.setStatus(1, FruitStatus.FAVOURITE)
        assertEquals(FruitStatus.FAVOURITE, repository.getUserState(1).status)

        repository.setStatus(1, FruitStatus.NONE)
        assertEquals(FruitStatus.NONE, repository.getUserState(1).status)
        assertTrue(userStateDao.states.value.isEmpty())
    }

    @Test
    fun `строка состояния живёт пока плод скрыт`() = runTest {
        repository.setHidden(1, true)
        repository.setStatus(1, FruitStatus.NONE)

        assertEquals(1, userStateDao.states.value.size)
        assertTrue(repository.getUserState(1).isHidden)

        repository.setHidden(1, false)
        assertTrue(userStateDao.states.value.isEmpty())
    }

    @Test
    fun `заметка сохраняется с временем, пустая — удаляется`() = runTest {
        now = 42L
        repository.saveNote(1, "  важно  ")
        assertEquals("важно", noteDao.notes.value.single().body)
        assertEquals(42L, noteDao.notes.value.single().updatedAt)

        now = 100L
        repository.saveNote(1, "   ")
        assertTrue(noteDao.notes.value.isEmpty())
    }

    @Test
    fun `пресет фильтра сохраняется и восстанавливается`() = runTest {
        repository.saveFilter(
            name = "Хочу Logia",
            query = "gura",
            filter = FruitFilterState(
                status = CatalogStatusFilter.WANT,
                selectedTypes = setOf("Logia", "Zoan")
            )
        )

        val preset = repository.savedFilters.first().single()
        assertEquals("Хочу Logia", preset.name)
        assertEquals("gura", preset.query)
        assertEquals(CatalogStatusFilter.WANT, preset.filter.status)
        assertEquals(setOf("Logia", "Zoan"), preset.filter.selectedTypes)
    }

    @Test
    fun `пустое имя пресета отклоняется`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            repository.saveFilter("  ", "", FruitFilterState())
        }
    }
}
