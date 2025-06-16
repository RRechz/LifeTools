package com.babelsoftware.lifetools.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.babelsoftware.lifetools.BuildConfig
import com.babelsoftware.lifetools.data.AndroidDownloader
import com.babelsoftware.lifetools.data.Downloader
import com.babelsoftware.lifetools.data.GithubRelease
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

/**
 * --- UI State ---
 */
data class UpdateUiState(
    val isChecking: Boolean = false,
    val currentVersion: String = BuildConfig.VERSION_NAME,
    val latestVersionName: String? = null,
    val updateAvailable: Boolean = false,
    val updateCheckError: String? = null,
    val downloadUrl: String? = null,
    val downloadState: DownloadState = DownloadState.Idle
)

/**
 * --- İndirme State ---
 */
sealed class DownloadState {
    object Idle : DownloadState()
    data class Progress(val bytesDownloaded: Long, val totalBytes: Long) : DownloadState()
    object Success : DownloadState()
    data class Error(val message: String) : DownloadState()
}

/**
 * --- UpdateViewModel ---
 */
class UpdateViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(UpdateUiState())
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()

    private val downloader: Downloader = AndroidDownloader(application)

    private val githubUser = "RRechz"
    private val githubRepo = "LifeTools"

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    /**
     * --- Yeni Sürümü Kontrol Et ---
     */
    fun checkForUpdates() {
        _uiState.update {
            it.copy(
                isChecking = true,
                updateCheckError = null,
                latestVersionName = null,
                updateAvailable = false
            )
        }

        viewModelScope.launch {
            try {
                val latestRelease: GithubRelease = client
                    .get("https://api.github.com/repos/$githubUser/$githubRepo/releases/latest")
                    .body()

                val fetchedLatestVersion = latestRelease.tagName
                val apkAsset = latestRelease.assets.find { it.name.endsWith(".apk") }
                val fetchedDownloadUrl = apkAsset?.downloadUrl

                val currentVersion = _uiState.value.currentVersion.removePrefix("v").trim()
                val latestVersion = fetchedLatestVersion.removePrefix("v").trim()

                val isNewer = latestVersion > currentVersion

                _uiState.update {
                    it.copy(
                        latestVersionName = fetchedLatestVersion,
                        updateAvailable = isNewer,
                        downloadUrl = fetchedDownloadUrl,
                        isChecking = false
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        updateCheckError = "Güncelleme kontrolü başarısız oldu: ${e.message}",
                        isChecking = false
                    )
                }
            }
        }
    }

    /**
     * --- Yeni APK'yı İndir ---
     */
    fun startDownload() {
        val downloadUrl = _uiState.value.downloadUrl ?: return

        _uiState.update {
            it.copy(downloadState = DownloadState.Progress(0, 0))
        }

        viewModelScope.launch {
            try {
                downloader.downloadFile(
                    url = downloadUrl,
                    fileName = "LifeTools-latest.apk",
                    description = "Yeni sürüm indiriliyor...",
                    onProgress = { bytesDownloaded, totalBytes ->
                        _uiState.update {
                            it.copy(downloadState = DownloadState.Progress(bytesDownloaded, totalBytes))
                        }
                    },
                    onSuccess = {
                        _uiState.update {
                            it.copy(downloadState = DownloadState.Success)
                        }
                    },
                    onError = { errorMsg ->
                        _uiState.update {
                            it.copy(downloadState = DownloadState.Error(errorMsg))
                        }
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(downloadState = DownloadState.Error(e.message ?: "İndirme sırasında bir hata oluştu"))
                }
            }
        }
    }

    /**
     * --- State Sıfırla ---
     */
    fun clearUpdateState() {
        _uiState.update {
            it.copy(
                latestVersionName = null,
                updateAvailable = false,
                updateCheckError = null,
                downloadUrl = null,
                downloadState = DownloadState.Idle
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        client.close()
    }
}
