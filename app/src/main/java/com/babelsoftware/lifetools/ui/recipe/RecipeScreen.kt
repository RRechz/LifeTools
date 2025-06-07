package com.babelsoftware.lifetools.ui.recipe

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.babelsoftware.lifetools.R
import com.babelsoftware.lifetools.ui.theme.LifeToolsTheme
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(
    recipeViewModel: RecipeViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by recipeViewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Short)
            // TODO: ViewModel'a clearErrorMessage() eklenebilir
        }
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(uiState.messages.size - 1)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.recipe_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri")
                    }
                }
            )
        },
        bottomBar = {
            val isLoading = uiState.messages.lastOrNull()?.isLoading ?: false
            RecipeInputBar(
                text = uiState.inputText,
                onTextChanged = { recipeViewModel.onInputTextChanged(it) },
                onSendClick = { recipeViewModel.sendMessage() },
                isLoading = isLoading
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.messages.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Hangi malzemelerle harikalar yaratmak istersin?",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(uiState.messages) { message ->
                    when (message.participant) {
                        Participant.USER -> UserMessageBubble(message = message)
                        Participant.AI -> AiMessageBubble(message = message)
                        Participant.ERROR -> ErrorMessageBubble(message = message)
                    }
                }
            }
        }
    }
}

@Composable
fun UserMessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp)),
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Text(text = message.text, modifier = Modifier.padding(16.dp))
        }
    }
}

@Composable
fun AiMessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp)),
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
            tonalElevation = 1.dp
        ) {
            if (message.isLoading) {
                TypingIndicator(modifier = Modifier.padding(16.dp))
            } else {
                MarkdownText(
                    markdown = message.text,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
                )
            }
        }
    }
}

@Composable
fun ErrorMessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp)),
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ) {
            Text(text = message.text, modifier = Modifier.padding(16.dp))
        }
    }
}

@Composable
fun TypingIndicator(modifier: Modifier = Modifier) {
    val dotCount = 3
    val animationDelay = 300

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        for (i in 0 until dotCount) {
            val animatable = remember { Animatable(0f) }
            LaunchedEffect(Unit) {
                animatable.animateTo(
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = keyframes {
                            durationMillis = dotCount * animationDelay
                            0f at (i * animationDelay)
                            1f at (i * animationDelay + animationDelay / 2)
                            0f at (i * animationDelay + animationDelay)
                        },
                        repeatMode = RepeatMode.Restart
                    )
                )
            }
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = animatable.value))
            )
        }
    }
}

@Composable
fun RecipeInputBar(
    text: String,
    onTextChanged: (String) -> Unit,
    onSendClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .navigationBarsPadding() // Sadece iç Row'a padding vererek klavyenin arkasında kalmasını engelle
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChanged,
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(id = R.string.ingredients_placeholder)) },
                label = { Text(stringResource(id = R.string.ingredients_label)) },
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onSendClick,
                enabled = text.isNotBlank() && !isLoading,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Filled.Send, contentDescription = stringResource(id = R.string.get_ideas_button))
                }
            }
        }
    }
}


// --- Preview Fonksiyonları ---

@Preview(showBackground = true, name = "Recipe Screen - Empty")
@Composable
fun RecipeScreenPreview_Empty() {
    LifeToolsTheme {
        // Preview için state hoisting'li bir yapı idealdir.
        // Şimdilik sadece boş bir ekranı görmek için doğrudan çağırıyoruz.
        RecipeScreen(onNavigateBack = {})
    }
}

@Preview(showBackground = true, name = "Recipe Screen - Chat History")
@Composable
fun RecipeScreenPreview_Chat() {
    val sampleMessages = listOf(
        ChatMessage("Domates, soğan, sarımsak, zeytinyağı", Participant.USER),
        ChatMessage(
            """
            Harika malzemeler! İşte size lezzetli bir **Domates Çorbası** tarifi:

            ### Malzemeler:
            * 4 adet büyük boy domates
            * 1 adet kuru soğan
            * 2 diş sarımsak
            * 2 yemek kaşığı zeytinyağı
            * Tuz, karabiber

            ### Yapılışı:
            1.  Soğanı ve sarımsağı zeytinyağında kavurun.
            2.  Domatesleri ekleyip pişirin.
            3.  Blenderdan geçirin ve baharatları ekleyin.
            """.trimIndent(), Participant.AI
        ),
        ChatMessage("Yapay zeka yazıyor...", Participant.AI, isLoading = true)
    )

    LifeToolsTheme {
        // Bu preview'un çalışması için RecipeScreen'in state hoisting ile düzenlenmesi gerekir.
        // Şimdilik sadece baloncukları ayrı ayrı preview edelim:
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(16.dp)) {
            sampleMessages.forEach { message ->
                when (message.participant) {
                    Participant.USER -> UserMessageBubble(message = message)
                    Participant.AI -> AiMessageBubble(message = message)
                    Participant.ERROR -> ErrorMessageBubble(message = message)
                }
            }
        }
    }
}