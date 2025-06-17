package com.babelsoftware.lifetools.ui.main

import android.widget.Toast
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.babelsoftware.lifetools.R
import com.babelsoftware.lifetools.ToolItem
import com.babelsoftware.lifetools.ui.navigation.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- DUMMY DATA ---
data class ToolCategory(val title: String, val tools: List<ToolItem>)

private val toolCategories = listOf(
    ToolCategory(
        title = "Popüler Araçlar",
        tools = listOf(
            ToolItem(R.string.recipe_screen_title, R.drawable.recipes_with_ai, Screen.RECIPES),
            ToolItem(R.string.movie_screen_title, R.drawable.movie_with_ai, Screen.MOVIES)
        )
    ),
    ToolCategory(
        title = "Oyun Araçları",
        tools = listOf(
            ToolItem(R.string.truth_or_dare, R.drawable.truth_or_dare_with_ai, Screen.TRUTH_OR_DARE_HUB)
        )
    )
)

private val upcomingTools = listOf(
    ToolItem(R.string.tool_sd_to_hd_title, R.drawable.sd_to_hd_with_ai, Screen.MAIN),
    ToolItem(R.string.tool_todo_with_ai_title, R.drawable.todo_with_ai, Screen.MAIN),
    ToolItem(R.string.tool_ai_effect_photos_title, R.drawable.photo_effect_with_ai, Screen.MAIN)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = viewModel(),
    onNavigate: (Screen) -> Unit
) {
    val mainUiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showSheet = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        Icons.Default.Menu,
                        contentDescription = stringResource(R.string.menu)
                    )
                }
            },
            containerColor = Color.Transparent,
            modifier = modifier
        ) { innerPadding ->
            LazyColumn(
                contentPadding = innerPadding,
                verticalArrangement = Arrangement.spacedBy(32.dp),
                modifier = Modifier.padding(vertical = 24.dp)
            ) {
                item {
                    FancyHeaderCard(
                        isUpdateAvailable = mainUiState.isUpdateAvailable,
                        latestVersionName = mainUiState.latestVersionName.toString(),
                        tipOfTheDay = mainUiState.tipOfTheDay
                    )
                }

                items(toolCategories) { category ->
                    FancyToolsSection(
                        title = category.title,
                        tools = category.tools,
                        onToolClick = { onNavigate(it.screenRoute) }
                    )
                }

                item {
                    FancyToolsSection(
                        title = stringResource(R.string.coming_soon),
                        tools = upcomingTools,
                        isEnabled = false,
                        onToolClick = {
                            Toast.makeText(context, R.string.coming_soon_MAİN, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = bottomSheetState
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        stringResource(R.string.lifetools_menu),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Button(
                        onClick = {
                            showSheet = false
                            onNavigate(Screen.SETTINGS)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.settings))
                    }
                    Button(
                        onClick = {
                            showSheet = false
                            Toast.makeText(context, R.string.favorite_tools, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.favorite))
                    }
                }
            }
        }
    }
}

@Composable
fun FancyHeaderCard(
    isUpdateAvailable: Boolean,
    latestVersionName: String,
    tipOfTheDay: String
) {
    val dateFormat = SimpleDateFormat("d MMMM EEEE", Locale("tr"))
    val date = dateFormat.format(Date())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(8.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                )
                .padding(12.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = if (isUpdateAvailable) "${stringResource(R.string.update_glance)} $latestVersionName"
                    else "💡 $tipOfTheDay",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.W100),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "$date | LifeTools BETA",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun FancyToolsSection(
    title: String,
    tools: List<ToolItem>,
    onToolClick: (ToolItem) -> Unit,
    isEnabled: Boolean = true
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(9.dp))
        HeroCarousel(
            items = tools,
            onToolClick = onToolClick,
            isEnabled = isEnabled
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun FancyToolCard(
    toolItem: ToolItem,
    onClick: () -> Unit,
    isEnabled: Boolean = true
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val cardWidth = screenWidth * 0.8f
    val cardHeight = cardWidth * 0.40f

    Card(
        onClick = { if (isEnabled) onClick() },
        modifier = Modifier
            .width(cardWidth)
            .height(cardHeight)
            .clip(RoundedCornerShape(20.dp))
            .shadow(10.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = toolItem.iconResId),
                contentDescription = null,
                modifier = Modifier
                    .size(cardHeight * 0.5f)
                    .clip(RoundedCornerShape(25.dp))
            )
            Spacer(Modifier.width(8.dp))
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxHeight()
            ) {
                Text(
                    text = stringResource(id = toolItem.titleResId),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Bu araç ile yapabileceklerin hakkında kısa açıklama buraya sığar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.powered_by_ai),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HeroCarousel(
    items: List<ToolItem>,
    onToolClick: (ToolItem) -> Unit,
    isEnabled: Boolean = true
) {
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { items.size }
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 25.dp),
            pageSpacing = 3.dp
        ) { page ->
            FancyToolCard(
                toolItem = items[page],
                onClick = { onToolClick(items[page]) },
                isEnabled = isEnabled
            )
        }

        // Slider(
            // value = pagerState.currentPage.toFloat(),
            // onValueChange = {
                // LaunchedEffect(it) {
                    // pagerState.animateScrollToPage(it.toInt())
                // }
            // },
            // valueRange = 0f..(items.size - 1).toFloat(),
            // steps = items.size - 2
        // )
    }
}


// --- PREVIEW ---
// @Preview(showBackground = true)
// @Composable
// fun MainScreenPreview() {
    //LifeToolsTheme {
        // MainScreen(onNavigate = {})
    // }
// }
