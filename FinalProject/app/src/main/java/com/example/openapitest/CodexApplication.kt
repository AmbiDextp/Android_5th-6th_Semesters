package com.example.openapitest

import android.app.Application
import androidx.work.Configuration
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class CodexApplication : Application(), Configuration.Provider, ImageLoaderFactory {

    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        container.settingsRepository.settings
            .map { it.autoSync }
            .distinctUntilChanged()
            .onEach { enabled -> container.syncScheduler.setAutoSync(enabled) }
            .launchIn(applicationScope)

        applicationScope.launch {
            container.userdataRepository.ensureSystemCollections()
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(container.workerFactory)
            .build()

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(64L * 1024 * 1024)
                    .build()
            }
            .crossfade(true)
            .build()
}
