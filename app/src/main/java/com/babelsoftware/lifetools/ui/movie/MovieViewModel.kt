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
import kotlinx.serialization.Serializable

// İYİLEŞTİRME 1: Her bir öneriyi temsil edecek yeni bir data class
@Serializable
data class MovieRecommendation(
    val title: String = "N/A",
    val year: String = "N/A",
    val platforms: String = "N/A",
    val description: String = "Açıklama bulunamadı.",
    val imdb: String
)

// İYİLEŞTİRME 2: UiState'i yapılandırılmış veri listesini tutacak şekilde güncelledik
data class MovieUiState(
    val selectedContentType: ContentType = ContentType.MOVIE,
    val selectedGenres: List<String> = emptyList(),
    val yearInput: String = "",
    val selectedPlatforms: List<String> = emptyList(),
    val imdbRating: Float = 7.0f, // YENİ: Başlangıç değeri olarak 7.0
    val recommendationList: List<MovieRecommendation> = emptyList(), // recommendations: String yerine
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class MovieViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MovieUiState())
    val uiState: StateFlow<MovieUiState> = _uiState.asStateFlow()

    private lateinit var generativeModel: GenerativeModel

    init {
        try {
            generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash-latest",
                apiKey = BuildConfig.GEMINI_API_KEY
            )
        } catch (e: Exception) {
            _uiState.update { it.copy(errorMessage = "Film/Dizi AI Modeli başlatılamadı: ${e.localizedMessage}") }
        }
    }

    // YENİ: İMDB puanını güncelleyecek fonksiyon
    fun onImdbRatingChanged(newRating: Float) {
        _uiState.update { it.copy(imdbRating = newRating, recommendationList = emptyList(), errorMessage = null) }
    }

    // Filtre güncelleme fonksiyonları aynı, sadece 'recommendations' yerine 'recommendationList'i temizliyorlar
    fun selectContentType(type: ContentType) {
        _uiState.update { it.copy(selectedContentType = type, recommendationList = emptyList(), errorMessage = null) }
    }

    fun toggleGenre(genre: String) {
        _uiState.update { currentState ->
            val updatedGenres = currentState.selectedGenres.toMutableList().apply {
                if (contains(genre)) remove(genre) else add(genre)
            }
            currentState.copy(selectedGenres = updatedGenres, recommendationList = emptyList(), errorMessage = null)
        }
    }

    fun onYearChanged(year: String) {
        if (year.all { it.isDigit() } && year.length <= 4) {
            _uiState.update { it.copy(yearInput = year, recommendationList = emptyList(), errorMessage = null) }
        }
    }

    fun togglePlatform(platform: String) {
        _uiState.update { currentState ->
            val updatedPlatforms = currentState.selectedPlatforms.toMutableList().apply {
                if (contains(platform)) remove(platform) else add(platform)
            }
            currentState.copy(selectedPlatforms = updatedPlatforms, recommendationList = emptyList(), errorMessage = null)
        }
    }

    // İYİLEŞTİRME 3: Prompt'u daha spesifik ve parse edilebilir hale getirdik
    fun getMovieRecommendations() {
        if (!::generativeModel.isInitialized) {
            _uiState.update { it.copy(errorMessage = "AI Modeli kullanılamıyor.", isLoading = false) }
            return
        }

        _uiState.update { it.copy(isLoading = true, recommendationList = emptyList(), errorMessage = null) }

        val currentState = _uiState.value
        val contentTypeTurkish = if (currentState.selectedContentType == ContentType.MOVIE) "film" else "dizi"

        // Yapay zekadan kolayca ayrıştırabileceğimiz bir formatta cevap istiyoruz.
        var prompt = "Bana ${contentTypeTurkish} öner. Filtrelerim şunlar:"
        if (currentState.selectedGenres.isNotEmpty()) prompt += "\n- Türler: ${currentState.selectedGenres.joinToString(", ")}"
        if (currentState.yearInput.isNotBlank()) prompt += "\n- Yapım Yılı: ${currentState.yearInput} yılına yakın"
        if (currentState.selectedPlatforms.isNotEmpty()) prompt += "\n- Platformlar: ${currentState.selectedPlatforms.joinToString(", ")}"

        // YENİ: Prompt'a İMDB puanı filtresini ekliyoruz
        prompt += "\n- İMDB Puanı: ${currentState.imdbRating} ve üzeri"

        prompt += """

        Lütfen 3 adet öneri sun. Sunacağın filmlerin bilgilerini İMDB sitesinden al. Bu, filmler hakkında en doğru bilgilerin olmasını sağlar.
        Her bir öneriyi şu formatta ve etiketleri kullanarak hazırla:
        BAŞLIK: <Film/Dizi_Adı>
        YIL: <Yapım_Yılı>
        PLATFORMLAR: <Platform 1,_Platform 2>
        İMDB PUAN: <Film_imdb_puanı>
        AÇIKLAMA: <Açıklama>
        ---
        """.trimIndent()


        viewModelScope.launch {
            try {
                val response = generativeModel.generateContent(prompt)
                response.text?.let { movieResults ->
                    // İYİLEŞTİRME 4: Gelen metni ayrıştırıp listeye çeviriyoruz
                    val recommendations = parseMovieRecommendations(movieResults)
                    _uiState.update { it.copy(recommendationList = recommendations, isLoading = false) }
                } ?: run {
                    _uiState.update { it.copy(errorMessage = "Yapay zekadan boş öneri alındı.", isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Öneri alınırken bir hata oluştu: ${e.localizedMessage}", isLoading = false)
                }
                e.printStackTrace()
            }
        }
    }

    // İYİLEŞTİRME 5: Yanıtı ayrıştıran yardımcı fonksiyon
    private fun parseMovieRecommendations(responseText: String): List<MovieRecommendation> {
        val recommendations = mutableListOf<MovieRecommendation>()
        // Her bir film/dizi bloğunu "---" ayıracına göre böl
        val blocks = responseText.split("---").filter { it.isNotBlank() }

        for (block in blocks) {
            val lines = block.lines()
            var title = ""
            var year = ""
            var imdb = ""
            var platforms = ""
            var description = ""

            lines.forEach { line ->
                when {
                    line.startsWith("BAŞLIK:") -> title = line.removePrefix("BAŞLIK:").trim()
                    line.startsWith("YIL:") -> year = line.removePrefix("YIL:").trim()
                    line.startsWith("IMDB:") -> imdb = line.removePrefix("IMDB:").trim() // YENİ
                    line.startsWith("PLATFORMLAR:") -> platforms = line.removePrefix("PLATFORMLAR:").trim()
                    line.startsWith("AÇIKLAMA:") -> description = line.removePrefix("AÇIKLAMA:").trim()
                    // Eğer açıklama birden çok satıra yayılıyorsa, onu mevcut açıklamaya ekle
                    description.isNotEmpty() && !line.startsWith("BAŞLIK:") && !line.startsWith("YIL:") && !line.startsWith("PLATFORMLAR:") ->
                        description += "\n" + line.trim()
                }
            }
            if (title.isNotEmpty()) {
                recommendations.add(MovieRecommendation(title, year, platforms, description, imdb))
            }
        }
        return recommendations
    }

    /**
     * Widget için tek ve kısa bir film önerisi getirir.
     * @return Başarılı olursa öneri metnini (String), başarısız olursa null döner.
     */
    suspend fun fetchSingleMovieRecommendationForWidget(): String? {
        if (!::generativeModel.isInitialized) {
            return null
        }

        // Widget için çok daha basit ve kısa bir cevap isteyen özel prompt.
        val prompt = """
        Bana popüler veya kült olabilecek, genel beğeniye uygun tek bir film öner. Film bilgilerini İMDB'den al.
        Cevabın çok kısa olsun ve sadece filmin adını ve parantez içerisinde yapım yılı içersin.
        Bu cevap bir mobil uygulama widget'ında gösterilecek, bu yüzden kısa ve öz olması çok önemli.
        
        Örnek formatlar:
        - Yüzüklerin Efendisi: Yüzük Kardeşliği (2001)
        - Ucuz Roman (1994)
        - Parazit (2019)
        
        Lütfen sadece bu formatta tek bir satır cevap ver.
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