// File: ui/movie/MovieScreen.kt
package com.babelsoftware.lifetools.ui.movie

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Theaters // Alternatif ikon
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.babelsoftware.lifetools.R
import com.babelsoftware.lifetools.ui.theme.LifeToolsTheme

// Bu sabitler ve enum, projenin daha merkezi bir yerine taşınabilir.
val allGenres = listOf("Aksiyon", "Komedi", "Bilim Kurgu", "Dram", "Korku", "Romantik", "Belgesel", "Animasyon", "Macera", "Fantastik", "Gerilim", "Suç", "Aile", "Müzikal", "Gizem")
val allPlatforms = listOf("Netflix", "Prime Video", "Disney+", "HBO Max", "Apple TV+", "BluTV", "Gain", "Exxen", "TOD", "MUBI")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MovieScreen(
    movieViewModel: MovieViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by movieViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Short)
            movieViewModel.clearErrorMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.movie_screen_title)) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri") } }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // FİLTRELEME BÖLÜMÜ (tek bir item içinde)
            item {
                FilterSection(
                    uiState = uiState,
                    movieViewModel = movieViewModel
                )
            }

            // SONUÇ BÖLÜMÜ
            if (uiState.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (uiState.recommendationList.isNotEmpty()) {
                // Her bir öneri için ayrı bir kart oluşturuyoruz
                items(uiState.recommendationList, key = { it.title + it.year }) { recommendation ->
                    MovieRecommendationCard(recommendation = recommendation)
                }
            } else {
                // Başlangıçta veya sonuç yoksa gösterilecek yer tutucu
                item {
                    Box(modifier = Modifier.fillParentMaxHeight(0.5f).fillParentMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(id = R.string.ai_movie_response_placeholder),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// YENİ: Genişletilebilir Filtreleme Bölümü Composable'ı
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterSection(
    uiState: MovieUiState,
    movieViewModel: MovieViewModel
) {
    var isExpanded by remember { mutableStateOf(true) } // Başlangıçta açık olsun

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Tıklanabilir Başlık
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.FilterList,
                    contentDescription = "Filtrele",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Filtreleme Seçenekleri",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (isExpanded) "Gizle" else "Göster"
                )
            }

            // Genişletilebilir İçerik
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HorizontalDivider() // Başlık ile filtreler arasına ayraç
                    ContentTypeSelector(
                        selectedContentType = uiState.selectedContentType,
                        onContentTypeSelected = { movieViewModel.selectContentType(it) },
                        enabled = !uiState.isLoading
                    )
                    FilterChipGroup(
                        title = stringResource(id = R.string.genres_selection),
                        allItems = allGenres,
                        selectedItems = uiState.selectedGenres,
                        onItemSelected = { movieViewModel.toggleGenre(it) },
                        enabled = !uiState.isLoading
                    )
                    OutlinedTextField(
                        value = uiState.yearInput,
                        onValueChange = { movieViewModel.onYearChanged(it) },
                        label = { Text(stringResource(id = R.string.production_year_label)) },
                        placeholder = { Text(stringResource(id = R.string.production_year_placeholder)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading
                    )
                    FilterChipGroup(
                        title = stringResource(id = R.string.platforms_selection),
                        allItems = allPlatforms,
                        selectedItems = uiState.selectedPlatforms,
                        onItemSelected = { movieViewModel.togglePlatform(it) },
                        enabled = !uiState.isLoading
                    )
                    Button(
                        onClick = { movieViewModel.getMovieRecommendations() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading
                    ) {
                        AnimatedContent(targetState = uiState.isLoading, label = "Button Loading Animation") { isLoading ->
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                            } else {
                                Text(stringResource(id = R.string.get_recommendations_button))
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- YARDIMCI COMPOSABLE'LAR ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentTypeSelector(
    selectedContentType: ContentType,
    onContentTypeSelected: (ContentType) -> Unit,
    enabled: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = stringResource(id = R.string.content_type_selection), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(selected = selectedContentType == ContentType.MOVIE, onClick = { onContentTypeSelected(ContentType.MOVIE) }, shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2), enabled = enabled, icon = {}) { Text(stringResource(id = R.string.movies_tab)) }
            SegmentedButton(selected = selectedContentType == ContentType.SERIES, onClick = { onContentTypeSelected(ContentType.SERIES) }, shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2), enabled = enabled, icon = {}) { Text(stringResource(id = R.string.series_tab)) }
        }
    }
}

// İYİLEŞTİRME: FilterChipGroup artık yatayda kaydırılabilir (LazyRow kullanıyor)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChipGroup(
    title: String,
    allItems: List<String>,
    selectedItems: List<String>,
    onItemSelected: (String) -> Unit,
    enabled: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        // FlowRow yerine LazyRow kullanıyoruz
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp), // Çipler arası yatay boşluk
            contentPadding = PaddingValues(horizontal = 2.dp) // Listenin kenarlarında hafif boşluk
        ) {
            items(items = allItems, key = { it }) { item ->
                val isSelected = selectedItems.contains(item)
                FilterChip(
                    selected = isSelected,
                    onClick = { onItemSelected(item) },
                    label = { Text(item) },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Filled.Done, null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                    } else {
                        null
                    },
                    enabled = enabled
                )
            }
        }
    }
}

