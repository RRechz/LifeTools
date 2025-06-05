// File: ui/settings/LanguageSettingsScreen.kt
package com.babelsoftware.lifetools.ui.settings

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.babelsoftware.lifetools.R
import com.babelsoftware.lifetools.model.AppLanguage // AppLanguage enum'ı için import
import com.babelsoftware.lifetools.ui.LanguageSelector // Onboarding'den LanguageSelector'ı import et
import com.babelsoftware.lifetools.ui.theme.LifeToolsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSettingsScreen(
    settingsViewModel: SettingsViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val currentLanguage by settingsViewModel.appLanguage.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dil Ayarları") }, // stringResource kullanılabilir
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()) // Çok fazla ayar olursa diye scroll
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dil Seçim Paneli (Onboarding'deki LanguageStepContent'e benzer bir Surface içinde)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)), // Expressive köşe yuvarlaklığı
                shadowElevation = 4.dp,
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
            ) {
                LanguageSelector(
                    selectedLanguage = currentLanguage,
                    onLanguageSelected = { newLanguage ->
                        settingsViewModel.updateLanguage(newLanguage)
                        // TODO: Kullanıcıya dilin uygulama yeniden başlatıldığında
                        // veya bir sonraki açılışta değişeceğine dair bir bilgi verilebilir.
                        // Veya anında dil değiştirme mekanizması (daha karmaşık) tetiklenebilir.
                    },
                    modifier = Modifier.padding(16.dp) // Surface iç padding'i
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Not: Dil değişikliğinin tam olarak uygulanması için uygulamanın yeniden başlatılması gerekebilir.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Language Settings Screen Preview")
@Composable
fun LanguageSettingsScreenPreview() {
    LifeToolsTheme {
        LanguageSettingsScreen(
            // settingsViewModel = viewModel(), // Preview'da sorun çıkarabilir
            onNavigateBack = {}
        )
    }
}