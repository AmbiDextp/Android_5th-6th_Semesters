package com.example.openapitest.data

import com.example.openapitest.fakes.FakeOneApi
import com.example.openapitest.fakes.InMemoryFruitDao
import com.example.openapitest.fakes.fruitDto
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class CatalogRepositoryTest {

    private val api = FakeOneApi()
    private val dao = InMemoryFruitDao()
    private var now = 1_000L
    private val repository = CatalogRepository(api, dao, clock = { now }, ioDispatcher = Dispatchers.Unconfined)

    @Test
    fun `успешное обновление сохраняет плоды с меткой времени`() = runTest {
        api.dtos = listOf(fruitDto(1), fruitDto(2))

        val result = repository.refresh()

        assertTrue(result is RefreshResult.Success)
        assertEquals(2, dao.count())
        assertEquals(now, dao.lastCachedAt())
    }

    @Test
    fun `некорректные записи отбрасываются`() = runTest {
        api.dtos = listOf(
            fruitDto(1),
            fruitDto(2, name = "   "),
            fruitDto(id = null)
        )

        repository.refresh()

        assertEquals(1, dao.count())
    }

    @Test
    fun `сетевая ошибка сохраняет старый кэш и помечается как offline`() = runTest {
        api.dtos = listOf(fruitDto(1))
        repository.refresh()

        api.error = IOException("no network")
        val result = repository.refresh()

        assertTrue(result is RefreshResult.Error && result.offline)
        assertEquals(1, dao.count())
    }

    @Test
    fun `http ошибка не считается offline`() = runTest {
        api.error = HttpException(Response.error<Any>(500, "".toResponseBody(null)))

        val result = repository.refresh()

        assertTrue(result is RefreshResult.Error && !result.offline)
    }

    @Test
    fun `пустой ответ API трактуется как ошибка и не затирает кэш`() = runTest {
        api.dtos = listOf(fruitDto(1))
        repository.refresh()

        api.dtos = emptyList()
        val result = repository.refresh()

        assertTrue(result is RefreshResult.Error && !result.offline)
        assertEquals(1, dao.count())
    }

    @Test
    fun `очистка кэша удаляет только несвязанные плоды`() = runTest {
        api.dtos = listOf(fruitDto(1), fruitDto(2), fruitDto(3))
        repository.refresh()
        dao.protectedIds = setOf(1, 2)

        val deleted = repository.clearUnreferencedCache()

        assertEquals(1, deleted)
        assertEquals(listOf(1, 2), dao.fruits.value.map { it.id }.sorted())
    }

    @Test
    fun `плоды из кэша отдаются как доменные модели`() = runTest {
        api.dtos = listOf(fruitDto(1))
        repository.refresh()

        val names = repository.fruits.first().map { it.name }

        assertEquals(listOf("Fruit 1"), names)
    }
}
