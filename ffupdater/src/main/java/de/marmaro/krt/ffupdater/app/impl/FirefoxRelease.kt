package de.marmaro.krt.ffupdater.app.impl

import android.content.Context
import android.os.Build
import de.marmaro.krt.ffupdater.R
import de.marmaro.krt.ffupdater.app.BaseAppDetail
import de.marmaro.krt.ffupdater.app.UpdateCheckSubResult
import de.marmaro.krt.ffupdater.app.impl.fetch.ApiConsumer
import de.marmaro.krt.ffupdater.app.impl.fetch.mozillaci.MozillaCiConsumer
import de.marmaro.krt.ffupdater.device.ABI
import de.marmaro.krt.ffupdater.device.DeviceEnvironment
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME

/**
 * https://firefox-ci-tc.services.mozilla.com/tasks/index/mobile.v2.fenix.release.latest
 * https://www.apkmirror.com/apk/mozilla/firefox/
 */
class FirefoxRelease(private val apiConsumer: ApiConsumer) : BaseAppDetail() {
    override val packageName = "org.mozilla.firefox"
    override val displayTitle = R.string.firefox_release_title
    override val displayDescription = R.string.firefox_release_description
    override val displayWarning: Int? = null
    override val displayDownloadSource = R.string.mozilla_ci
    override val signatureHash = "a78b62a5165b4494b2fead9e76a280d22d937fee6251aece599446b2ea319b04"
    override val minApiLevel = Build.VERSION_CODES.LOLLIPOP
    override val supportedAbis = listOf(ABI.ARM64_V8A, ABI.ARMEABI_V7A, ABI.X86_64, ABI.X86)

    override fun getDisplayInstalledVersion(context: Context): String {
        return context.getString(R.string.installed_version, getInstalledVersionFromPackageManager(context))
    }

    override fun getInstalledVersion(context: Context): String? {
        return getInstalledVersionFromSharedPreferences(context, INSTALLED_VERSION_KEY)
    }

    override fun updateCheckBlocking(context: Context,
                                     deviceEnvironment: DeviceEnvironment): UpdateCheckSubResult {
        val abiString = deviceEnvironment.abis.mapNotNull {
            when (it) {
                ABI.ARM64_V8A -> "arm64-v8a"
                ABI.ARMEABI_V7A -> "armeabi-v7a"
                ABI.X86 -> "x86"
                ABI.X86_64 -> "x86_64"
                ABI.ARMEABI, ABI.MIPS, ABI.MIPS64 -> null
            }
        }.first()
        val mozillaCiConsumer = MozillaCiConsumer(
                apiConsumer = apiConsumer,
                task = "mobile.v2.fenix.release.latest.$abiString",
                apkArtifact = "public/build/$abiString/target.apk")
        val result = mozillaCiConsumer.updateCheck()
        val version = result.timestamp
        val shortVersion = version.split("T")[0]
        return UpdateCheckSubResult(
                downloadUrl = result.url,
                version = version,
                displayVersion = context.getString(R.string.available_version_timestamp, shortVersion),
                publishDate = ZonedDateTime.parse(result.timestamp, ISO_ZONED_DATE_TIME),
                fileHashSha256 = result.hash,
                fileSizeBytes = null)
    }

    override fun installationCallback(context: Context, installedVersion: String) {
        setInstalledVersionInSharedPreferences(context, INSTALLED_VERSION_KEY, installedVersion)
    }

    companion object {
        const val INSTALLED_VERSION_KEY = "device_app_register_FIREFOX_RELEASE_version_name"
    }
}