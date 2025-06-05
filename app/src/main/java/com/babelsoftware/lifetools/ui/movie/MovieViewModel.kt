// File: ui/movie/MovieViewModel.kt
package com.babelsoftware.lifetools.ui.movie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babelsoftware.lifetools.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// MovieScreen için UI durumunu tutacak data class
data class MovieUiState(
    val selectedContentType: ContentType = ContentType.MOVIE, // Varsayılan olarak film
    val selectedGenres: List<String> = emptyList(),
    val yearInput: String = "",
    val selectedPlatforms: List<String> = emptyList(),
    val recommendations: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
    // allGenres ve allPlatforms listeleri şimdilik MovieScreen'de sabit,
    // dinamik olsaydı burada veya repository'de olabilirdi.
)

class MovieViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MovieUiState())
    val uiState: StateFlow<MovieUiState> = _uiState.asStateFlow()

    private lateinit var generativeModel: GenerativeModel

    init {
        // Gemini 1.5 Pro modelini ve API anahtarını kullanarak modeli başlatıyoruz.
        try {
            generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash-latest", // Film/Dizi önerileri için Pro model
                apiKey = BuildConfig.GEMINI_API_KEY
            )
        } catch (e: Exception) {
            _uiState.update {
                it.copy(errorMessage = "Film/Dizi AI Modeli başlatılamadı: ${e.localizedMessage}")
            }
        }
    }

    fun selectContentType(type: ContentType) {
        _uiState.update { it.copy(selectedContentType = type, recommendations = "", errorMessage = null) }
    }

    fun toggleGenre(genre: String) {
        _uiState.update { currentState ->
            val updatedGenres = currentState.selectedGenres.toMutableList()
            if (updatedGenres.contains(genre)) {
                updatedGenres.remove(genre)
            } else {
                updatedGenres.add(genre)
            }
            currentState.copy(selectedGenres = updatedGenres, recommendations = "", errorMessage = null)
        }
    }

    fun onYearChanged(year: String) {
        // Basit bir doğrulama (sadece rakam ve 4 karakter)
        if (year.all { it.isDigit() } && year.length <= 4) {
            _uiState.update { it.copy(yearInput = year, recommendations = "", errorMessage = null) }
        }
    }

    fun togglePlatform(platform: String) {
        _uiState.update { currentState ->
            val updatedPlatforms = currentState.selectedPlatforms.toMutableList()
            if (updatedPlatforms.contains(platform)) {
                updatedPlatforms.remove(platform)
            } else {
                updatedPlatforms.add(platform)
            }
            currentState.copy(selectedPlatforms = updatedPlatforms, recommendations = "", errorMessage = null)
        }
    }

    fun getMovieRecommendations() {
        if (!::generativeModel.isInitialized) {
            _uiState.update { it.copy(errorMessage = "AI Modeli kullanılamıyor.", isLoading = false) }
            return
        }

        _uiState.update { it.copy(isLoading = true, recommendations = "", errorMessage = null) }

        val currentState = _uiState.value
        val contentTypeTurkish = if (currentState.selectedContentType == ContentType.MOVIE) "film" else "dizi"

        var prompt = "Bana ${contentTypeTurkish} öner."

        if (currentState.selectedGenres.isNotEmpty()) {
            prompt += " Şu türlerde olsun: ${currentState.selectedGenres.joinToString(", ")}."
        }
        if (currentState.yearInput.isNotBlank()) {
            prompt += " ${currentState.yearInput} yapımı veya o yıllara yakın olsun."
        }
        if (currentState.selectedPlatforms.isNotEmpty()) {
            prompt += " Mümkünse şu platformlarda bulunsun: ${currentState.selectedPlatforms.joinToString(", ")}."
        }

        prompt += " Bana bu kriterlere uygun birkaç farklı seçenek sun ve her bir öneri için kısa bir açıklama yap. Önerilerini liste halinde ve anlaşılır bir formatta ver."


        viewModelScope.launch {
            try {
                val response = generativeModel.generateContent(prompt)
                response.text?.let { movieResults ->
                    _uiState.update { it.copy(recommendations = movieResults, isLoading = false) }
                } ?: run {
                    _uiState.update { it.copy(errorMessage = "Yapay zekadan boş öneri alındı.", isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Öneri alınırken bir hata oluştu: ${e.localizedMessage}",
                        isLoading = false
                    )
                }
                e.printStackTrace()
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}