package com.example.openapitest.fakes

import com.example.openapitest.data.local.CollectionWithItems
import com.example.openapitest.data.local.FruitCollectionCount
import com.example.openapitest.data.local.HistoryWithFruit
import com.example.openapitest.data.local.RecentFruit
import com.example.openapitest.data.local.dao.CollectionDao
import com.example.openapitest.data.local.dao.FruitDao
import com.example.openapitest.data.local.dao.HistoryDao
import com.example.openapitest.data.local.dao.NoteDao
import com.example.openapitest.data.local.dao.SavedFilterDao
import com.example.openapitest.data.local.dao.UserStateDao
import com.example.openapitest.data.local.entity.CollectionEntity
import com.example.openapitest.data.local.entity.CollectionItemEntity
import com.example.openapitest.data.local.entity.FruitEntity
import com.example.openapitest.data.local.entity.FruitUserStateEntity
import com.example.openapitest.data.local.entity.HistoryEntity
import com.example.openapitest.data.local.entity.NoteEntity
import com.example.openapitest.data.local.entity.SavedFilterEntity
import com.example.openapitest.data.remote.OneApi
import com.example.openapitest.data.remote.OneFruitDto
import com.example.openapitest.data.settings.AppSettings
import com.example.openapitest.data.settings.SettingsRepository
import com.example.openapitest.data.settings.ThemeMode
import com.example.openapitest.data.settings.TtlOption
import com.example.openapitest.domain.HistoryType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

fun fruitDto(id: Int?, name: String = "Fruit $id", type: String = "Paramecia") =
    OneFruitDto(
        id = id,
        name = name,
        type = type,
        description = "Description $id",
        roman_name = null,
        filename = null
    )

fun fruitEntity(id: Int, cachedAt: Long = 0L, type: String = "Paramecia") =
    FruitEntity(
        id = id,
        name = "Fruit $id",
        type = type,
        description = "Description $id",
        romanName = null,
        img = null,
        cachedAt = cachedAt
    )

class FakeOneApi : OneApi {
    var dtos: List<OneFruitDto> = emptyList()
    var error: Exception? = null

    override suspend fun getFruits(): List<OneFruitDto> {
        error?.let { throw it }
        return dtos
    }
}

class FakeSettingsRepository : SettingsRepository {
    val state = MutableStateFlow(AppSettings())
    override val settings: Flow<AppSettings> = state

    override suspend fun setTheme(theme: ThemeMode) {
        state.value = state.value.copy(theme = theme)
    }

    override suspend fun setTtl(ttl: TtlOption) {
        state.value = state.value.copy(ttl = ttl)
    }

    override suspend fun setAutoSync(enabled: Boolean) {
        state.value = state.value.copy(autoSync = enabled)
    }
}

class InMemoryFruitDao : FruitDao {
    val fruits = MutableStateFlow<List<FruitEntity>>(emptyList())

    var protectedIds: Set<Int> = emptySet()

    override fun observeAll(): Flow<List<FruitEntity>> =
        fruits.map { list -> list.sortedBy { it.name.lowercase() } }

    override fun observeById(id: Int): Flow<FruitEntity?> =
        fruits.map { list -> list.firstOrNull { it.id == id } }

    override fun observeLastCachedAt(): Flow<Long?> =
        fruits.map { list -> list.maxOfOrNull { it.cachedAt } }

    override fun observeCount(): Flow<Int> = fruits.map { it.size }

    override suspend fun upsertAll(newFruits: List<FruitEntity>) {
        fruits.value =
            (fruits.value.associateBy { it.id } + newFruits.associateBy { it.id }).values.toList()
    }

    override suspend fun count(): Int = fruits.value.size

    override suspend fun lastCachedAt(): Long? = fruits.value.maxOfOrNull { it.cachedAt }

    override suspend fun deleteUnreferenced(): Int {
        val before = fruits.value.size
        fruits.value = fruits.value.filter { it.id in protectedIds }
        return before - fruits.value.size
    }
}

class InMemoryCollectionDao(private val fruitDao: InMemoryFruitDao) : CollectionDao {
    val collections = MutableStateFlow<List<CollectionEntity>>(emptyList())
    val items = MutableStateFlow<List<CollectionItemEntity>>(emptyList())
    private var nextId = 1L

    override fun observeCollectionsWithItems(): Flow<List<CollectionWithItems>> =
        combine(collections, items) { all, allItems ->
            all.sortedWith(
                compareByDescending<CollectionEntity> { it.isSystem }
                    .thenByDescending { it.isPinned }
                    .thenByDescending { it.createdAt }
            ).map { entity ->
                CollectionWithItems(entity, allItems.filter { it.collectionId == entity.id })
            }
        }

    override fun observeById(id: Long): Flow<CollectionEntity?> =
        collections.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun getById(id: Long): CollectionEntity? =
        collections.value.firstOrNull { it.id == id }

    override suspend fun getSystemCollection(): CollectionEntity? =
        collections.value.firstOrNull { it.isSystem }

    override fun observeCount(): Flow<Int> = collections.map { it.size }

    override suspend fun insert(collection: CollectionEntity): Long {
        val id = nextId++
        collections.value = collections.value + collection.copy(id = id)
        return id
    }

    override suspend fun rename(id: Long, name: String) {
        collections.value = collections.value.map {
            if (it.id == id) it.copy(name = name) else it
        }
    }

