// File: ui/settings/AppearanceSettingsScreen.kt
package com.babelsoftware.lifetools.ui.settings

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.babelsoftware.lifetools.R
import com.babelsoftware.lifetools.data.AppTheme // AppTheme enum'ı için
import com.babelsoftware.lifetools.ui.ThemePreviewPane // Onboarding'den import
import com.babelsoftware.lifetools.ui.DynamicColorSwitch // Onboarding'den import
import com.babelsoftware.lifetools.ui.StaticAccentColorSelector // Onboarding'den import
import com.babelsoftware.lifetools.ui.ThemeSelector // Onboarding'den import
import com.babelsoftware.lifetools.ui.predefinedStaticColors // Onboarding'den import
import com.babelsoftware.lifetools.ui.theme.LifeToolsTheme

// calculateOnColor fonksiyonu ui/theme/Color.kt içinde olmalı
// import com.babelsoftware.lifetools.ui.theme.calculateOnColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsScreen(
    settingsViewModel: SettingsViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val currentBaseTheme by settingsViewModel.appTheme.collectAsStateWithLifecycle()
    val isDynamicColorsEnabled by settingsViewModel.isDynamicColorsEnabled.collectAsStateWithLifecycle()
    val currentStaticAccentColor by settingsViewModel.staticAccentColor.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Görünüm Ayarları") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tema Önizleme Paneli
            ThemePreviewPane(
                baseThemeMode = currentBaseTheme,
                isDynamicColorsEnabled = isDynamicColorsEnabled,
                staticAccentColor = currentStaticAccentColor
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Dinamik Renkler Anahtarı
            Surface( // Onboarding'deki gibi bir Surface içine alabiliriz
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                shadowElevation = 2.dp,
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
            ) {
                DynamicColorSwitch(
                    checked = isDynamicColorsEnabled,
                    onCheckedChange = { settingsViewModel.updateDynamicColorsEnabled(it) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Statik Ana Renk Seçici (Dinamik renkler kapalıysa görünür)
            AnimatedVisibility(visible = !isDynamicColorsEnabled || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                Surface( // Onboarding'deki gibi bir Surface içine alabiliriz
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp) // Üstteki elemanla arasında biraz boşluk
                        .clip(RoundedCornerShape(20.dp)),
                    shadowElevation = 2.dp,
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                ) {
                    StaticAccentColorSelector(
                        availableColors = predefinedStaticColors, // Onboarding'den gelen liste
                        selectedColor = currentStaticAccentColor,
                        onColorSelected = { settingsViewModel.updateStaticAccentColor(it) },
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Açık/Koyu/Sistem Teması Seçici
            Surface( // Onboarding'deki gibi bir Surface içine alabiliriz
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(20.dp)),
                shadowElevation = 2.dp,
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
            ) {
                ThemeSelector( // Slider versiyonu
                    currentTheme = currentBaseTheme,
                    onThemeSelected = { settingsViewModel.updateTheme(it) },
                    modifier = Modifier.padding(16.dp)
                    // enabled = true // Her zaman etkin olabilir veya bir koşula bağlanabilir
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Appearance Settings Preview")
@Composable
fun AppearanceSettingsScreenPreview() {
    LifeToolsTheme {
        AppearanceSettingsScreen(
            // Preview için sahte bir ViewModel sağlamak daha doğru olurdu,
            // ama SettingsViewModel Application context gerektirmediği sürece
            // viewModel() önizlemede basit bir instance oluşturabilir.
            // Eğer sorun olursa, state hoisting ile düzenlemek gerekir.
            onNavigateBack = {}
        )
    }
}