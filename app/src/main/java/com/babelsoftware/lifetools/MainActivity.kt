package com.babelsoftware.lifetools

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.babelsoftware.lifetools.ui.OnboardingScreen
import com.babelsoftware.lifetools.ui.settings.SettingsViewModel
import com.babelsoftware.lifetools.ui.navigation.Screen
import com.babelsoftware.lifetools.ui.recipe.RecipeScreen
import com.babelsoftware.lifetools.ui.movie.MovieScreen
import com.babelsoftware.lifetools.data.AppTheme
import com.babelsoftware.lifetools.ui.theme.LifeToolsTheme
import androidx.compose.foundation.isSystemInDarkTheme // Bu import önemli olacak
import androidx.compose.foundation.layout.PaddingValues // Grid için padding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells // Grid hücreleri için
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid // Grid için
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card // Kart için
import androidx.compose.material3.CardDefaults // Kart varsayılanları için
import androidx.compose.material3.ExperimentalMaterial3Api // TopAppBar ve Card için
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold // Scaffold için
import androidx.compose.material3.TopAppBar // TopAppBar için
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.Row // Row için
import androidx.compose.foundation.layout.Spacer // Spacer için
import androidx.compose.foundation.layout.width // width modifier'ı için
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.IconButton
import androidx.compose.ui.unit.sp // sp (font boyutu) için
import com.babelsoftware.lifetools.BuildConfig // BuildConfig'i import edin
import com.babelsoftware.lifetools.ui.settings.AppearanceSettingsScreen
import com.babelsoftware.lifetools.ui.settings.LanguageSettingsScreen
import com.babelsoftware.lifetools.ui.settings.SettingsScreen
import com.babelsoftware.lifetools.ui.truthordare.SpinnerScreen
import com.babelsoftware.lifetools.ui.truthordare.SpinnerWithQuestionsScreen
import com.babelsoftware.lifetools.ui.truthordare.TruthOrDareHubScreen
import com.babelsoftware.lifetools.ui.truthordare.TruthQuestionsScreen

data class ToolItem(
    val title: String,
    @DrawableRes val iconResId: Int,
    val screenRoute: Screen // Hangi ekrana gideceğini belirtmek için Screen enum'ını kullanalım
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val currentAppTheme by settingsViewModel.appTheme.collectAsStateWithLifecycle()
            val isOnboardingCompleted by settingsViewModel.isOnboardingCompleted.collectAsStateWithLifecycle()

            // Mevcut gösterilecek ekranı takip etmek için bir state
            var currentScreen by remember { mutableStateOf(Screen.MAIN) }

            LifeToolsTheme(
                darkTheme = currentAppTheme.isDarkTheme() // Ufak bir iyileştirme
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!isOnboardingCompleted) {
                        OnboardingScreen(
                            settingsViewModel = settingsViewModel,
                            onOnboardingComplete = {
                                Log.d("MainActivity", "Onboarding tamamlandı, ana içeriğe geçiliyor.")
                                // Onboarding tamamlandığında currentScreen'i MAIN yapmaya gerek yok,
                                // çünkü isOnboardingCompleted true olunca zaten else bloğu çalışacak.
                            }
                        )
                    } else {
                        // Kurulum tamamlandıysa, mevcut ekrana göre içeriği göster
                        when (currentScreen) {
                            Screen.MAIN -> MainAppContent(
                                onNavigate = { destinationScreen ->
                                    if (destinationScreen != Screen.MAIN) { // Kendisine navigasyonu engelle
                                        currentScreen = destinationScreen
                                    }
                                }
                            )
                            Screen.RECIPES -> RecipeScreen(
                                onNavigateBack = { currentScreen = Screen.MAIN }
                            )
                            Screen.MOVIES -> MovieScreen(
                                onNavigateBack = { currentScreen = Screen.MAIN }
                            )
                            Screen.TRUTH_OR_DARE_HUB -> TruthOrDareHubScreen(
                                onNavigateToTruthQuestions = { currentScreen = Screen.TRUTH_QUESTIONS },
                                onNavigateToSpinner = { currentScreen = Screen.SPINNER_GAME },
                                onNavigateToSpinnerWithQuestions = { currentScreen = Screen.SPINNER_WITH_QUESTIONS },
                                onNavigateBack = { currentScreen = Screen.MAIN }
                            )
                            Screen.TRUTH_QUESTIONS -> TruthQuestionsScreen( // Placeholder
                                onNavigateBack = { currentScreen = Screen.TRUTH_OR_DARE_HUB }
                            )
                            Screen.SPINNER_GAME -> SpinnerScreen( // Placeholder
                                onNavigateBack = { currentScreen = Screen.TRUTH_OR_DARE_HUB }
                            )
                            Screen.SPINNER_WITH_QUESTIONS -> SpinnerWithQuestionsScreen( // Placeholder
                                onNavigateBack = { currentScreen = Screen.TRUTH_OR_DARE_HUB }
                            )
                            Screen.SETTINGS -> SettingsScreen(
                                // settingsViewModel burada tekrar alınabilir veya MainActivity'den parametre olarak geçirilebilir.
                                // SettingsScreen kendi içinde viewModel() ile alıyor.
                                onNavigateBack = { currentScreen = Screen.MAIN },
                                onNavigateToAppearanceSettings = { currentScreen = Screen.APPEARANCE_SETTINGS },
                                onNavigateToLanguageSettings = { currentScreen = Screen.LANGUAGE_SETTINGS }
                            )
                            Screen.APPEARANCE_SETTINGS -> AppearanceSettingsScreen( // Placeholder çağrısı
                                onNavigateBack = { currentScreen = Screen.SETTINGS }
                            )
                            Screen.LANGUAGE_SETTINGS -> LanguageSettingsScreen( // Placeholder çağrısı
                                onNavigateBack = { currentScreen = Screen.SETTINGS }
                            )
                        }
                    }
                }
            }
        }
    }
}

