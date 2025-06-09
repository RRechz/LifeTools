package com.babelsoftware.lifetools.ui.main

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.babelsoftware.lifetools.BuildConfig
import com.babelsoftware.lifetools.R
import com.babelsoftware.lifetools.ToolItem
import com.babelsoftware.lifetools.ui.navigation.Screen
import com.babelsoftware.lifetools.ui.theme.LifeToolsTheme

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
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainAppContent(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = viewModel(),
    onNavigate: (Screen) -> Unit
) {
    val mainUiState by mainViewModel.uiState.collectAsStateWithLifecycle()
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
            columns = GridCells.Adaptive(minSize = 160.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                // GlanceHeader'a ViewModel'dan gelen tüm ilgili state'leri geçiyoruz
                GlanceHeader(
                    tipOfTheDay = mainUiState.tipOfTheDay,
                    isUpdateAvailable = mainUiState.isUpdateAvailable,
                    latestVersionName = mainUiState.latestVersionName
                )
            }
            items(
                items = mainScreenTools,
                key = { toolItem -> toolItem.screenRoute }
            ) { tool ->
                ToolCard(
                    toolItem = tool,
                    onClick = { onNavigate(tool.screenRoute) },
                    modifier = Modifier // .animateItemPlacement(
                        // animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                    // )
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
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                contentDescription = title,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Preview(showBackground = true, name = "Main App Content Preview")
@Composable
fun MainAppContentPreview() {
    LifeToolsTheme {
        MainAppContent(onNavigate = {})
    }
}

@Preview(showBackground = true, name = "Glance Header - Update Available")
@Composable
fun GlanceHeaderUpdatePreview() {
    LifeToolsTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            GlanceHeader(
                tipOfTheDay = "Bu bir ipucudur.",
                isUpdateAvailable = true,
                latestVersionName = "v0.2.0-beta"
            )
        }
    }
}