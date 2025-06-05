// File: ui/settings/UpdateViewModel.kt (veya ui/update/UpdateViewModel.kt)
package com.babelsoftware.lifetools.ui.settings // veya com.babelsoftware.lifetools.ui.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babelsoftware.lifetools.BuildConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UpdateUiState(
    val isChecking: Boolean = false,
    val currentVersion: String = BuildConfig.VERSION_NAME, // Mevcut sürümü doğrudan alalım
    val latestVersionName: String? = null,
    val updateAvailable: Boolean = false,
    val updateCheckError: String? = null,
    val downloadUrl: String? = null // Gelecekte APK indirme linki için
)

class UpdateViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(UpdateUiState())
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()

    // GitHub repo bilgileri (kendi reponuza göre güncelleyin)
    private val githubUser = "RRechz" // TODO: Kendi GitHub kullanıcı adınızı yazın
    private val githubRepo = "LifeTools"       // TODO: Kendi GitHub repo adınızı yazın

    fun checkForUpdates() {
        _uiState.update { it.copy(isChecking = true, updateCheckError = null, latestVersionName = null, updateAvailable = false) }

        viewModelScope.launch {
            try {
                // TODO: Gerçek GitHub API çağrısı burada yapılacak.
                // Örnek: val response = githubApiService.getLatestRelease(githubUser, githubRepo)
                // Şimdilik simüle edelim:
                delay(2000) // Ağ isteğini simüle et

                // Simüle edilmiş en son sürüm (GitHub'dan gelen tag_name olacak)
                val fetchedLatestVersion = "v0.1.0-beta.3" // TODO: Test için bunu değiştirin
                val fetchedDownloadUrl = "https://github.com/RRechz/LifeTools/releases/latest/download/$fetchedLatestVersion/app-release.apk" // Örnek indirme linki

                // Sürüm karşılaştırması (basit bir string karşılaştırması, daha robust bir yöntem gerekebilir)
                // "v" ön ekini kaldırarak karşılaştırma
                val currentNumeric = _uiState.value.currentVersion.removePrefix("v")
                val latestNumeric = fetchedLatestVersion.removePrefix("v")

                // Daha iyi bir sürüm karşılaştırma mantığı (örn: major.minor.patch)
                // Şimdilik basitçe eşitlik kontrolü yapalım.
                // Daha robust bir karşılaştırma için: https://github.com/gemnasium/semver.org-SEMVER_SPECIFICATION (SemVer)
                val isNewer = latestNumeric > currentNumeric // Basit string karşılaştırması, dikkat!

                if (isNewer) {
                    _uiState.update {
                        it.copy(
                            latestVersionName = fetchedLatestVersion,
                            updateAvailable = true,
                            // downloadUrl = fetchedDownloadUrl,
                            isChecking = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            latestVersionName = fetchedLatestVersion, // En son sürümü yine de göster
                            updateAvailable = false,
                            isChecking = false
                        )
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        updateCheckError = "Güncelleme kontrolü başarısız: ${e.message}",
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
}