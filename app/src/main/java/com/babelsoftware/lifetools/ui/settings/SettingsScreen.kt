package com.babelsoftware.lifetools.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NewLabel
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Source
import androidx.compose.material.icons.filled.SystemUpdateAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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


// Ayar öğelerinin grup içindeki konumuna göre şeklini belirleyen yardımcı fonksiyon
@Composable
fun getSettingItemShape(
    isFirst: Boolean = false,
    isLast: Boolean = false,
    isStandalone: Boolean = false,
    cornerRadius: Dp = 16.dp
): Shape {
    return when {
        isStandalone -> RoundedCornerShape(cornerRadius)
        isFirst -> RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius, bottomStart = 0.dp, bottomEnd = 0.dp)
        isLast -> RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = cornerRadius, bottomEnd = cornerRadius)
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
    onClick: () -> Unit,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick),
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
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                subtitle?.let {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (trailingContent != null) {
                trailingContent()
            } else {
                // Eğer onClick tanımlıysa ve özel trailingContent yoksa ok göster
                if (onClick != {}) { // Basit bir kontrol, daha iyisi dışarıdan bir flag ile yapılabilir
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = viewModel(), // Ana ayarlar için
    updateViewModel: UpdateViewModel = viewModel(), // Güncelleme için yeni ViewModel
    onNavigateBack: () -> Unit,
    onNavigateToAppearanceSettings: () -> Unit,
    onNavigateToLanguageSettings: () -> Unit
) {
    val updateUiState by updateViewModel.uiState.collectAsStateWithLifecycle()
    var showUpdateDialog by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    // Güncelleme mevcut olduğunda ve henüz dialog gösterilmediyse dialog'u tetikle
    LaunchedEffect(updateUiState.updateAvailable) {
        if (updateUiState.updateAvailable) {
            showUpdateDialog = true
        }
    }

    if (showUpdateDialog) {
        AlertDialog(
            onDismissRequest = {
                showUpdateDialog = false
                updateViewModel.clearUpdateState() // Dialog kapatıldığında state'i temizle
            },
            title = { Text(stringResource(id = R.string.update_available_title)) },
            text = {
                Text(
                    stringResource(
                        id = R.string.update_available_message,
                        updateUiState.latestVersionName ?: "Bilinmeyen Sürüm"
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUpdateDialog = false
                        // ViewModel'daki indirme fonksiyonunu çağırıyoruz
                        updateViewModel.startDownload()
                        // clearUpdateState() fonksiyonunu şimdilik çağırmayalım,
                        // indirme başladıktan sonra belki bir "indiriliyor" mesajı göstermek isteriz.
                    }
                ) { Text(stringResource(id = R.string.download_update)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showUpdateDialog = false
                    updateViewModel.clearUpdateState()
                }) { Text(stringResource(id = R.string.later)) }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.onboarding_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp) // Gruplar/öğeler arası genel boşluk
        ) {
            // --- Görünüm Ayarları Grubu ---
            item {
                Text(
                    stringResource(R.string.appearance), // stringResource(id = R.string.settings_category_appearance)
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp, top = 8.dp) // Grup başlığı için boşluk
                )
            }
            item {
                SettingItem(
                    icon = Icons.Filled.Palette,
                    title = "Tema ve Renkler",
                    subtitle = "Açık/Koyu mod, dinamik renkler, ana renk",
                    shape = getSettingItemShape(isStandalone = true, cornerRadius = 20.dp), // Daha yuvarlak köşeler
                    onClick = onNavigateToAppearanceSettings
                )
            }

            // --- Dil Ayarları Grubu ---
            item {
                Text(
                    "Genel", // stringResource(id = R.string.settings_category_general)
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp, top = 16.dp)
                )
            }
            item {
                SettingItem(
                    icon = Icons.Filled.Language,
                    title = stringResource(R.string.language),
                    subtitle = stringResource(R.string.language_subtitle), // Seçili dili de gösterebiliriz (örn: "Türkçe")
                    shape = getSettingItemShape(isStandalone = true, cornerRadius = 20.dp),
                    onClick = onNavigateToLanguageSettings
                )
            }

            // --- Uygulama Bilgileri Grubu ---
            item {
                Text(
                    stringResource(R.string.about), // stringResource(id = R.string.settings_category_about)
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp, top = 16.dp)
                )
            }
            item {
                SettingItem(
                    icon = Icons.Filled.Info,
                    title = stringResource(R.string.version),
                    subtitle = "v${BuildConfig.VERSION_NAME}",
                    shape = getSettingItemShape(isFirst = true, isLast = false), // Grup örneği için ilk
                    onClick = { /* Tıklayınca bir şey yapmayabilir veya Toast gösterebilir */ },
                    trailingContent = null // Sağdaki oku kaldır
                )
            }
            item {
                SettingItem(
                    icon = Icons.Filled.Source,
                    title = stringResource(R.string.source_code),
                    subtitle = stringResource(R.string.source_code_subtitle),
                    shape = getSettingItemShape(isFirst = false, isLast = true, cornerRadius = 20.dp), // Grup örneği için son
                    onClick = { uriHandler.openUri("https://github.com/RRechz/LifeTools") }
                )
            }
            // --- Uygulama Güncellemesi Grubu ---
            item {
                Text(
                    stringResource(id = R.string.settings_category_update),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp, top = 16.dp)
                )
            }
            item {
                val updateSubtitle = when {
                    updateUiState.isChecking -> stringResource(id = R.string.checking_for_updates)
                    updateUiState.updateCheckError != null -> updateUiState.updateCheckError!!
                    updateUiState.updateAvailable -> "Yeni sürüm mevcut: ${updateUiState.latestVersionName}"
                    updateUiState.latestVersionName != null -> "Uygulamanız güncel (En son: ${updateUiState.latestVersionName})"
                    else -> stringResource(id = R.string.current_version, updateUiState.currentVersion)
                }
                SettingItem(
                    icon = Icons.Filled.SystemUpdateAlt,
                    title = stringResource(id = R.string.check_for_updates),
                    subtitle = updateSubtitle,
                    shape = getSettingItemShape(isFirst = true, isLast = true),
                    onClick = {
                        if (!updateUiState.isChecking) {
                            updateViewModel.checkForUpdates()
                        }
                    },
                    trailingContent = {
                        if (updateUiState.isChecking) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else if (updateUiState.updateAvailable) {
                            // Belki bir "İndir" butonu veya farklı bir ikon
                            Icon(
                                imageVector = Icons.Filled.NewLabel, // Örnek: Yeni etiket ikonu
                                contentDescription = "Güncelleme mevcut",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            }
        }
    }
}

// SettingsScreenPreview'u güncellemeniz gerekebilir, çünkü yeni UpdateViewModel parametresi eklendi.
// Veya UpdateViewModel'ı da SettingsScreen içinde viewModel() ile alabilirsiniz.
// Şimdilik SettingsScreenPreview'u basit tutuyorum, UpdateViewModel'sız.
@Preview(showBackground = true, name = "Settings Screen Preview")
@Composable
fun SettingsScreenPreview() {
    LifeToolsTheme {
        SettingsScreen(
            // updateViewModel = UpdateViewModel(), // Preview için sorun çıkarabilir, state hoisting idealdir.
            onNavigateBack = {},
            onNavigateToAppearanceSettings = {},
            onNavigateToLanguageSettings = {}
        )
    }
}