@Composable
fun MovieRecommendationCard(recommendation: MovieRecommendation, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = Icons.Filled.Theaters, // Daha genel bir ikon
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(text = recommendation.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            if (recommendation.year.isNotBlank() || recommendation.platforms.isNotBlank()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (recommendation.year.isNotBlank()) {
                        Text(text = "Yapım Yılı: ${recommendation.year}", style = MaterialTheme.typography.labelLarge)
                    }
                    if (recommendation.platforms.isNotBlank()) {
                        Text(text = "Platformlar: ${recommendation.platforms}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(text = recommendation.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}


// --- PREVIEW ---

@Preview(showBackground = true, name = "Movie Screen - Initial/Empty State")
@Composable
fun MovieScreenPreview_Empty() {
    LifeToolsTheme {
        // Bu preview, MovieScreen'in başlangıçtaki boş halini gösterir.
        // uiState varsayılan olarak boş bir recommendationList ile başlar.
        MovieScreen(onNavigateBack = {})
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Movie Screen - Loading State")
@Composable
fun MovieScreenPreview_Loading() {
    LifeToolsTheme {
        // Yüklenme durumunu doğrudan simüle etmek için,
        // LazyColumn içinde gösterdiğimiz yükleme item'ını burada oluşturabiliriz.
        Scaffold(
            topBar = { TopAppBar(title = { Text("Film & Dizi Önerileri") }) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator() // Yüklenme anında gösterdiğimiz component
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Movie Screen - With Results")
@Composable
fun MovieScreenPreview_WithResults() {
    // Örnek bir öneri listesi oluşturalım.
    val sampleRecommendations = listOf(
        MovieRecommendation(
            title = "Başlangıç (Inception)",
            year = "2010",
            platforms = "Netflix, Prime Video",
            description = "Çok yetenekli bir hırsız olan Dom Cobb'un uzmanlık alanı, insanların en savunmasız oldukları rüya görme anında, bilinçaltının derinliklerindeki değerli sırları çekip çıkarmak ve onları çalmaktır."
        ),
        MovieRecommendation(
            title = "Yıldızlararası (Interstellar)",
            year = "2014",
            platforms = "Netflix",
            description = "İnsanlığın sonunun geldiği bir gelecekte, bir grup kaşif, insanlık için yeni bir yuva bulmak amacıyla solucan deliğinden geçerek yıldızlararası bir yolculuğa çıkar."
        )
    )

    LifeToolsTheme {
        // Bu preview, tüm ekranı simüle etmek yerine sadece sonuç kartlarının
        // bir liste içinde nasıl göründüğünü test eder.
        Scaffold(
            topBar = { TopAppBar(title = { Text("Film & Dizi Önerileri") }) }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier.padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(sampleRecommendations) { recommendation ->
                    MovieRecommendationCard(recommendation = recommendation)
                }
            }
        }
    }
}