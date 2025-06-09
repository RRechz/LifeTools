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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.babelsoftware.lifetools.ui.OnboardingScreen
import com.babelsoftware.lifetools.ui.main.MainAppContent
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

// ToolItem data class'ını da string resource ID'si alacak şekilde güncelleyebiliriz.
data class ToolItem(
    @StringRes val titleResId: Int,
    @DrawableRes val iconResId: Int,
    val screenRoute: Screen
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleInstallIntent(intent) // onCreate içinde intent kontrolü

        setContent {
            // ---- State Yönetimi ----
            val settingsViewModel: SettingsViewModel = viewModel()
            // ViewModel'dan tek bir uiState topluyoruz
            val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

            // ---- Navigasyon State'i ----
            var currentScreen by remember { mutableStateOf(Screen.MAIN) }

            // ---- Ana Tema Sarmalayıcısı ----
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
                    when (uiState.isOnboardingCompleted) {
                        null -> {
                            // Durum yükleniyor, bir bekleme göstergesi göster
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        false -> {
                            // Kurulum tamamlanmamış, Onboarding ekranını göster
                            OnboardingScreen(onOnboardingComplete = {})
                        }
                        true -> {
                            // Kurulum tamamlanmış, ana navigasyon mantığını çalıştır
                            when (currentScreen) {
                                Screen.MAIN -> MainAppContent(onNavigate = { currentScreen = it })
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
    } // <-- onCreate fonksiyonunun BİTİŞİ

    // DİĞER FONKSİYONLAR BU SEVİYEDE, SINIFIN DOĞRUDAN ÜYESİ OLMALI

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleInstallIntent(intent)
    }

    private fun handleInstallIntent(intent: Intent?) {
        if (intent?.action == "ACTION_INSTALL_UPDATE") {
            val fileUri = intent.data
            if (fileUri != null) {
                triggerApkInstall(fileUri)
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

} // <<<--- MainActivity SINIFININ BİTİŞİ