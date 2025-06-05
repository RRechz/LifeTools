// File: ui/recipe/RecipeScreen.kt
package com.babelsoftware.lifetools.ui.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color // Kullanılmıyorsa kaldırılabilir
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // viewModel() için import
import androidx.lifecycle.compose.collectAsStateWithLifecycle // collectAsStateWithLifecycle için import
import com.babelsoftware.lifetools.R
import com.babelsoftware.lifetools.ui.theme.LifeToolsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(
    recipeViewModel: RecipeViewModel = viewModel(), // ViewModel'ı alıyoruz
    onNavigateBack: () -> Unit
) {
    // ViewModel'dan UI state'ini topluyoruz
    val uiState by recipeViewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // Hata mesajı varsa Snackbar göstermek için
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            // Hata gösterildikten sonra ViewModel'da temizlenebilir (opsiyonel)
            // recipeViewModel.clearErrorMessage() // ViewModel'a böyle bir fonksiyon eklenirse
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }, // Snackbar'ı ekliyoruz
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.recipe_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back_to_main_screen)
                        )
                    }
                }
            )
        },
        bottomBar = {
            RecipeInputBar(
                text = uiState.inputText,
                onTextChanged = { recipeViewModel.onInputTextChanged(it) },
                onSendClick = { recipeViewModel.getRecipeIdeas() },
                isLoading = uiState.isLoading // Yüklenme durumunu butona iletiyoruz
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
            Spacer(modifier = Modifier.height(16.dp))

            // Yüklenme durumu veya AI cevabı alanı
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 200.dp) // Minimum yükseklik
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center // Yükleme göstergesini ortalamak için
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator()
                } else if (uiState.recipeResponse.isNotBlank()) {
                    Text(
                        text = uiState.recipeResponse,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    // Başlangıçta veya cevap yokken gösterilecek yer tutucu
                    Text(
                        text = stringResource(id = R.string.ai_response_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp)) // En alttaki input bar için boşluk
        }
    }
}

@Composable
fun RecipeInputBar(
    text: String,
    onTextChanged: (String) -> Unit,
    onSendClick: () -> Unit,
    isLoading: Boolean, // Yüklenme durumunu alıyoruz
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChanged,
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(id = R.string.ingredients_placeholder)) },
                label = { Text(stringResource(id = R.string.ingredients_label)) },
                singleLine = false,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading // Yüklenirken TextField'ı devre dışı bırak
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onSendClick,
                enabled = text.isNotBlank() && !isLoading, // Yüklenmiyorsa ve metin varsa aktif
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary, // Buton içindeki indicator için
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Filled.Send,
                        contentDescription = stringResource(id = R.string.get_ideas_button)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecipeScreenPreview_Idle() {
    LifeToolsTheme {
        // Preview için sahte bir ViewModel veya state gerekebilir.
        // Şimdilik direkt RecipeScreen'i çağıralım, viewModel() default olarak boş bir state ile başlar.
        RecipeScreen(onNavigateBack = {})
    }
}

@Preview(showBackground = true)
@Composable
fun RecipeScreenPreview_Loading() {
    val loadingState = RecipeUiState(isLoading = true, inputText = "Domates, biber")
    // Bu preview için ViewModel'ı mock'lamak veya state'i manuel sağlamak daha doğru olur.
    // Şimdilik basit bir gösterim:
    LifeToolsTheme {
        // Sahte ViewModel veya state ile preview yapma
        // Bu kısım daha detaylı bir mocklama gerektirebilir.
        // RecipeScreen(recipeViewModel = provideFakeViewModelWithState(loadingState), onNavigateBack = {})
        // Şimdilik temel UI elemanlarını görmek için RecipeInputBar'ı preview edebiliriz:
        RecipeInputBar(text = "Domates, biber", onTextChanged = {}, onSendClick = {}, isLoading = true)
    }
}