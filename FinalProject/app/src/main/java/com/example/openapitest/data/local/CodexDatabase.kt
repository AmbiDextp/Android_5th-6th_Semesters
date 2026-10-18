package com.example.openapitest.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
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

@Database(
    entities = [
        FruitEntity::class,
        CollectionEntity::class,
        CollectionItemEntity::class,
        NoteEntity::class,
        FruitUserStateEntity::class,
        SavedFilterEntity::class,
        HistoryEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class CodexDatabase : RoomDatabase() {
    abstract fun fruitDao(): FruitDao
    abstract fun collectionDao(): CollectionDao
    abstract fun noteDao(): NoteDao
    abstract fun userStateDao(): UserStateDao
    abstract fun savedFilterDao(): SavedFilterDao
    abstract fun historyDao(): HistoryDao
}
