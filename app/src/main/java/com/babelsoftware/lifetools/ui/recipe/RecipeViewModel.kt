// File: ui/recipe/RecipeViewModel.kt
package com.babelsoftware.lifetools.ui.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babelsoftware.lifetools.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// İYİLEŞTİRME: Sohbet mesajlarını modellemek için yeni data class'lar
enum class Participant {
    USER, AI, ERROR
}

data class ChatMessage(
    val text: String,
    val participant: Participant,
    val isLoading: Boolean = false // Sadece AI mesajları için "yazıyor..." animasyonu
)

// İYİLEŞTİRME: UiState'i sohbet geçmişini tutacak şekilde güncelledik
data class RecipeUiState(
    val inputText: String = "",
    val messages: List<ChatMessage> = emptyList(), // recipeResponse yerine mesaj listesi
    val errorMessage: String? = null
)

class RecipeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    private lateinit var generativeModel: GenerativeModel

    init {
        try {
            generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash-latest",
                apiKey = BuildConfig.GEMINI_API_KEY
            )
        } catch (e: Exception) {
            _uiState.update {
                it.copy(errorMessage = "AI Modeli başlatılamadı: ${e.localizedMessage}")
            }
        }
    }

    fun onInputTextChanged(newText: String) {
        _uiState.update { it.copy(inputText = newText, errorMessage = null) }
    }

    // İYİLEŞTİRME: Fonksiyonun mantığı sohbet akışına göre güncellendi
    fun sendMessage() { // Fonksiyon adını daha genel hale getirdik
        val ingredients = _uiState.value.inputText
        if (ingredients.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Lütfen malzeme girin.") }
            return
        }

        // 1. Kullanıcının mesajını listeye ekle ve input'u temizle
        val userMessage = ChatMessage(text = ingredients, participant = Participant.USER)
        // 2. "Yapay zeka yazıyor..." mesajını (loading state) listeye ekle
        val aiLoadingMessage = ChatMessage(text = "", participant = Participant.AI, isLoading = true)

        _uiState.update {
            it.copy(
                messages = it.messages + userMessage + aiLoadingMessage,
                inputText = "", // Input alanını temizle
                errorMessage = null
            )
        }

        // Kullanıcının girdiği malzemelerin sonuna otomatik prompt'u ekliyoruz
        val prompt = "$ingredients elimde bu malzemeler var ve yemek yapmak istiyorum. Bana fikir verir misin? Cevabını Markdown formatında, başlıklar ve listeler kullanarak daha okunaklı bir şekilde hazırla."

        viewModelScope.launch {
            try {
                if (!::generativeModel.isInitialized) {
                    throw IllegalStateException("AI Modeli düzgün başlatılamadı.")
                }
                val response = generativeModel.generateContent(prompt)
                response.text?.let { recipe ->
                    // 3. "Yapay zeka yazıyor..." mesajını kaldırıp yerine gerçek cevabı koy
                    val aiResponseMessage = ChatMessage(text = recipe, participant = Participant.AI)
                    _uiState.update {
                        val updatedMessages = it.messages.dropLast(1) // Son (loading) mesajı kaldır
                        it.copy(messages = updatedMessages + aiResponseMessage)
                    }
                } ?: run {
                    val errorMessage = ChatMessage("Yapay zekadan boş cevap alındı.", Participant.ERROR)
                    _uiState.update {
                        val updatedMessages = it.messages.dropLast(1)
                        it.copy(messages = updatedMessages + errorMessage)
                    }
                }
            } catch (e: Exception) {
                // Hata durumunda da "Yapay zeka yazıyor..." mesajını kaldırıp yerine hata mesajını koy
                val errorMessage = ChatMessage("Tarif alınırken bir hata oluştu: ${e.localizedMessage}", Participant.ERROR)
                _uiState.update {
                    val updatedMessages = it.messages.dropLast(1)
                    it.copy(messages = updatedMessages + errorMessage)
                }
                e.printStackTrace()
            }
        }
    }
}