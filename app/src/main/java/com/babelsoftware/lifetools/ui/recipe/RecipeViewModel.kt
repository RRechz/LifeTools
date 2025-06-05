// File: ui/recipe/RecipeViewModel.kt
package com.babelsoftware.lifetools.ui.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babelsoftware.lifetools.BuildConfig // API anahtarı için
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ViewModel için state'leri tutacak bir data class (opsiyonel ama düzenli)
data class RecipeUiState(
    val inputText: String = "",
    val recipeResponse: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class RecipeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    private lateinit var generativeModel: GenerativeModel

    init {
        // Gemini 1.5 Flash modelini ve API anahtarını kullanarak modeli başlatıyoruz.
        // API anahtarının BuildConfig içinde olduğundan emin olun.
        try {
            generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash-latest", // Yemek tarifleri için Flash model
                apiKey = BuildConfig.GEMINI_API_KEY
            )
        } catch (e: Exception) {
            _uiState.update {
                it.copy(errorMessage = "AI Modeli başlatılamadı: ${e.localizedMessage}")
            }
            // Burada modeli başlatamazsak, kullanıcıya bir hata göstermemiz gerekebilir.
            // Veya loglayıp, API çağrılarını engelleyebiliriz.
        }
    }

    fun onInputTextChanged(newText: String) {
        _uiState.update { it.copy(inputText = newText, errorMessage = null) } // Hata varsa temizle
    }

    fun getRecipeIdeas() {
        val ingredients = _uiState.value.inputText
        if (ingredients.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Lütfen malzeme girin.") }
            return
        }

        // Daha önce bir hata varsa temizleyelim ve yükleniyor durumunu başlatalım
        _uiState.update { it.copy(isLoading = true, recipeResponse = "", errorMessage = null) }

        // Kullanıcının girdiği malzemelerin sonuna otomatik prompt'u ekliyoruz
        val prompt = "$ingredients elimde bu malzemeler var ve yemek yapmak istiyorum. Bana fikir verir misin?"

        viewModelScope.launch {
            try {
                if (!::generativeModel.isInitialized) {
                    throw IllegalStateException("AI Modeli düzgün başlatılamadı.")
                }
                val response = generativeModel.generateContent(prompt)
                response.text?.let { recipe ->
                    _uiState.update { it.copy(recipeResponse = recipe, isLoading = false) }
                } ?: run {
                    _uiState.update { it.copy(errorMessage = "Yapay zekadan boş cevap alındı.", isLoading = false) }
                }
            } catch (e: Exception) {
                // Hata durumunda kullanıcıya bilgi verelim
                _uiState.update {
                    it.copy(
                        errorMessage = "Tarif alınırken bir hata oluştu: ${e.localizedMessage}",
                        isLoading = false
                    )
                }
                e.printStackTrace() // Geliştirme aşamasında loglama için
            }
        }
    }
}