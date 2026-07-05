package dev.curated.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.disk.DiskCache
import coil3.network.cachecontrol.CacheControlCacheStrategy
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.CachePolicy
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import dagger.hilt.android.HiltAndroidApp
import dev.curated.app.core.privacy.VolumePrivacyHelper
import dev.curated.app.presentation.privacy.GazeProtectionCoordinator
import dev.curated.app.settings.domain.AppPreferences
import dev.curated.app.work.MpvCleanupWorker
import dev.curated.app.work.SyncWorker
import javax.inject.Inject
import kotlin.time.ExperimentalTime
import okhttp3.OkHttpClient
import okio.Path.Companion.toOkioPath
import timber.log.Timber

@HiltAndroidApp
class BaseApplication : Application(), Configuration.Provider, SingletonImageLoader.Factory {
    @Inject lateinit var appPreferences: AppPreferences

    @Inject lateinit var curatedHttpClient: OkHttpClient

    @Inject lateinit var workerFactory: HiltWorkerFactory

    @Inject lateinit var volumePrivacyHelper: VolumePrivacyHelper

    @Inject lateinit var gazeProtectionCoordinator: GazeProtectionCoordinator

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        AppCompatDelegate.setDefaultNightMode(curatedMvpDefaultNightMode())
        volumePrivacyHelper.start(this)
        gazeProtectionCoordinator.start(this)

        val workManager = WorkManager.getInstance(applicationContext)

        if (curatedMvpSchedulesFindroidUserDataSync()) {
            scheduleUserDataSync(workManager)
        }
        scheduleMpvCleanup(workManager)
    }

    @OptIn(ExperimentalCoilApi::class, ExperimentalTime::class)
    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(
                    OkHttpNetworkFetcherFactory(
                        callFactory = { curatedHttpClient },
                        cacheStrategy = { CacheControlCacheStrategy() },
                    )
                )
                add(SvgDecoder.Factory())
            }
            .diskCachePolicy(
                if (appPreferences.getValue(appPreferences.imageCache)) CachePolicy.ENABLED
                else CachePolicy.DISABLED
            )
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache").toOkioPath())
                    .maxSizeBytes(
                        appPreferences.getValue(appPreferences.imageCacheSize) * 1024L * 1024
                    )
                    .build()
            }
            .crossfade(true)
            .build()
    }

    private fun scheduleUserDataSync(workManager: WorkManager) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest =
            OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()

        workManager
            .enqueueUniqueWork(
                uniqueWorkName = "syncUserData",
                existingWorkPolicy = ExistingWorkPolicy.KEEP,
                request = syncWorkRequest
            )
    }

    private fun scheduleMpvCleanup(workManager: WorkManager) {
        val constraints = Constraints.Builder()
            .setRequiresDeviceIdle(true)
            .setRequiresBatteryNotLow(true)
            .build()

        val cleanupRequest =
            OneTimeWorkRequestBuilder<MpvCleanupWorker>()
                .setConstraints(constraints)
                .build()

        workManager.enqueueUniqueWork(
            uniqueWorkName = "mpv_cleanup",
            existingWorkPolicy = ExistingWorkPolicy.KEEP,
            request = cleanupRequest
        )
    }
}

internal fun curatedMvpSchedulesFindroidUserDataSync(): Boolean = false

internal fun curatedMvpDefaultNightMode(): Int = AppCompatDelegate.MODE_NIGHT_YES
