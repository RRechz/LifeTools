package com.babelsoftware.lifetools.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babelsoftware.lifetools.BuildConfig
import com.babelsoftware.lifetools.data.GithubRelease
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import android.app.Application // AndroidViewModel için import
import androidx.lifecycle.AndroidViewModel // ViewModel yerine AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.babelsoftware.lifetools.data.AndroidDownloader // Downloader'ımızı import et
import com.babelsoftware.lifetools.data.Downloader // Arayüzü de import et
import io.ktor.client.*

data class UpdateUiState(
    val isChecking: Boolean = false,
    val currentVersion: String = BuildConfig.VERSION_NAME,
    val latestVersionName: String? = null,
    val updateAvailable: Boolean = false,
    val updateCheckError: String? = null,
    val downloadUrl: String? = null
)

class UpdateViewModel(
    private val application: Application // Application context'ini al
) : AndroidViewModel(application) { // AndroidViewModel'dan türet

    private val _uiState = MutableStateFlow(UpdateUiState())
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()

    private val downloader: Downloader = AndroidDownloader(application) // Downloader'ı başlat

    private val githubUser = "RRechz" // TODO: Kendi GitHub kullanıcı adınızı yazın
    private val githubRepo = "LifeTools" // TODO: Kendi GitHub repo adınızı yazın

    // Ktor HttpClient'ı oluştur
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true // API'den gelen tüm alanları parse etmek zorunda kalmamak için
            })
        }
    }

    fun checkForUpdates() {
        _uiState.update { it.copy(isChecking = true, updateCheckError = null, latestVersionName = null, updateAvailable = false) }

        viewModelScope.launch {
            try {
                // Gerçek GitHub API çağrısı
                val latestRelease: GithubRelease = client.get("https://api.github.com/repos/$githubUser/$githubRepo/releases/latest").body()

                val fetchedLatestVersion = latestRelease.tagName
                // Genellikle 'app-release.apk' veya benzeri bir isimle yüklenir
                val apkAsset = latestRelease.assets.find { it.name.endsWith(".apk") }
                val fetchedDownloadUrl = apkAsset?.downloadUrl

                // Sürüm karşılaştırması
                val currentVersion = _uiState.value.currentVersion.removePrefix("v").trim()
                val latestVersion = fetchedLatestVersion.removePrefix("v").trim()

                // TODO: Daha robust bir sürüm karşılaştırma mantığı eklenebilir.
                // Şimdilik basit string karşılaştırması yapıyoruz.
                val isNewer = latestVersion > currentVersion

                if (isNewer) {
                    _uiState.update {
                        it.copy(
                            latestVersionName = fetchedLatestVersion,
                            updateAvailable = true,
                            downloadUrl = fetchedDownloadUrl,
                            isChecking = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            latestVersionName = fetchedLatestVersion,
                            updateAvailable = false,
                            isChecking = false
                        )
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        updateCheckError = "Güncelleme kontrolü başarısız oldu.",
                        isChecking = false
                    )
                }
            }
        }
    }

    fun clearUpdateState() {
        _uiState.update {
            it.copy(
                latestVersionName = null,
                updateAvailable = false,
                updateCheckError = null,
                downloadUrl = null
            )
        }
    }

    fun startDownload() {
        val downloadUrl = _uiState.value.downloadUrl ?: return
        val latestVersion = _uiState.value.latestVersionName ?: "LifeTool_Update"

        val fileName = "app-release.apk"
        val description = "LifeTool için yeni sürüm indiriliyor."

        val downloadId = downloader.downloadFile(downloadUrl, fileName, description)

        // TODO: Bu downloadId'yi bir state'te saklayarak indirme durumunu
        // (progress, completion) takip edebiliriz.
    }


    override fun onCleared() {
        super.onCleared()
        client.close()
    }
}