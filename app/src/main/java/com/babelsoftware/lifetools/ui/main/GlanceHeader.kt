package com.babelsoftware.lifetools.ui.main

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdateAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GlanceHeader(
    modifier: Modifier = Modifier,
    tipOfTheDay: String,
    isUpdateAvailable: Boolean, // Yeni parametre
    latestVersionName: String?  // Yeni parametre
) {
    // AnimatedContent artık güncelleme durumuna göre iki farklı içerik arasında geçiş yapacak
    AnimatedContent(
        targetState = isUpdateAvailable,
        transitionSpec = {
            // Yukarı aşağı kayma animasyonu
            slideInVertically { height -> height } + fadeIn() togetherWith
                    slideOutVertically { height -> -height } + fadeOut()
        },
        label = "Glance Content Animation"
    ) { updateAvailable ->
        if (updateAvailable) {
            // YENİ: Güncelleme mevcut olduğunda gösterilecek içerik
            UpdateAvailableContent(versionName = latestVersionName ?: "")
        } else {
            // Normalde gösterilecek içerik (Karşılama, Tarih, İpucu)
            DefaultGlanceContent(tipOfTheDay = tipOfTheDay)
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
        Text(
            text = tipOfTheDay,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = welcomeText,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = dateText,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun UpdateAvailableContent(versionName: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.SystemUpdateAlt,
            contentDescription = "Güncelleme Mevcut",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "Yeni Güncelleme Mevcut!",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "$versionName sürümü için Ayarlar'ı kontrol et.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}