// AppTheme enum'una yardımcı bir fonksiyon ekleyebiliriz
@Composable
fun AppTheme.isDarkTheme(): Boolean {
    return when (this) {
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        AppTheme.SYSTEM_DEFAULT -> androidx.compose.foundation.isSystemInDarkTheme()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(
    modifier: Modifier = Modifier,
    onNavigate: (Screen) -> Unit // Tek bir navigasyon fonksiyonu
) {
    // Araçların listesi
    val tools = listOf(
        ToolItem(
            title = stringResource(id = R.string.recipe_screen_title), // Yemek Tarifleri
            iconResId = R.drawable.recipes_with_ai, // Kendi ikonunuz
            screenRoute = Screen.RECIPES
        ),
        ToolItem(
            title = stringResource(id = R.string.movie_screen_title), // Film/Dizi Önerileri
            iconResId = R.drawable.movie_with_ai, // Kendi ikonunuz
            screenRoute = Screen.MOVIES
        ),
        ToolItem(
            title = stringResource(id = R.string.truth_or_dare), // Doğruluk mu Cesaretlik mi?
            iconResId = R.drawable.truth_or_dare_with_ai, // Kendi ikonunuz (placeholder)
            screenRoute = Screen.TRUTH_OR_DARE_HUB // TODO: TRUTH_OR_DARE için Screen enum'ına ekle ve burayı güncelle
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) { // Dikeyde ortala
                        Text(
                            text = stringResource(id = R.string.app_main_title), // "LifeTools"
                            style = MaterialTheme.typography.titleLarge // Ana başlık stili
                        )
                        Spacer(modifier = Modifier.width(2.dp)) // Araya boşluk
                        Text(
                            text = "v${BuildConfig.VERSION_NAME}", // Sürüm bilgisi
                            style = MaterialTheme.typography.bodySmall.copy( // Daha küçük bir stil
                                fontSize = MaterialTheme.typography.titleLarge.fontSize / 2.5 // Yaklaşık yarısı
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant, // Biraz daha soluk bir renk
                            modifier = Modifier.padding(top = 9.dp) // Ana başlıkla dikey hizalamayı iyileştirmek için hafif üst padding
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { onNavigate(Screen.SETTINGS) }) { // Ayarlar ekranına git
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(id = R.string.settings_title)
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), // İki sütunlu bir ızgara
            contentPadding = PaddingValues(16.dp), // Grid çevresine ve iç boşluklar
            verticalArrangement = Arrangement.spacedBy(16.dp), // Kartlar arası dikey boşluk
            horizontalArrangement = Arrangement.spacedBy(16.dp), // Kartlar arası yatay boşluk
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            items(tools.size) { index ->
                val tool = tools[index]
                ToolCard(
                    toolItem = tool,
                    onClick = { onNavigate(tool.screenRoute) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolCard(
    toolItem: ToolItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f), // Kartları kare yapar (isteğe bağlı)
        shape = RoundedCornerShape(20.dp), // Expressive köşe yuvarlaklığı
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize() // Kartın içini doldur
                .padding(16.dp), // Kart içi padding
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // İçeriği ortala
        ) {
            Icon(
                painter = painterResource(id = toolItem.iconResId),
                contentDescription = toolItem.title,
                modifier = Modifier.size(48.dp), // İkon boyutu
                tint = MaterialTheme.colorScheme.primary // İkon rengi
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = toolItem.title,
                style = MaterialTheme.typography.titleMedium, // Kart başlığı stili
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2, // Başlığın en fazla iki satır olmasını sağla
                overflow = TextOverflow.Ellipsis // Taşarsa ... ile göster
            )
        }
    }
}

// Yemek Tarifleri özelliği için yer tutucu ekran
@Composable
fun RecipeFeatureScreenPlaceholder(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit // Geri dönmek için callback
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Yemek Tarifleri Ekranı (Yakında!)",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateBack) {
            Text("Ana Ekrana Geri Dön")
        }
        // Burası RecipeScreen.kt'nin içeriği ile dolacak
    }
}

@Preview(showBackground = true, name = "Main App Content Preview")
@Composable
fun MainAppContentPreview() {
    LifeToolsTheme { // Ana tema Composable'ınız
        MainAppContent(
            onNavigate = { selectedScreen ->
                // Preview içinde bu callback bir şey yapmak zorunda değil,
                // sadece MainAppContent'in doğru şekilde çağrılmasını sağlar.
                // İstersen Logcat'e bir mesaj yazdırabilirsin:
                // Log.d("MainAppContentPreview", "Navigate to: $selectedScreen")
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RecipeFeatureScreenPlaceholderPreview() {
    LifeToolsTheme {
        RecipeFeatureScreenPlaceholder(onNavigateBack = {})
    }
}