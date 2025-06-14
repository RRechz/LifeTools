package com.babelsoftware.lifetools.ui.main

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdateAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.babelsoftware.lifetools.BuildConfig
import com.babelsoftware.lifetools.R
import com.babelsoftware.lifetools.ToolItem
import com.babelsoftware.lifetools.ui.main.GlanceHeader
import com.babelsoftware.lifetools.ui.navigation.Screen
import com.babelsoftware.lifetools.ui.theme.LifeToolsTheme
import java.text.SimpleDateFormat
import java.util.*

// --- DATA ve SABİTLER ---

private val mainTools = listOf(
    ToolItem(titleResId = R.string.recipe_screen_title, iconResId = R.drawable.recipes_with_ai, screenRoute = Screen.RECIPES),
    ToolItem(titleResId = R.string.movie_screen_title, iconResId = R.drawable.movie_with_ai, screenRoute = Screen.MOVIES),
    ToolItem(titleResId = R.string.truth_or_dare, iconResId = R.drawable.truth_or_dare_with_ai, screenRoute = Screen.TRUTH_OR_DARE_HUB)
)

private val upcomingTools = listOf(
    ToolItem(titleResId = R.string.tool_sd_to_hd_title, iconResId = R.drawable.sd_to_hd_with_ai, screenRoute = Screen.MAIN),
    ToolItem(titleResId = R.string.tool_todo_with_ai_title, iconResId = R.drawable.todo_with_ai, screenRoute = Screen.MAIN),
    ToolItem(titleResId = R.string.tool_ai_effect_photos_title, iconResId = R.drawable.photo_effect_with_ai, screenRoute = Screen.MAIN)
)

// --- ANA EKRAN COMPOSABLE'I ---

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainAppContent(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = viewModel(),
    onNavigate: (Screen) -> Unit
) {
    val mainUiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current // HATA DÜZELTMESİ: Toast mesajı için context'i burada alıyoruz.

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(id = R.string.app_main_title), style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "v${BuildConfig.VERSION_NAME}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.titleLarge.fontSize / 1.9),
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
                        Icon(Icons.Filled.Settings, contentDescription = stringResource(id = R.string.settings_title))
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                GlanceHeader(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    tipOfTheDay = mainUiState.tipOfTheDay,
                    isUpdateAvailable = mainUiState.isUpdateAvailable,
                    latestVersionName = mainUiState.latestVersionName
                )
            }
            item {
                ToolsSection(
                    title = "Araçların",
                    tools = mainTools,
                    onToolClick = { tool -> onNavigate(tool.screenRoute) },
                    onSeeAllClick = { /* TODO */ }
                )
            }
            item {
                ToolsSection(
                    title = "Yakında Gelecekler",
                    tools = upcomingTools,
                    onToolClick = {
                        // HATA DÜZELTMESİ: 'context' değişkenini burada kullanıyoruz.
                        Toast.makeText(context, R.string.coming_soon, Toast.LENGTH_SHORT).show()
                    },
                    onSeeAllClick = null
                )
            }
        }
    }
}


// --- YARDIMCI COMPOSABLE'LAR ---

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ToolsSection(
    title: String,
    tools: List<ToolItem>,
    onToolClick: (ToolItem) -> Unit,
    onSeeAllClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            if (onSeeAllClick != null) {
                TextButton(onClick = onSeeAllClick) { Text("Tümü") }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = tools, key = { it.titleResId }) { tool ->
                ToolCard(
                    toolItem = tool,
                    onClick = { onToolClick(tool) },
                    modifier = Modifier
                        .width(160.dp)
                        // .animateItemPlacement() // Anotasyon doğru olduğu için artık çalışacaktır.
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
    val title = stringResource(id = toolItem.titleResId)
    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(painter = painterResource(id = toolItem.iconResId), contentDescription = title, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun DefaultGlanceContent(tipOfTheDay: String) {
    val calendar = Calendar.getInstance()
    val welcomeText = when (calendar.get(Calendar.HOUR_OF_DAY)) {
        in 6..11 -> "Günaydın!"
        in 12..17 -> "Tünaydın!"
        else -> "İyi akşamlar!"
    }
    val dateFormat = SimpleDateFormat("d MMMM EEEE", Locale("tr"))
    val dateText = dateFormat.format(calendar.time)

    Column {
        Text(text = tipOfTheDay, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Normal, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = welcomeText, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = dateText, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun UpdateAvailableContent(versionName: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = Icons.Filled.SystemUpdateAlt, contentDescription = "Güncelleme Mevcut", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text("Yeni Güncelleme Mevcut!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("$versionName sürümü için Ayarlar'ı kontrol et.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// --- PREVIEW ---

@Preview(showBackground = true, name = "Main Screen Preview")
@Composable
fun MainAppContentPreview() {
    LifeToolsTheme {
        MainAppContent(onNavigate = {})
    }
}