// File: ui/truthordare/SpinnerScreen.kt
package com.babelsoftware.lifetools.ui.truthordare

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.babelsoftware.lifetools.R
import kotlin.math.cos
import kotlin.math.sin

// Alternatif renkler listesi
val wheelColors = listOf(
    Color(0xFFEF5350), Color(0xFFEC407A), Color(0xFFAB47BC), Color(0xFF7E57C2),
    Color(0xFF5C6BC0), Color(0xFF42A5F5), Color(0xFF29B6F6), Color(0xFF26C6DA),
    Color(0xFF26A69A), Color(0xFF66BB6A), Color(0xFF9CCC65), Color(0xFFD4E157)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpinnerScreen(
    spinnerVM: SpinnerViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by spinnerVM.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val rotationAngle = remember { Animatable(0f) } // Animasyon için

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Short)
            spinnerVM.clearErrorMessage()
        }
    }

    // Sonuç için AlertDialog
    if (uiState.selectedItem != null) {
        AlertDialog(
            onDismissRequest = { spinnerVM.clearSelectedItem() },
            title = { Text("Çark Sonucu") },
            text = {
                Text(
                    "'${uiState.selectedItem}' seçildi!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            confirmButton = {
                TextButton(onClick = { spinnerVM.clearSelectedItem() }) {
                    Text("Tamam")
                }
            }
        )
    }
    // Spin hedefini gözlemle ve animasyonu başlat
    LaunchedEffect(uiState.spinTarget) {
        uiState.spinTarget?.let { target ->
            rotationAngle.animateTo(
                targetValue = target.targetRotationDegrees,
                animationSpec = tween(
                    durationMillis = 3500, // Dönüş süresi
                    easing = FastOutSlowInEasing // Yumuşak başlama ve bitiş
                )
            )
            // Animasyon bittikten sonra sonucu ViewModel'a bildir
            val winningItem = uiState.items.getOrNull(target.winningItemIndex)
            if (winningItem != null) {
                spinnerVM.setSpinningCompleted(winningItem)
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
            val fabEnabled = uiState.items.size >= 2 && !uiState.isSpinning // Etkinlik durumunu hesapla

            // Etkinlik durumuna göre renkleri belirle
            val currentContainerColor = if (fabEnabled) {
                MaterialTheme.colorScheme.primary // Etkin renk (bir önceki FAB'daki gibi)
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f) // M3'teki devre dışı bırakılmış container alpha'sı
            }
            val currentContentColor = if (fabEnabled) {
                MaterialTheme.colorScheme.onPrimary // Etkin renk
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) // M3'teki devre dışı bırakılmış content alpha'sı
            }

            ExtendedFloatingActionButton(
                onClick = {
                    if (fabEnabled) { // Sadece etkinken işlemi gerçekleştir
                        spinnerVM.spinWheel()
                    }
                },
                icon = {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = "Çevir",
                        tint = currentContentColor // Rengi dinamik olarak ata
                    )
                },
                text = {
                    Text(
                        "Çarkı Çevir",
                        color = currentContentColor // Rengi dinamik olarak ata
                    )
                },
                expanded = true, // Her zaman geniş kalsın
                containerColor = currentContainerColor, // Dinamik container rengi
                contentColor = currentContentColor // Bu, içindeki Text ve Icon için varsayılan content rengini ayarlar,
                // ancak Icon'da tint ve Text'te color ile ayrıca belirtmek daha garantilidir.
                // enabled parametresi kaldırıldı.
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Scaffold'dan gelen padding
                .padding(16.dp), // Genel ekran içi padding
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Metin Giriş Alanı ve Ekle Butonu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.currentInput,
                    onValueChange = { spinnerVM.onInputChanged(it) },
                    label = { Text("Çark için metin ekle") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = !uiState.isSpinning
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { spinnerVM.addItem() },
                    enabled = uiState.currentInput.isNotBlank() && !uiState.isSpinning
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Ekle")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. ÇARK GÖRSEL ALANI (WheelCanvas ve Seçim Oku'nu içeren Box)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(16.dp), // Bu padding'i vertical olarak değiştirebiliriz: .padding(vertical = 16.dp)
                contentAlignment = Alignment.TopCenter
            ) {
                if (uiState.items.isNotEmpty()) {
                    WheelCanvas(
                        items = uiState.items,
                        currentRotationDegrees = rotationAngle.value
                    )
                    // Seçim Oku (Pointer)
                    Icon(
                        painter = painterResource(id = R.drawable.spinner_arrow), // Özel bir ok ikonu ekleyin
                        contentDescription = "Seçim Oku",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.TopCenter) // Üstte ortala
                            .size(36.dp)
                            .offset(y = (-18).dp) // Yarısı kadar yukarı kaydırarak çarkın tam üstüne gelsin
                    )
                } else if (uiState.isSpinning) { // Henüz item yok ama spin tetiklendi (hata durumu)
                    CircularProgressIndicator()
                }
                else {
                    Text("Çarkı oluşturmak için öğe ekleyin", style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Eklenen Öğeler Listesi (Listenin yüksekliğini sınırlamak gerekebilir)
            Text("Eşyalar (${uiState.items.size}):", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp)) // Çark ile liste arasına boşluk
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Listenin kalan alanı esnek şekilde kaplaması için
                    .heightIn(min = 56.dp, max = 200.dp) // Min ve Max yükseklik (max değeri artırılabilir)
            ) {
                itemsIndexed(uiState.items, key = { _, item -> item }) { index, item ->
                    ListItem(
                        headlineContent = { Text(item) },
                        trailingContent = {
                            IconButton(
                                onClick = { spinnerVM.removeItem(item) },
                                enabled = !uiState.isSpinning
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Sil")
                            }
                        }
                    )
                    if (index < uiState.items.size - 1) {
                        HorizontalDivider()
                    }
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
    if (items.isEmpty()) return

    val segmentAngle = 360f / items.size
    val textPaint = remember {
        Paint().apply {
            color = Color.Black.toArgb() // Metin rengi
            textSize = 40f // Metin boyutu (yoğunluğa göre ayarlanabilir)
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val radius = (kotlin.math.min(canvasWidth, canvasHeight) / 2f) * 0.9f // %90'ını kullansın
        val center = Offset(canvasWidth / 2, canvasHeight / 2)

        rotate(degrees = currentRotationDegrees, pivot = center) {
            items.forEachIndexed { index, item ->
                val startAngle = index * segmentAngle - 90f // -90f ile ilk dilimi yukarıdan başlat
                val sweepAngle = segmentAngle

                // Dilimi çiz
                drawArc(
                    color = wheelColors[index % wheelColors.size], // Renkleri tekrarla
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2)
                )
                // Dilim kenarlığı (opsiyonel)
                drawArc(
                    color = Color.Black.copy(alpha = 0.5f),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = 2.dp.toPx())
                )

                // Dilim üzerine metin yazma (Bu kısım daha karmaşık ve iyileştirme gerektirebilir)
                // Metni dilimin ortasına ve dışa doğru yazmak için:
                val textAngleRad = Math.toRadians((startAngle + sweepAngle / 2).toDouble()).toFloat()
                val textRadius = radius * 0.7f // Metnin çark merkezinden uzaklığı
                val textX = center.x + textRadius * cos(textAngleRad)
                val textY = center.y + textRadius * sin(textAngleRad)

                // Metni döndürerek yazmak daha iyi okunabilirlik sağlar ama Canvas API'sinde karmaşıktır.
                // Şimdilik basitçe metni yazalım.
                // drawContext.canvas.nativeCanvas.save()
                // drawContext.canvas.nativeCanvas.rotate(startAngle + sweepAngle / 2 + 90, textX, textY) // Metni dilime göre döndür
                if (item.isNotBlank()) { // Boş itemları atla
                    drawContext.canvas.nativeCanvas.drawText(
                        item.take(10), // Uzun metinler için kısaltma
                        textX,
                        textY + textPaint.textSize / 3, // Dikeyde ortalamak için ince ayar
                        textPaint
                    )
                }
                // drawContext.canvas.nativeCanvas.restore()
            }
        }
    }
}