// File: ui/main/MainViewModel.kt
package com.babelsoftware.lifetools.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babelsoftware.lifetools.BuildConfig
import com.babelsoftware.lifetools.data.GithubRelease
import com.google.ai.client.generativeai.GenerativeModel
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
import java.util.Calendar

data class MainUiState(
    val tipOfTheDay: String = "Yeni bir güne başla...",
    val isLoadingTip: Boolean = false,
    // YENİ: Güncelleme ile ilgili state'ler
    val isUpdateAvailable: Boolean = false,
    val latestVersionName: String? = null
)

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private lateinit var generativeModel: GenerativeModel
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    private val githubUser = "RRechz" // TODO: Kendi GitHub kullanıcı adınızı yazın
    private val githubRepo = "LifeTools" // TODO: Kendi GitHub repo adınızı yazın

    init {
        try {
            generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash-latest",
                apiKey = BuildConfig.GEMINI_API_KEY
            )
            // ViewModel başlatıldığında hem ipucu al hem de güncellemeyi kontrol et
            fetchTipOfTheDay()
            checkForUpdates()
        } catch (e: Exception) {
            _uiState.update { it.copy(tipOfTheDay = "İpucu yüklenemedi.") }
        }
    }

    fun fetchTipOfTheDay() {
        _uiState.update { it.copy(isLoadingTip = true) }

        viewModelScope.launch {
            try {
                if (!::generativeModel.isInitialized) {
                    throw IllegalStateException("AI Modeli başlatılamadı.")
                }

                // İYİLEŞTİRME: Günün saatine göre bir tema belirliyoruz.
                val calendar = Calendar.getInstance()
                val promptContext = when (calendar.get(Calendar.HOUR_OF_DAY)) {
                    in 6..11 -> "güne enerjik başlamasına yardımcı olacak motive edici bir söz"
                    in 12..17 -> "günün ortasında küçük bir mola vermesini sağlayacak düşündürücü bir fikir"
                    in 18..22 -> "günün yorgunluğunu atmasına yardımcı olacak sakinleştirici bir tavsiye"
                    else -> "geceye huzurlu bir geçiş yapmasını sağlayacak sakin bir düşünce"
                }

                val prompt = "Kullanıcıya, $promptContext söyle. Cevabın sadece cümlenin kendisi olsun, 15 kelimeyi geçmesin ve herhangi bir başlık veya tırnak işareti içermesin."

                val response = generativeModel.generateContent(prompt)

                _uiState.update {
                    it.copy(
                        tipOfTheDay = response.text ?: "Bugünü harika geçir!",
                        isLoadingTip = false
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        tipOfTheDay = "Hayatın tadını çıkar.",
                        isLoadingTip = false
                    )
                }
            }
        }
    }

    // YENİ: UpdateViewModel'daki mantığın bir benzeri buraya eklendi
    fun checkForUpdates() {
        viewModelScope.launch {
            try {
                val latestRelease: GithubRelease = client.get("https://api.github.com/repos/$githubUser/$githubRepo/releases/latest").body()
                val fetchedLatestVersion = latestRelease.tagName

                val currentVersion = BuildConfig.VERSION_NAME.removePrefix("v").trim()
                val latestVersion = fetchedLatestVersion.removePrefix("v").trim()

                // TODO: Daha robust bir sürüm karşılaştırma mantığı eklenebilir.
                val isNewer = latestVersion > currentVersion // Basit string karşılaştırması

                if (isNewer) {
                    _uiState.update {
                        it.copy(
                            latestVersionName = fetchedLatestVersion,
                            isUpdateAvailable = true
                        )
                    }
                }
            } catch (e: Exception) {
                // Hata durumunda sessiz kal, kullanıcıyı rahatsız etme. Sadece logla.
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        client.close()
    }
}