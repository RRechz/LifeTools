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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.babelsoftware.lifetools.ui.DynamicColorSwitch
import com.babelsoftware.lifetools.ui.StaticAccentColorSelector
import com.babelsoftware.lifetools.ui.ThemePreviewPane
import com.babelsoftware.lifetools.ui.ThemeSelector
import com.babelsoftware.lifetools.ui.predefinedStaticColors
import com.babelsoftware.lifetools.ui.theme.LifeToolsTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsScreen(
    settingsViewModel: SettingsViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    // İYİLEŞTİRME 1: Artık 3 ayrı state yerine tek bir uiState topluyoruz.
    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp) // Elemanlar arasına genel boşluk
        ) {
            // Tema Önizleme Paneli
            ThemePreviewPane(
                baseThemeMode = uiState.appTheme,
                isDynamicColorsEnabled = uiState.isDynamicColorsEnabled,
                staticAccentColor = uiState.staticAccentColor
            )

            // Dinamik Renkler Anahtarı Grubu
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                shadowElevation = 2.dp,
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
            ) {
                DynamicColorSwitch(
                    checked = uiState.isDynamicColorsEnabled,
                    onCheckedChange = { settingsViewModel.updateDynamicColorsEnabled(it) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Statik Ana Renk Seçici Grubu
            AnimatedVisibility(visible = !uiState.isDynamicColorsEnabled || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp)),
                    shadowElevation = 2.dp,
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                ) {
                    StaticAccentColorSelector(
                        availableColors = predefinedStaticColors,
                        selectedColor = uiState.staticAccentColor,
                        onColorSelected = { settingsViewModel.updateStaticAccentColor(it) },
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Açık/Koyu/Sistem Teması Seçici Grubu
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                shadowElevation = 2.dp,
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
            ) {
                ThemeSelector(
                    currentTheme = uiState.appTheme,
                    onThemeSelected = { settingsViewModel.updateTheme(it) },
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Appearance Settings Preview")
@Composable
fun AppearanceSettingsScreenPreview() {
    LifeToolsTheme {
        // Bu preview'un çalışması için state hoisting ile
        // AppearanceSettingsScreen'in ViewModel yerine state'leri alması gerekir.
        // Şimdilik bu şekilde bırakabiliriz, canlı test daha doğru sonuç verir.
        AppearanceSettingsScreen(
            onNavigateBack = {}
        )
    }
}