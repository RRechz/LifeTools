package com.babelsoftware.lifetools.ui.settings

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.babelsoftware.lifetools.BuildConfig
import com.babelsoftware.lifetools.R
import com.babelsoftware.lifetools.ui.theme.LifeToolsTheme

// --- Modern SettingItem ---
@Composable
fun SettingItem(
    modifier: Modifier = Modifier,
    icon: ImageVector?,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    toggleState: MutableState<Boolean>? = null
) {
    val rowModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Row(
        modifier = rowModifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.let {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = it,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
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

        trailingContent?.invoke()

        toggleState?.let {
            Switch(
                checked = it.value,
                onCheckedChange = { checked -> it.value = checked }
            )
        } ?: run {
            if (onClick != null && trailingContent == null) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// --- Başlık ---
@Composable
fun CategoryTitleDecorated(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(24.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// --- Gradient Kart ---
@Composable
fun GradientCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.0f)
        )
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(16.dp),
            content = content
        )
    }
}

// --- Ana SettingsScreen ---
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

    val notificationsEnabled = remember { mutableStateOf(true) }

    LaunchedEffect(updateUiState.updateAvailable) {
        if (updateUiState.updateAvailable) {
            showUpdateDialog = true
        }
    }

    if (showUpdateDialog) {
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            confirmButton = {
                TextButton(onClick = { showUpdateDialog = false }) {
                    Text("Tamam")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showUpdateDialog = false
                    updateUiState.downloadUrl?.let { url ->
                        uriHandler.openUri(url)
                    }
                }) {
                    Text("Git")
                }
            },
            title = { Text("Güncelleme Mevcut") },
            text = {
                Text("Yeni sürüm bulundu: ${updateUiState.latestVersionName}\nLütfen en yeni özellikler için güncelleyin.")
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri")
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
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // --- Kişiselleştirme Kartı ---
            item {
                GradientCard {
                    CategoryTitleDecorated("Kişiselleştirme")

                    SettingItem(
                        icon = Icons.Filled.Palette,
                        title = "Tema ve Renkler",
                        subtitle = "Açık/Koyu mod, dinamik renkler, ana renk",
                        onClick = onNavigateToAppearanceSettings
                    )

                    Divider(Modifier.padding(start = 64.dp))

                    SettingItem(
                        icon = Icons.Filled.Language,
                        title = stringResource(R.string.language),
                        subtitle = stringResource(R.string.language_subtitle),
                        onClick = onNavigateToLanguageSettings
                    )
                }
            }

            // --- Genel Kartı ---
            item {
                GradientCard {
                    CategoryTitleDecorated("Genel")

                    SettingItem(
                        icon = Icons.Filled.Notifications,
                        title = "Bildirimler",
                        subtitle = "Anlık uyarılar ve hatırlatıcılar",
                        toggleState = notificationsEnabled
                    )
                }
            }

            // --- Uygulama Kartı ---
            item {
                GradientCard {
                    CategoryTitleDecorated("Uygulama")

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
                        onClick = { if (!updateUiState.isChecking) updateViewModel.checkForUpdates() },
                        trailingContent = {
                            if (updateUiState.isChecking)
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            else if (updateUiState.updateAvailable)
                                Icon(
                                    Icons.Filled.NewLabel,
                                    contentDescription = "Güncelleme mevcut",
                                    tint = MaterialTheme.colorScheme.error
                                )
                        }
                    )

                    Divider(Modifier.padding(start = 64.dp))

                    SettingItem(
                        icon = Icons.Filled.Info,
                        title = stringResource(R.string.version),
                        subtitle = "v${BuildConfig.VERSION_NAME}",
                        onClick = null,
                        trailingContent = {}
                    )

                    Divider(Modifier.padding(start = 64.dp))

                    SettingItem(
                        icon = Icons.Filled.Source,
                        title = stringResource(R.string.source_code),
                        subtitle = stringResource(R.string.source_code_subtitle),
                        onClick = {
                            uriHandler.openUri("https://github.com/RRechz/LifeTools")
                        }
                    )
                }
            }
        }
    }
}