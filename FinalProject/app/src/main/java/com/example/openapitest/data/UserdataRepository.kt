package com.example.openapitest.data

import com.example.openapitest.data.local.CollectionWithItems
import com.example.openapitest.data.local.dao.CollectionDao
import com.example.openapitest.data.local.dao.NoteDao
import com.example.openapitest.data.local.dao.SavedFilterDao
import com.example.openapitest.data.local.dao.UserStateDao
import com.example.openapitest.data.local.entity.CollectionEntity
import com.example.openapitest.data.local.entity.CollectionItemEntity
import com.example.openapitest.data.local.entity.NoteEntity
import com.example.openapitest.data.local.entity.SavedFilterEntity
import com.example.openapitest.data.local.entity.toDomain
import com.example.openapitest.data.local.entity.toEntity
import com.example.openapitest.domain.FruitFilterState
import com.example.openapitest.domain.FruitStatus
import com.example.openapitest.domain.FruitUserState
import com.example.openapitest.domain.SavedFilter
import com.example.openapitest.model.Fruit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserdataRepository(
    private val collectionDao: CollectionDao,
    private val noteDao: NoteDao,
    private val userStateDao: UserStateDao,
    private val savedFilterDao: SavedFilterDao,
    private val clock: () -> Long = System::currentTimeMillis
) {

    val collections: Flow<List<CollectionWithItems>> =
        collectionDao.observeCollectionsWithItems()

    val collectionsCount: Flow<Int> = collectionDao.observeCount()

    val fruitCollectionCounts: Flow<Map<Int, Int>> =
        collectionDao.observeFruitCounts().map { list ->
            list.associate { it.fruitId to it.count }
        }

    fun observeCollectionName(id: Long): Flow<String?> =
        collectionDao.observeById(id).map { it?.name }

    fun observeFruitsInCollection(id: Long): Flow<List<Fruit>> =
        collectionDao.observeFruitsInCollection(id).map { list ->
            list.map { it.toDomain() }
        }

    fun observeCollectionIdsForFruit(fruitId: Int): Flow<Set<Long>> =
        collectionDao.observeItemsForFruit(fruitId).map { items ->
            items.map { it.collectionId }.toSet()
        }

    suspend fun ensureSystemCollections() {
        if (collectionDao.getSystemCollection() == null) {
            collectionDao.insert(
                CollectionEntity(
                    name = FAVOURITES_NAME,
                    emoji = "\u2764\uFE0F",
                    isPinned = true,
                    isSystem = true,
                    createdAt = clock()
                )
            )
        }
    }

    suspend fun createCollection(name: String, emoji: String = "\uD83C\uDF4D"): Long {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "Название коллекции не может быть пустым" }
        return collectionDao.insert(
            CollectionEntity(name = trimmed, emoji = emoji, createdAt = clock())
        )
    }

    suspend fun renameCollection(id: Long, name: String) {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "Название коллекции не может быть пустым" }
        require(collectionDao.getById(id)?.isSystem != true) {
            "Системную коллекцию переименовать нельзя"
        }
        collectionDao.rename(id, trimmed)
    }

    suspend fun deleteCollection(id: Long) {
        require(collectionDao.getById(id)?.isSystem != true) {
            "Системную коллекцию удалить нельзя"
        }
        collectionDao.delete(id)
    }

    suspend fun setCollectionPinned(id: Long, pinned: Boolean) =
        collectionDao.setPinned(id, pinned)

    suspend fun addFruitToCollection(collectionId: Long, fruitId: Int) {
        val favouritesId = collectionDao.getSystemCollection()?.id
        if (favouritesId != null && collectionId == favouritesId) {
            setStatus(fruitId, FruitStatus.FAVOURITE)
        } else {
            collectionDao.addItem(CollectionItemEntity(collectionId, fruitId, clock()))
        }
    }

    suspend fun removeFruitFromCollection(collectionId: Long, fruitId: Int) {
        val favouritesId = collectionDao.getSystemCollection()?.id
        if (favouritesId != null && collectionId == favouritesId) {
            setStatus(fruitId, FruitStatus.NONE)
        } else {
            collectionDao.removeItem(collectionId, fruitId)
        }
    }

    fun observeNote(fruitId: Int): Flow<NoteEntity?> = noteDao.observeByFruit(fruitId)

    val notesCount: Flow<Int> = noteDao.observeCount()

    suspend fun saveNote(fruitId: Int, body: String) {
        val trimmed = body.trim()
        if (trimmed.isEmpty()) {
            noteDao.deleteByFruit(fruitId)
        } else {
            noteDao.upsert(NoteEntity(fruitId = fruitId, body = trimmed, updatedAt = clock()))
        }
    }

    val userStates: Flow<Map<Int, FruitUserState>> =
        userStateDao.observeAll().map { list ->
            list.associate { it.fruitId to it.toDomain() }
        }

    fun observeUserState(fruitId: Int): Flow<FruitUserState> =
        userStateDao.observeByFruit(fruitId).map { it?.toDomain() ?: FruitUserState(fruitId) }

    suspend fun getUserState(fruitId: Int): FruitUserState =
        userStateDao.getByFruit(fruitId)?.toDomain() ?: FruitUserState(fruitId)

    suspend fun setStatus(fruitId: Int, status: FruitStatus) {
        mutateState(fruitId) { it.copy(status = status) }
        syncFavouritesCollection(fruitId, status == FruitStatus.FAVOURITE)
    }

    suspend fun setHidden(fruitId: Int, hidden: Boolean) =
        mutateState(fruitId) { it.copy(isHidden = hidden) }

    suspend fun setPinned(fruitId: Int, pinned: Boolean) =
        mutateState(fruitId) { it.copy(isPinned = pinned) }

    private suspend fun mutateState(fruitId: Int, transform: (FruitUserState) -> FruitUserState) {
        val current = userStateDao.getByFruit(fruitId)?.toDomain() ?: FruitUserState(fruitId)
        val next = transform(current)
        if (next.isDefault) {
            userStateDao.deleteByFruit(fruitId)
        } else {
            userStateDao.upsert(next.toEntity())
        }
    }

    private suspend fun syncFavouritesCollection(fruitId: Int, isFavourite: Boolean) {
        val favourites = collectionDao.getSystemCollection() ?: return
        val inFavourites = collectionDao.findItem(favourites.id, fruitId) != null
        when {
            isFavourite && !inFavourites ->
                collectionDao.addItem(
                    CollectionItemEntity(favourites.id, fruitId, clock())
                )

            !isFavourite && inFavourites ->
                collectionDao.removeItem(favourites.id, fruitId)
        }
    }

    val savedFilters: Flow<List<SavedFilter>> =
        savedFilterDao.observeAll().map { list -> list.map { it.toDomain() } }

    val savedFiltersCount: Flow<Int> = savedFilterDao.observeCount()

    suspend fun saveFilter(name: String, query: String, filter: FruitFilterState): Long {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "Название пресета не может быть пустым" }
        return savedFilterDao.insert(
            SavedFilterEntity(
                name = trimmed,
                query = query,
                status = filter.status.name,
                typesCsv = filter.selectedTypes.joinToString(","),
                createdAt = clock()
            )
        )
    }

    suspend fun renameFilter(id: Long, name: String) {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "Название пресета не может быть пустым" }
        savedFilterDao.rename(id, trimmed)
    }

    suspend fun deleteFilter(id: Long) = savedFilterDao.delete(id)

    companion object {
        const val FAVOURITES_NAME = "Любимые"
    }
}
