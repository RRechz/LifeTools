package com.babelsoftware.lifetools

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.babelsoftware.lifetools.ui.OnboardingScreen
import com.babelsoftware.lifetools.ui.movie.MovieScreen
import com.babelsoftware.lifetools.ui.navigation.Screen
import com.babelsoftware.lifetools.ui.recipe.RecipeScreen
import com.babelsoftware.lifetools.ui.settings.AppearanceSettingsScreen
import com.babelsoftware.lifetools.ui.settings.LanguageSettingsScreen
import com.babelsoftware.lifetools.ui.settings.SettingsScreen
import com.babelsoftware.lifetools.ui.settings.SettingsViewModel
import com.babelsoftware.lifetools.ui.theme.LifeToolsTheme
import com.babelsoftware.lifetools.ui.truthordare.SpinnerScreen
import com.babelsoftware.lifetools.ui.truthordare.SpinnerViewModel
import com.babelsoftware.lifetools.ui.truthordare.SpinnerWithQuestionsScreen
import com.babelsoftware.lifetools.ui.truthordare.TruthOrDareHubScreen
import com.babelsoftware.lifetools.ui.truthordare.TruthQuestionsScreen
import androidx.compose.animation.core.tween // animateItemPlacement için
import androidx.compose.foundation.ExperimentalFoundationApi // animateItemPlacement için
import androidx.compose.foundation.lazy.grid.GridItemSpan // span için


// İYİLEŞTİRME 1: 'tools' listesi artık Composable dışında, sadece bir kez oluşturuluyor.
private val mainScreenTools = listOf(
    ToolItem(
        titleResId = R.string.recipe_screen_title,
        iconResId = R.drawable.recipes_with_ai,
        screenRoute = Screen.RECIPES
    ),
    ToolItem(
        titleResId = R.string.movie_screen_title,
        iconResId = R.drawable.movie_with_ai,
        screenRoute = Screen.MOVIES
    ),
    ToolItem(
        titleResId = R.string.truth_or_dare,
        iconResId = R.drawable.truth_or_dare_with_ai,
        screenRoute = Screen.TRUTH_OR_DARE_HUB
    )
    // Gelecekte yeni bir araç eklemek için buraya yeni bir ToolItem eklemen yeterli.
)

// ToolItem data class'ını da string resource ID'si alacak şekilde güncelleyebiliriz.
data class ToolItem(
    @StringRes val titleResId: Int, // title: String yerine
    @DrawableRes val iconResId: Int,
    val screenRoute: Screen
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleInstallIntent(intent) // Uygulama açıldığında intent kontrolü

        setContent {
            // ---- State Yönetimi ----
            val settingsViewModel: SettingsViewModel = viewModel()
            val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

            // ---- Navigasyon State'i ----
            var currentScreen by remember { mutableStateOf(Screen.MAIN) }

            // ---- Ana Tema Sarmalayıcısı ----
            // LifeToolsTheme'i çağırırken:
            LifeToolsTheme(
                appThemeMode = uiState.appTheme,
                isDynamicColorsEnabled = uiState.isDynamicColorsEnabled,
                staticAccentColor = uiState.staticAccentColor
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // ---- Ana Navigasyon Mantığı ----
                    if (!uiState.isOnboardingCompleted) {
                        OnboardingScreen(
                            onOnboardingComplete = { /* Kurulum bittiğinde bu lambda tetiklenir */ }
                        )
                    } else {
                        when (currentScreen) {
                            Screen.MAIN -> MainAppContent(
                                onNavigate = { destinationScreen ->
                                    currentScreen = destinationScreen
                                }
                            )
                            Screen.SETTINGS -> SettingsScreen(
                                onNavigateBack = { currentScreen = Screen.MAIN },
                                onNavigateToAppearanceSettings = { currentScreen = Screen.APPEARANCE_SETTINGS },
                                onNavigateToLanguageSettings = { currentScreen = Screen.LANGUAGE_SETTINGS }
                            )
                            Screen.APPEARANCE_SETTINGS -> AppearanceSettingsScreen(
                                onNavigateBack = { currentScreen = Screen.SETTINGS }
                            )
                            Screen.LANGUAGE_SETTINGS -> LanguageSettingsScreen(
                                onNavigateBack = { currentScreen = Screen.SETTINGS }
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
                            Screen.TRUTH_QUESTIONS -> TruthQuestionsScreen(
                                onNavigateBack = { currentScreen = Screen.TRUTH_OR_DARE_HUB }
                            )
                            Screen.SPINNER_GAME -> {
                                val spinnerViewModel: SpinnerViewModel = viewModel()
                                val spinnerUiState by spinnerViewModel.uiState.collectAsStateWithLifecycle()
                                SpinnerScreen(
                                    uiState = spinnerUiState,
                                    onInputChanged = spinnerViewModel::onInputChanged,
                                    onAddItem = spinnerViewModel::addItem,
                                    onRemoveItem = spinnerViewModel::removeItem,
                                    onReorderItems = spinnerViewModel::reorderItems,
                                    onSpinWheel = spinnerViewModel::spinWheel,
                                    onSpinAnimationCompleted = spinnerViewModel::setSpinningCompleted,
                                    onClearSelectedItem = spinnerViewModel::clearSelectedItem,
                                    onClearErrorMessage = spinnerViewModel::clearErrorMessage,
                                    onNavigateBack = { currentScreen = Screen.TRUTH_OR_DARE_HUB }
                                )
                            }
                            Screen.SPINNER_WITH_QUESTIONS -> SpinnerWithQuestionsScreen(
                                onNavigateBack = { currentScreen = Screen.TRUTH_OR_DARE_HUB }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) { // <<<--- DÜZELTME: Intent? yerine Intent
        super.onNewIntent(intent)
        // Eğer aktivite zaten açıksa ve yeni bir intent gelirse (örn. bildirimden)
        handleInstallIntent(intent)
    }

    private fun handleInstallIntent(intent: Intent?) { // Burası nullable kalabilir
        if (intent?.action == "ACTION_INSTALL_UPDATE") {
            val fileUri = intent.data
            if (fileUri != null) {
                triggerApkInstall(fileUri)
                // Intent'in tekrar işlenmemesi için action'ı temizle
                setIntent(Intent(this, MainActivity::class.java))
            }
        }
    }

    private fun triggerApkInstall(apkUri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!packageManager.canRequestPackageInstalls()) {
                val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(settingsIntent)
                return
            }
        }

        try {
            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(installIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Uygulama yüklenemedi. Lütfen 'İndirilenler' klasörünüzü kontrol edin.", Toast.LENGTH_LONG).show()
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class) // Yeni anotasyon eklendi
@Composable
fun MainAppContent(
    modifier: Modifier = Modifier,
    onNavigate: (Screen) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(id = R.string.app_main_title),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "v${BuildConfig.VERSION_NAME}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = MaterialTheme.typography.titleLarge.fontSize / 1.9
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { onNavigate(Screen.SETTINGS) }) {
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
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // items fonksiyonu 'androidx.compose.foundation.lazy.grid' paketinden gelmeli
            items(
                items = mainScreenTools,
                key = { toolItem -> toolItem.screenRoute } // Her öğe için benzersiz anahtar
            ) { tool ->
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
    val title = stringResource(id = toolItem.titleResId) // String'i burada alıyoruz

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = toolItem.iconResId),
                contentDescription = title, // Erişilebilirlik için
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


@Preview(showBackground = true, name = "Main App Content Preview")
@Composable
fun MainAppContentPreview() {
    LifeToolsTheme {
        MainAppContent(
            onNavigate = {}
        )
    }
}