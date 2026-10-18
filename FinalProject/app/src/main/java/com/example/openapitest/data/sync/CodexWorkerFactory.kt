package com.example.openapitest.data.sync

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.openapitest.data.CatalogRepository

class CodexWorkerFactory(
    private val catalogRepository: CatalogRepository
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? = when (workerClassName) {
        CatalogSyncWorker::class.java.name ->
            CatalogSyncWorker(appContext, workerParameters, catalogRepository)
        else -> null
    }
}
