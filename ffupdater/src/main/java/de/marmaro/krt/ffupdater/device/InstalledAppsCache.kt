package de.marmaro.krt.ffupdater.device

import android.content.Context
import android.util.Log
import androidx.annotation.Keep
import de.marmaro.krt.ffupdater.FFUpdater.Companion.LOG_TAG
import de.marmaro.krt.ffupdater.app.App
import de.marmaro.krt.ffupdater.app.entity.InstallationStatus
import okhttp3.internal.toImmutableList

@Keep
object InstalledAppsCache {
    private var isInitialized = false
    private var installedCorrectFingerprint = mutableListOf<App>()
    private var installedDifferentFingerprint = mutableListOf<App>()

    suspend fun getInstalledAppsWithCorrectFingerprint(context: Context): List<App> {
        initializeCacheIfNecessary(context.applicationContext)
        return installedCorrectFingerprint.toImmutableList()
    }

    suspend fun getInstalledAppsWithDifferentFingerprint(context: Context): List<App> {
        initializeCacheIfNecessary(context.applicationContext)
        return installedDifferentFingerprint.toImmutableList()
    }

    private suspend fun initializeCacheIfNecessary(context: Context) {
        if (!isInitialized) {
            updateCache(context)
        }
    }

    suspend fun updateCache(context: Context) {
        Log.i(LOG_TAG, "InstalledAppsCache: Update cache of installed apps.")
        val correctApps = mutableListOf<App>()
        val differentApps = mutableListOf<App>()
        App.values().forEach {
            when (it.findImpl().isInstalled(context.applicationContext)) {
                InstallationStatus.INSTALLED -> correctApps.add(it)
                InstallationStatus.INSTALLED_WITH_DIFFERENT_FINGERPRINT -> differentApps.add(it)
                else -> {}
            }
        }
        installedCorrectFingerprint = correctApps
        installedDifferentFingerprint = differentApps
        Log.i(LOG_TAG, "InstalledAppsCache: Cache was updated.")
    }
}