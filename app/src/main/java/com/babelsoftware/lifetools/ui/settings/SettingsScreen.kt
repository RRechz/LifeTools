// File: ui/settings/SettingsScreen.kt
package com.babelsoftware.lifetools.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.babelsoftware.lifetools.BuildConfig
import com.babelsoftware.lifetools.R
import com.babelsoftware.lifetools.ui.theme.LifeToolsTheme

// --- Bu kısım ui/components/SettingComponents.kt gibi ayrı bir dosyaya taşınabilir ---

@Composable
fun getSettingItemShape(
    isFirst: Boolean,
    isLast: Boolean,
    cornerRadius: Dp = 20.dp
): Shape {
    return when {
        isFirst && isLast -> RoundedCornerShape(cornerRadius) // Tek başına
        isFirst -> RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius)
        isLast -> RoundedCornerShape(bottomStart = cornerRadius, bottomEnd = cornerRadius)
        else -> RectangleShape
    }
}

@Composable
fun SettingItem(
    modifier: Modifier = Modifier,
    icon: ImageVector?,
    title: String,
    subtitle: String? = null,
    shape: Shape,
    onClick: (() -> Unit)? = null, // Tıklanabilir olmayabilir, bu yüzden nullable
    trailingContent: (@Composable () -> Unit)? = null
) {
    val rowModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .then(rowModifier), // Tıklanabilirlik için modifier'ı buraya uygula
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = if (subtitle == null) 16.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = title,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                subtitle?.let {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            // Sağ taraftaki içerik
            if (trailingContent != null) {
                trailingContent()
            } else if (onClick != null) { // Sadece tıklanabilirse ve özel içerik yoksa oku göster
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SettingsCategoryTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp, start = 4.dp)
    )
}

// --- Ana Ekran Composable'ı ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = viewModel(),
    updateViewModel: UpdateViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToAppearanceSettings: () -> Unit,
    onNavigateToLanguageSettings: () -> Unit
) {
    val updateUiState by updateViewModel.uiState.collectAsStateWithLifecycle()
    var showUpdateDialog by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(updateUiState.updateAvailable) {
        if (updateUiState.updateAvailable) {
            showUpdateDialog = true
        }
    }

    if (showUpdateDialog) {
        // ... AlertDialog kodu aynı ...
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.settings_title)) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp) // Gruplar arası boşluk
        ) {
            // --- GÖRÜNÜM & GENEL GRUBU ---
            item {
                Column { // Öğeleri Divider ile birleştirmek için Column içine alıyoruz
                    SettingsCategoryTitle(title = "Kişiselleştirme") // Ortak başlık
                    SettingItem(
                        icon = Icons.Filled.Palette,
                        title = "Tema ve Renkler",
                        subtitle = "Açık/Koyu mod, dinamik renkler, ana renk",
                        shape = getSettingItemShape(isFirst = true, isLast = false),
                        onClick = onNavigateToAppearanceSettings
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp)) // İkon hizasından Divider
                    SettingItem(
                        icon = Icons.Filled.Language,
                        title = stringResource(R.string.language),
                        subtitle = stringResource(R.string.language_subtitle),
                        shape = getSettingItemShape(isFirst = false, isLast = true),
                        onClick = onNavigateToLanguageSettings
                    )
                }
            }

            // --- GÜNCELLEME VE BİLGİ GRUBU ---
            item {
                Column {
                    SettingsCategoryTitle(title = "Uygulama")
                    // Güncelleme Öğesi
                    val updateSubtitle = when {
                        updateUiState.isChecking -> stringResource(id = R.string.checking_for_updates)
                        updateUiState.updateCheckError != null -> updateUiState.updateCheckError!!
                        updateUiState.updateAvailable -> "Yeni sürüm mevcut: ${updateUiState.latestVersionName}"
                        else -> "Uygulamanız güncel"
                    }
                    SettingItem(
                        icon = Icons.Filled.SystemUpdateAlt,
                        title = stringResource(id = R.string.check_for_updates),
                        subtitle = updateSubtitle,
                        shape = getSettingItemShape(isFirst = true, isLast = false),
                        onClick = { if (!updateUiState.isChecking) updateViewModel.checkForUpdates() },
                        trailingContent = {
                            if (updateUiState.isChecking) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            else if (updateUiState.updateAvailable) Icon(Icons.Filled.NewLabel, "Güncelleme mevcut", tint = MaterialTheme.colorScheme.error)
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                    // Versiyon Öğesi
                    SettingItem(
                        icon = Icons.Filled.Info,
                        title = stringResource(R.string.version),
                        subtitle = "v${BuildConfig.VERSION_NAME}",
                        shape = getSettingItemShape(isFirst = false, isLast = false),
                        onClick = null, // Tıklanabilir değil
                        trailingContent = {} // Sağdaki oku kaldırır
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                    // Kaynak Kodu Öğesi
                    SettingItem(
                        icon = Icons.Filled.Source,
                        title = stringResource(R.string.source_code),
                        subtitle = stringResource(R.string.source_code_subtitle),
                        shape = getSettingItemShape(isFirst = false, isLast = true),
                        onClick = { uriHandler.openUri("https://github.com/RRechz/LifeTools") }
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true, name = "Settings Screen Preview")
@Composable
fun SettingsScreenPreview() {
    LifeToolsTheme {
        SettingsScreen(
            onNavigateBack = {},
            onNavigateToAppearanceSettings = {},
            onNavigateToLanguageSettings = {}
        )
    }
}