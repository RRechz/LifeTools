// File: ui/truthordare/SpinnerScreen.kt
package com.babelsoftware.lifetools.ui.truthordare

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.babelsoftware.lifetools.R
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import kotlin.math.cos
import kotlin.math.sin

// wheelColors listesi ve WheelCanvas Composable'ı aynı kalabilir
val wheelColors = listOf(
    Color(0xFFEF5350), Color(0xFFEC407A), Color(0xFFAB47BC), Color(0xFF7E57C2),
    Color(0xFF5C6BC0), Color(0xFF42A5F5), Color(0xFF29B6F6), Color(0xFF26C6DA),
    Color(0xFF26A69A), Color(0xFF66BB6A), Color(0xFF9CCC65), Color(0xFFD4E157)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpinnerScreen(
    // ViewModel yerine UI state'ini ve event lambdalarını alıyoruz
    uiState: SpinnerUiState,
    onInputChanged: (String) -> Unit,
    onAddItem: () -> Unit,
    onRemoveItem: (String) -> Unit,
    onReorderItems: (Int, Int) -> Unit,
    onSpinWheel: () -> Unit,
    onSpinAnimationCompleted: (String) -> Unit, // Animasyon bittiğinde çağrılacak yeni lambda
    onClearSelectedItem: () -> Unit,
    onClearErrorMessage: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val rotationAngleAnimatable = remember { Animatable(0f) }
    // YENİ: Reorderable için state oluşturuyoruz
    val reorderableState = rememberReorderableLazyListState(
        onMove = { from, to ->
            // ViewModel'daki fonksiyonu çağır
            onReorderItems(from.index, to.index)
        }
    )

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Short)
            onClearErrorMessage()
        }
    }

    if (uiState.selectedItem != null && !uiState.isSpinning) {
        AlertDialog(
            onDismissRequest = onClearSelectedItem,
            title = { Text("Çark Sonucu") },
            text = {
                Text(
                    "'${uiState.selectedItem}' seçildi!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            confirmButton = {
                TextButton(onClick = onClearSelectedItem) { Text("Tamam") }
            }
        )
    }

    LaunchedEffect(uiState.spinTarget) {
        uiState.spinTarget?.let { target ->
            if (kotlin.math.abs(rotationAngleAnimatable.value - target.targetRotationDegrees) > 1f) {
                rotationAngleAnimatable.animateTo(
                    targetValue = target.targetRotationDegrees,
                    animationSpec = tween(durationMillis = 3500, easing = FastOutSlowInEasing)
                )
            }
            val winningItem = uiState.items.getOrNull(target.winningItemIndex)
            if (winningItem != null) {
                onSpinAnimationCompleted(winningItem)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Çarkıfelek Oyunu") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        },
        floatingActionButton = {
            val fabEnabled = uiState.items.size >= 2 && !uiState.isSpinning
            val currentContainerColor = if (fabEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            val currentContentColor = if (fabEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)

            ExtendedFloatingActionButton(
                onClick = { if (fabEnabled) onSpinWheel() },
                icon = { Icon(Icons.Filled.PlayArrow, "Çevir", tint = currentContentColor) },
                text = { Text("Çarkı Çevir", color = currentContentColor) },
                expanded = true,
                containerColor = currentContainerColor,
                contentColor = currentContentColor
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.currentInput,
                    onValueChange = onInputChanged,
                    label = { Text("Çark için metin ekle") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = !uiState.isSpinning
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onAddItem,
                    enabled = uiState.currentInput.isNotBlank() && !uiState.isSpinning
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Ekle")
                }
            }

            Text("Eklenecekler (${uiState.items.size}):", style = MaterialTheme.typography.titleSmall, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            // DÜZELTME: LazyColumn'u reorderable (yeniden sıralanabilir) yapıyoruz
            LazyColumn(
                state = reorderableState.listState, // Kütüphanenin kendi state'ini kullanıyoruz
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Ağırlığı güncelledim, layout'u test et
                    .reorderable(reorderableState) // reorderable modifier'ını ekliyoruz
                    .detectReorderAfterLongPress(reorderableState) // Uzun basarak sürüklemeyi etkinleştir
            ) {
                items(uiState.items, key = { it }) { item ->
                    // Her bir öğeyi ReorderableItem içine alıyoruz
                    ReorderableItem(reorderableState, key = item) { isDragging ->
                        val elevation = if (isDragging) 8.dp else 0.dp
                        val backgroundColor = if (isDragging) MaterialTheme.colorScheme.primaryContainer else Color.Transparent

                        ListItem(
                            modifier = Modifier
                                .background(backgroundColor, shape = MaterialTheme.shapes.small)
                                .shadow(elevation, shape = MaterialTheme.shapes.small), // Sürüklenirken gölge ekle
                            headlineContent = { Text(item) },
                            leadingContent = {
                                // Sürükleme ikonu
                                Icon(
                                    imageVector = Icons.Default.DragHandle,
                                    contentDescription = "Sırala"
                                )
                            },
                            trailingContent = {
                                IconButton(onClick = { onRemoveItem(item) }, enabled = !uiState.isSpinning) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Sil")
                                }
                            }
                        )
                    }
                    HorizontalDivider()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f) // Veya başka bir ağırlık
                    .aspectRatio(1f)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.TopCenter // Seçim okunu üste koymak için
            ) {
                // Sizin paylaştığınız kod bloğu ve diğer durumlar buraya gelecek:
                if (uiState.items.isNotEmpty()) {
                    WheelCanvas(
                        items = uiState.items,
                        currentRotationDegrees = rotationAngleAnimatable.value
                    )
                    // Seçim Oku (Pointer) ikonu
                    Icon(
                        painter = painterResource(id = R.drawable.spinner_arrow),
                        contentDescription = "Seçim Oku",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .size(36.dp)
                            .offset(y = (-18).dp) // Yarısı kadar yukarı kaydırarak çarkın tam üstüne gelsin
                    )
                } else if (uiState.isSpinning) { // Henüz item yok ama spin tetiklendi (hata durumu)
                    CircularProgressIndicator()
                } else {
                    // Başlangıçta veya öğe kalmadığında gösterilecek metin
                    Text("Çarkı oluşturmak için öğe ekleyin", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
fun WheelCanvas(
    items: List<String>,
    currentRotationDegrees: Float,
    modifier: Modifier = Modifier
) {
    // Eğer hiç öğe yoksa, bir şey çizme
    if (items.isEmpty()) return

    val segmentAngle = 360f / items.size
    val textPaint = remember {
        Paint().apply {
            color = Color.Black.toArgb() // Metin rengi
            textSize = 40f // Metin boyutu (bu değer yoğunluğa göre ayarlanabilir)
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val radius = (kotlin.math.min(canvasWidth, canvasHeight) / 2f) * 0.9f // Alanın %90'ını kullansın
        val center = Offset(canvasWidth / 2, canvasHeight / 2)

        // Tüm çizimi mevcut dönüş açısına göre döndür
        rotate(degrees = currentRotationDegrees, pivot = center) {
            items.forEachIndexed { index, item ->
                val startAngle = index * segmentAngle - 90f // -90f ile ilk dilimi yukarıdan başlat
                val sweepAngle = segmentAngle

                // Dilimi çiz
                drawArc(
                    color = wheelColors[index % wheelColors.size], // Renk listesindeki renkleri tekrarla
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true, // Pasta dilimi gibi çizilmesi için
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2)
                )
                // Dilim kenarlığı (isteğe bağlı)
                drawArc(
                    color = Color.Black.copy(alpha = 0.5f),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = 1.dp.toPx()) // Kenarlık kalınlığı
                )

                // Dilim üzerine metin yazma
                if (item.isNotBlank()) {
                    // Metni dilimin ortasına ve dışa doğru konumlandır
                    val textAngleRad = Math.toRadians((startAngle + sweepAngle / 2).toDouble()).toFloat()
                    val textRadius = radius * 0.7f // Metnin çark merkezinden uzaklığı
                    val textX = center.x + textRadius * cos(textAngleRad)
                    val textY = center.y + textRadius * sin(textAngleRad)

                    // Metni çiz
                    drawContext.canvas.nativeCanvas.drawText(
                        item.take(10), // Uzun metinler için ilk 10 karakteri al
                        textX,
                        textY + textPaint.textSize / 3, // Dikeyde ortalamak için ince ayar
                        textPaint
                    )
                }
            }
        }
    }
}