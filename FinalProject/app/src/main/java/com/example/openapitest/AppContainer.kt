package com.example.openapitest

import android.content.Context
import androidx.room.Room
import com.example.openapitest.data.CatalogRepository
import com.example.openapitest.data.CatalogTtlPolicy
import com.example.openapitest.data.HistoryRepository
import com.example.openapitest.data.UserdataRepository
import com.example.openapitest.data.local.CodexDatabase
import com.example.openapitest.data.settings.DataStoreSettingsRepository
import com.example.openapitest.data.settings.SettingsRepository
import com.example.openapitest.data.sync.CodexWorkerFactory
import com.example.openapitest.data.sync.SyncScheduler

class AppContainer(context: Context) {

    private val database: CodexDatabase = Room.databaseBuilder(
        context.applicationContext,
        CodexDatabase::class.java,
        "codex.db"
    ).fallbackToDestructiveMigration().build()

    val settingsRepository: SettingsRepository =
        DataStoreSettingsRepository(context.applicationContext)

    val catalogRepository: CatalogRepository =
        CatalogRepository(NetworkModule.api, database.fruitDao())

    val userdataRepository: UserdataRepository = UserdataRepository(
        collectionDao = database.collectionDao(),
        noteDao = database.noteDao(),
        userStateDao = database.userStateDao(),
        savedFilterDao = database.savedFilterDao()
    )

    val historyRepository: HistoryRepository =
        HistoryRepository(database.historyDao())

    val syncScheduler: SyncScheduler = SyncScheduler(context.applicationContext)

    val workerFactory: CodexWorkerFactory = CodexWorkerFactory(catalogRepository)

    val ttlPolicy: CatalogTtlPolicy = CatalogTtlPolicy()
}
