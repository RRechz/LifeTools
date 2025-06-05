// File: ui/movie/MovieScreen.kt
package com.babelsoftware.lifetools.ui.movie

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done // Done ikonu için
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // viewModel() için import
import androidx.lifecycle.compose.collectAsStateWithLifecycle // collectAsStateWithLifecycle için import
import com.babelsoftware.lifetools.R
import com.babelsoftware.lifetools.ui.theme.LifeToolsTheme

// Constant Lists [Platfrom contents may vary from region to region! (Will be set as String)]
val allGenres = listOf("Aksiyon", "Komedi", "Bilim Kurgu", "Dram", "Korku", "Romantik", "Belgesel", "Animasyon", "Macera", "Fantastik", "Gerilim", "Suç", "Aile", "Müzikal", "Gizem")
val allPlatforms = listOf("Netflix", "Prime Video", "Disney+", "HBO Max", "Apple TV+", "BluTV", "Gain", "Exxen", "TOD", "MUBI")


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MovieScreen(
    movieViewModel: MovieViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by movieViewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            movieViewModel.clearErrorMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.movie_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back_to_main_screen)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            // 1. Movie/TV Series Selection
            ContentTypeSelector(
                selectedContentType = uiState.selectedContentType,
                onContentTypeSelected = { movieViewModel.selectContentType(it) },
                enabled = !uiState.isLoading // Disable when loading
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 2. Genre Selection
            FilterChipGroup(
                title = stringResource(id = R.string.genres_selection),
                allItems = allGenres,
                selectedItems = uiState.selectedGenres,
                onItemSelected = { movieViewModel.toggleGenre(it) },
                enabled = !uiState.isLoading // Disable when loading
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 3. Year of Production
            OutlinedTextField(
                value = uiState.yearInput,
                onValueChange = { movieViewModel.onYearChanged(it) },
                label = { Text(stringResource(id = R.string.production_year_label)) },
                placeholder = { Text(stringResource(id = R.string.production_year_placeholder)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading // Disable when loading
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 4. Choose Platform
            FilterChipGroup(
                title = stringResource(id = R.string.platforms_selection),
                allItems = allPlatforms,
                selectedItems = uiState.selectedPlatforms,
                onItemSelected = { movieViewModel.togglePlatform(it) },
                enabled = !uiState.isLoading // Disable when loading
            )
            Spacer(modifier = Modifier.height(24.dp))

            // 5. Take Suggestion Button
            Button(
                onClick = { movieViewModel.getMovieRecommendations() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading // Disable when loading
            ) {
                if (uiState.isLoading && uiState.recommendations.isBlank()) { // Sadece ilk yüklemede butonda göster
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(id = R.string.get_recommendations_button))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // 6. Result Field and Loading Indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 200.dp) // Minimum height
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                    if (uiState.isLoading && uiState.recommendations.isBlank()) { // First upload and no response
                    CircularProgressIndicator()
                } else if (uiState.recommendations.isNotBlank()) {
                    Text(
                        text = uiState.recommendations,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.ai_movie_response_placeholder),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentTypeSelector(
    selectedContentType: com.babelsoftware.lifetools.ui.movie.ContentType,
    onContentTypeSelected: (ContentType) -> Unit,
    enabled: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = R.string.content_type_selection),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = selectedContentType == com.babelsoftware.lifetools.ui.movie.ContentType.MOVIE,
                onClick = { onContentTypeSelected(com.babelsoftware.lifetools.ui.movie.ContentType.MOVIE) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                enabled = enabled, // Etkinlik durumunu ekle
                icon = {} // Boş ikon ekleyerek metnin ortalanmasını sağlıyoruz (Material3'ün bir özelliği)
            ) {
                Text(stringResource(id = R.string.movies_tab))
            }
            SegmentedButton(
                selected = selectedContentType == ContentType.SERIES,
                onClick = { onContentTypeSelected(ContentType.SERIES) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                enabled = enabled, // Etkinlik durumunu ekle
                icon = {} // Boş ikon
            ) {
                Text(stringResource(id = R.string.series_tab))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterChipGroup(
    title: String,
    allItems: List<String>,
    selectedItems: List<String>, // Artık SnapshotStateList değil, ViewModel'dan gelen List<String>
    onItemSelected: (String) -> Unit,
    enabled: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            allItems.forEach { item ->
                val isSelected = selectedItems.contains(item)
                FilterChip(
                    selected = isSelected,
                    onClick = { onItemSelected(item) },
                    label = { Text(item) },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Filled.Done, contentDescription = "$item seçildi", modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                    } else {
                        null
                    },
                    enabled = enabled // Etkinlik durumunu ekle
                )
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun MovieScreenPreview_Integrated() {
    LifeToolsTheme {
        MovieScreen(onNavigateBack = {}) // viewModel() default olarak çağrılacak
    }
}