    override suspend fun setPinned(id: Long, isPinned: Boolean) {
        collections.value = collections.value.map {
            if (it.id == id) it.copy(isPinned = isPinned) else it
        }
    }

    override suspend fun delete(id: Long) {
        collections.value = collections.value.filter { it.id != id }
        items.value = items.value.filter { it.collectionId != id }
    }

    override suspend fun addItem(item: CollectionItemEntity) {
        items.value = items.value.filterNot {
            it.collectionId == item.collectionId && it.fruitId == item.fruitId
        } + item
    }

    override suspend fun removeItem(collectionId: Long, fruitId: Int) {
        items.value = items.value.filterNot {
            it.collectionId == collectionId && it.fruitId == fruitId
        }
    }

    override suspend fun findItem(collectionId: Long, fruitId: Int): CollectionItemEntity? =
        items.value.firstOrNull {
            it.collectionId == collectionId && it.fruitId == fruitId
        }

    override fun observeItemsForFruit(fruitId: Int): Flow<List<CollectionItemEntity>> =
        items.map { list -> list.filter { it.fruitId == fruitId } }

    override fun observeFruitsInCollection(collectionId: Long): Flow<List<FruitEntity>> =
        combine(items, fruitDao.fruits) { allItems, allFruits ->
            allItems.filter { it.collectionId == collectionId }
                .sortedByDescending { it.addedAt }
                .mapNotNull { item -> allFruits.firstOrNull { it.id == item.fruitId } }
        }

    override fun observeFruitCounts(): Flow<List<FruitCollectionCount>> =
        items.map { list ->
            list.groupBy { it.fruitId }.map { (fruitId, group) ->
                FruitCollectionCount(fruitId, group.size)
            }
        }
}

class InMemoryNoteDao : NoteDao {
    val notes = MutableStateFlow<List<NoteEntity>>(emptyList())

    override fun observeByFruit(fruitId: Int): Flow<NoteEntity?> =
        notes.map { list -> list.firstOrNull { it.fruitId == fruitId } }

    override fun observeCount(): Flow<Int> = notes.map { it.size }

    override suspend fun upsert(note: NoteEntity) {
        notes.value = notes.value.filter { it.fruitId != note.fruitId } + note
    }

    override suspend fun deleteByFruit(fruitId: Int) {
        notes.value = notes.value.filter { it.fruitId != fruitId }
    }
}

class InMemoryUserStateDao : UserStateDao {
    val states = MutableStateFlow<List<FruitUserStateEntity>>(emptyList())

    override fun observeAll(): Flow<List<FruitUserStateEntity>> = states

    override fun observeByFruit(fruitId: Int): Flow<FruitUserStateEntity?> =
        states.map { list -> list.firstOrNull { it.fruitId == fruitId } }

    override suspend fun getByFruit(fruitId: Int): FruitUserStateEntity? =
        states.value.firstOrNull { it.fruitId == fruitId }

    override suspend fun upsert(state: FruitUserStateEntity) {
        states.value = states.value.filter { it.fruitId != state.fruitId } + state
    }

    override suspend fun deleteByFruit(fruitId: Int) {
        states.value = states.value.filter { it.fruitId != fruitId }
    }
}

class InMemorySavedFilterDao : SavedFilterDao {
    val filters = MutableStateFlow<List<SavedFilterEntity>>(emptyList())
    private var nextId = 1L

    override fun observeAll(): Flow<List<SavedFilterEntity>> =
        filters.map { list -> list.sortedByDescending { it.createdAt } }

    override fun observeCount(): Flow<Int> = filters.map { it.size }

    override suspend fun insert(filter: SavedFilterEntity): Long {
        val id = nextId++
        filters.value = filters.value + filter.copy(id = id)
        return id
    }

    override suspend fun rename(id: Long, name: String) {
        filters.value = filters.value.map {
            if (it.id == id) it.copy(name = name) else it
        }
    }

    override suspend fun delete(id: Long) {
        filters.value = filters.value.filter { it.id != id }
    }
}

class InMemoryHistoryDao(private val fruitDao: InMemoryFruitDao) : HistoryDao {
    val entries = MutableStateFlow<List<HistoryEntity>>(emptyList())
    private var nextId = 1L

    override fun observeAllWithFruit(): Flow<List<HistoryWithFruit>> =
        combine(entries, fruitDao.fruits) { all, allFruits ->
            all.sortedByDescending { it.timestamp }.map { entry ->
                HistoryWithFruit(entry, allFruits.firstOrNull { it.id == entry.fruitId })
            }
        }

    override fun observeCount(): Flow<Int> = entries.map { it.size }

    override fun observeRecentViewed(): Flow<List<RecentFruit>> =
        entries.map { all ->
            all.filter { it.type == HistoryType.VIEWED.name }
                .groupBy { it.fruitId }
                .map { (fruitId, group) -> RecentFruit(fruitId, group.maxOf { it.timestamp }) }
                .sortedByDescending { it.lastAt }
                .take(5)
        }

    override suspend fun last(): HistoryEntity? = entries.value.maxByOrNull { it.timestamp }

    override suspend fun insert(entry: HistoryEntity): Long {
        val id = nextId++
        entries.value = entries.value + entry.copy(id = id)
        return id
    }

    override suspend fun update(entry: HistoryEntity) {
        entries.value = entries.value.map { if (it.id == entry.id) entry else it }
    }

    override suspend fun clear() {
        entries.value = emptyList()
    }
}
