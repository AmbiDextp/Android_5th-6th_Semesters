package com.example.openapitest.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.BackoffPolicy
import com.example.openapitest.data.CatalogRepository
import com.example.openapitest.data.RefreshResult
import java.util.concurrent.TimeUnit

class CatalogSyncWorker(
    appContext: Context,
    params: WorkerParameters,
    private val repository: CatalogRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val result = repository.refresh()
        return when {
            result is RefreshResult.Success -> Result.success()
            shouldRetry(result, runAttemptCount, MAX_ATTEMPTS) -> Result.retry()
            else -> Result.failure()
        }
    }

    companion object {
        const val MAX_ATTEMPTS = 3
    }
}

fun shouldRetry(result: RefreshResult, runAttemptCount: Int, maxAttempts: Int = 3): Boolean =
    result is RefreshResult.Error && runAttemptCount + 1 < maxAttempts

class SyncScheduler(private val context: Context) {

    fun setAutoSync(enabled: Boolean) {
        val workManager = WorkManager.getInstance(context)
        if (enabled) {
            val request = PeriodicWorkRequestBuilder<CatalogSyncWorker>(
                SYNC_INTERVAL_HOURS, TimeUnit.HOURS
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        } else {
            workManager.cancelUniqueWork(WORK_NAME)
        }
    }

    companion object {
        const val WORK_NAME = "catalog_sync"
        const val SYNC_INTERVAL_HOURS = 6L
    }
}
