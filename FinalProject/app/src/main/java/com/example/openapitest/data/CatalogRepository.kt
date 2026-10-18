package com.example.openapitest.data

import com.example.openapitest.data.local.dao.FruitDao
import com.example.openapitest.data.local.entity.toDomain
import com.example.openapitest.data.remote.OneApi
import com.example.openapitest.data.remote.toEntity
import com.example.openapitest.model.Fruit
import java.io.IOException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import retrofit2.HttpException

sealed class RefreshResult {
    data object Success : RefreshResult()
    data class Error(val offline: Boolean, val cause: Exception? = null) : RefreshResult()
}

class CatalogRepository(
    private val api: OneApi,
    private val fruitDao: FruitDao,
    private val clock: () -> Long = System::currentTimeMillis,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    val fruits: Flow<List<Fruit>> =
        fruitDao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeFruit(id: Int): Flow<Fruit?> =
        fruitDao.observeById(id).map { it?.toDomain() }

    val lastCachedAt: Flow<Long?> = fruitDao.observeLastCachedAt()

    val cachedCount: Flow<Int> = fruitDao.observeCount()

    private val refreshMutex = Mutex()

    suspend fun refresh(): RefreshResult = withContext(ioDispatcher) {
        refreshMutex.withLock {
            try {
                val now = clock()
                val entities = api.getFruits().mapNotNull { it.toEntity(now) }
                if (entities.isEmpty()) {
                    return@withLock RefreshResult.Error(offline = false)
                }
                fruitDao.upsertAll(entities)
                RefreshResult.Success
            } catch (e: IOException) {
                RefreshResult.Error(offline = true, cause = e)
            } catch (e: HttpException) {
                RefreshResult.Error(offline = false, cause = e)
            } catch (e: Exception) {
                RefreshResult.Error(offline = false, cause = e)
            }
        }
    }

    suspend fun clearUnreferencedCache(): Int = withContext(ioDispatcher) {
        fruitDao.deleteUnreferenced()
    }
}
