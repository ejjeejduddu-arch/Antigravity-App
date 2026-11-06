package com.example.gemini.data.remote

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.gemini.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private const val TAG = "RemoteConfigManager"
private const val PREFS_NAME = "antigravity_remote_config_prefs"
private const val KEY_DISMISSED_ANNOUNCEMENTS = "dismissed_announcements"

// Primary Vercel Serverless Edge API, fallback to GitHub Raw
private const val PRIMARY_CONFIG_URL = "https://antigravity-app-tawny.vercel.app/api/config"
private const val FALLBACK_CONFIG_URL = "https://raw.githubusercontent.com/ejjeejduddu-arch/Antigravity-App/master/remote_config.json"

data class MaintenanceConfig(
    val enabled: Boolean = false,
    val title: String = "System Under Maintenance",
    val message: String = "We are currently optimizing server infrastructure. Antigravity will be back online shortly!"
)

data class AnnouncementConfig(
    val active: Boolean = false,
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "info", // "info", "warning", "success", "alert"
    val dismissible: Boolean = true
)

data class RemoteVersionConfig(
    val latest_version_name: String = "1.0.45",
    val latest_version_code: Int = 145,
    val min_supported_version_code: Int = 1,
    val download_url: String = "https://antigravity-app-tawny.vercel.app/download/release-apk",
    val changelog: List<String> = emptyList()
)

data class FeaturesConfig(
    val default_model: String = "Gemini 3.8 Flash (High)",
    val guest_chat_enabled: Boolean = true,
    val audio_enabled: Boolean = true
)

data class RemoteConfigData(
    val maintenance: MaintenanceConfig = MaintenanceConfig(),
    val announcement: AnnouncementConfig = AnnouncementConfig(),
    val version: RemoteVersionConfig = RemoteVersionConfig(),
    val features: FeaturesConfig = FeaturesConfig(),
    val updated_at: String = ""
)

object RemoteConfigManager {

    private val _config = MutableStateFlow(RemoteConfigData())
    val config: StateFlow<RemoteConfigData> = _config.asStateFlow()

    private val _isFetching = MutableStateFlow(false)
    val isFetching: StateFlow<Boolean> = _isFetching.asStateFlow()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    fun init(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            fetchConfig(context)
        }
    }

    suspend fun fetchConfig(context: Context): Result<RemoteConfigData> = withContext(Dispatchers.IO) {
        _isFetching.value = true
        try {
            // 1. Try Primary Edge URL (Fast & Global CDN)
            var jsonString = fetchStringFromUrl(PRIMARY_CONFIG_URL)

            // 2. Fallback to GitHub raw repository if Vercel endpoint is unreachable
            if (jsonString == null) {
                Log.w(TAG, "Primary config endpoint failed, attempting fallback to GitHub raw...")
                jsonString = fetchStringFromUrl(FALLBACK_CONFIG_URL)
            }

            if (jsonString != null) {
                val parsed = parseConfig(jsonString)
                _config.value = parsed
                Log.d(TAG, "Remote config fetched successfully: maintenance=${parsed.maintenance.enabled}, announcement=${parsed.announcement.active}")
                _isFetching.value = false
                return@withContext Result.success(parsed)
            } else {
                _isFetching.value = false
                return@withContext Result.failure(Exception("Failed to fetch remote config from all sources"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching remote config", e)
            _isFetching.value = false
            return@withContext Result.failure(e)
        }
    }

    private fun fetchStringFromUrl(url: String): String? {
        return try {
            val request = Request.Builder()
                .url(url + "?nocache=" + System.currentTimeMillis())
                .header("User-Agent", "Antigravity-Android/${BuildConfig.VERSION_NAME}")
                .header("Cache-Control", "no-cache")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.string()
                } else {
                    Log.w(TAG, "HTTP error ${response.code} from $url")
                    null
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Network exception fetching from $url: ${e.message}")
            null
        }
    }

    private fun parseConfig(jsonString: String): RemoteConfigData {
        val root = JSONObject(jsonString)

        val maintenanceObj = root.optJSONObject("maintenance")
        val maintenance = if (maintenanceObj != null) {
            MaintenanceConfig(
                enabled = maintenanceObj.optBoolean("enabled", false),
                title = maintenanceObj.optString("title", "System Under Maintenance"),
                message = maintenanceObj.optString("message", "We are currently optimizing server infrastructure. Antigravity will be back online shortly!")
            )
        } else MaintenanceConfig()

        val annObj = root.optJSONObject("announcement")
        val announcement = if (annObj != null) {
            AnnouncementConfig(
                active = annObj.optBoolean("active", false),
                id = annObj.optString("id", "ann-default"),
                title = annObj.optString("title", ""),
                message = annObj.optString("message", ""),
                type = annObj.optString("type", "info"),
                dismissible = annObj.optBoolean("dismissible", true)
            )
        } else AnnouncementConfig()

        val verObj = root.optJSONObject("version")
        val version = if (verObj != null) {
            val changelogList = mutableListOf<String>()
            val clArray = verObj.optJSONArray("changelog")
            if (clArray != null) {
                for (i in 0 until clArray.length()) {
                    changelogList.add(clArray.optString(i))
                }
            }
            RemoteVersionConfig(
                latest_version_name = verObj.optString("latest_version_name", "1.0.45"),
                latest_version_code = verObj.optInt("latest_version_code", 145),
                min_supported_version_code = verObj.optInt("min_supported_version_code", 1),
                download_url = verObj.optString("download_url", "https://antigravity-app-tawny.vercel.app/download/release-apk"),
                changelog = changelogList
            )
        } else RemoteVersionConfig()

        val featObj = root.optJSONObject("features")
        val features = if (featObj != null) {
            FeaturesConfig(
                default_model = featObj.optString("default_model", "Gemini 3.8 Flash (High)"),
                guest_chat_enabled = featObj.optBoolean("guest_chat_enabled", true),
                audio_enabled = featObj.optBoolean("audio_enabled", true)
            )
        } else FeaturesConfig()

        val updatedAt = root.optString("updated_at", "")

        return RemoteConfigData(
            maintenance = maintenance,
            announcement = announcement,
            version = version,
            features = features,
            updated_at = updatedAt
        )
    }

    fun isAnnouncementDismissed(context: Context, announcementId: String): Boolean {
        if (announcementId.isBlank()) return false
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val dismissedSet = prefs.getStringSet(KEY_DISMISSED_ANNOUNCEMENTS, emptySet()) ?: emptySet()
        return dismissedSet.contains(announcementId)
    }

    fun dismissAnnouncement(context: Context, announcementId: String) {
        if (announcementId.isBlank()) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentSet = prefs.getStringSet(KEY_DISMISSED_ANNOUNCEMENTS, emptySet())?.toMutableSet() ?: mutableSetOf()
        currentSet.add(announcementId)
        prefs.edit().putStringSet(KEY_DISMISSED_ANNOUNCEMENTS, currentSet).apply()
    }

    fun isForceUpdateRequired(): Boolean {
        val minSupported = _config.value.version.min_supported_version_code
        return minSupported > BuildConfig.VERSION_CODE
    }
}
