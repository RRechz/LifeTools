// File: ui/truthordare/TruthQuestionsViewModel.kt
package com.babelsoftware.lifetools.ui.truthordare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babelsoftware.lifetools.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TruthQuestionsUiState(
    val questions: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class TruthQuestionsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TruthQuestionsUiState())
    val uiState: StateFlow<TruthQuestionsUiState> = _uiState.asStateFlow()

    private lateinit var generativeModel: GenerativeModel

    init {
        try {
            // Doğruluk/Cesaretlik için Gemini 1.5 Pro modelini hedefliyoruz
            // Kota sorunu olursa "gemini-1.5-flash-latest" ile değiştirilebilir.
            generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash-latest",
                apiKey = BuildConfig.GEMINI_API_KEY
            )
        } catch (e: Exception) {
            _uiState.update {
                it.copy(errorMessage = "Doğruluk Soruları AI Modeli başlatılamadı: ${e.localizedMessage}")
            }
        }
    }

    fun fetchTruthQuestions() {
        if (!::generativeModel.isInitialized) {
            _uiState.update { it.copy(errorMessage = "AI Modeli kullanılamıyor.", isLoading = false) }
            return
        }

        _uiState.update { it.copy(isLoading = true, questions = emptyList(), errorMessage = null) }

        val prompt = """
        Bir arkadaş grubu arasında veya gençlerin oynayabileceği, samimi ve eğlenceli bir ortam yaratacak "doğruluk" soruları listesi oluştur. 
        Sorular daha önce forumlarda (örneğin Ekşisözlük, Technopat gibi yerlerde) veya sosyal medyada sorulmuş popüler veya ilginç olabilecek soru tiplerine benzesin.
        Yaklaşık 10 ila 15 adet soru listele. Her bir soruyu yeni bir satırda numaralandırmadan ver.
        Örnek:
        En son kime yalan söyledin?
        Hiç kopya çektin mi?
        Hayatındaki en büyük pişmanlığın nedir?
        """.trimIndent()

        viewModelScope.launch {
            try {
                val response = generativeModel.generateContent(prompt)
                val responseText = response.text
                if (responseText != null) {
                    // Cevabı satırlara bölerek soru listesi oluştur
                    val questionList = responseText.lines().filter { it.isNotBlank() }
                    _uiState.update { it.copy(questions = questionList, isLoading = false) }
                } else {
                    _uiState.update { it.copy(errorMessage = "Yapay zekadan boş cevap alındı.", isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Doğruluk soruları alınırken bir hata oluştu: ${e.localizedMessage}",
                        isLoading = false
                    )
                }
                e.printStackTrace()
            }
        }
    }

    /**
     * Widget için tek ve kısa bir doğruluk sorusu getirir.
     * Bu fonksiyon direkt olarak çağrılmak için 'suspend' olarak işaretlenmiştir.
     * @return Başarılı olursa soruyu (String), başarısız olursa null döner.
     */
    suspend fun fetchSingleQuestionForWidget(): String? {
        if (!::generativeModel.isInitialized) {
            // Hata durumunu çağıran yere bildirmek için null dönebiliriz.
            return null
        }

        // Widget için daha kısa ve tek bir soru isteyen özel prompt.
        val prompt = """
        Bir mobil uygulama widget'ında gösterilmek üzere, ilgi çekici ve çok kısa, tek bir "doğruluk" sorusu üret. 
        Cevap sadece soru metnini içermelidir. Başka hiçbir ek açıklama veya numara olmamalıdır.
        Örnek:
        En son kime yalan söyledin?
        En büyük sırrın ne?
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent(prompt)
            // Gelen cevabın null değilse ve boş değilse ilk satırını alalım.
            response.text?.lines()?.firstOrNull { it.isNotBlank() }
        } catch (e: Exception) {
            e.printStackTrace()
            null // Hata durumunda null dön.
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}