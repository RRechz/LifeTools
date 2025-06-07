package com.babelsoftware.lifetools.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.babelsoftware.lifetools.data.AppTheme // AppTheme enum'ını model paketinden import et

// Bu varsayılan paletler, Color.kt dosyanızdaki tanımları kullanır.
// Dinamik renkler kapalı olduğunda temel olarak bu paletler kullanılır ve
// kullanıcının seçtiği statik renk ile bu paletlerin üzerine yazılır.
private val DarkDefaultColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightDefaultColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

// Bu yardımcı fonksiyonun Color.kt dosyasına taşınması daha iyi bir pratiktir.
fun calculateOnColor(backgroundColor: Color): Color {
    val luminance = (0.2126f * backgroundColor.red + 0.7152f * backgroundColor.green + 0.0722f * backgroundColor.blue)
    return if (luminance > 0.5f) Color.Black else Color.White
}


@Composable
fun LifeToolsTheme(
    // Parametreleri, SettingsViewModel'dan gelen tüm tercihleri alacak şekilde güncelliyoruz
    appThemeMode: AppTheme = AppTheme.SYSTEM_DEFAULT,
    isDynamicColorsEnabled: Boolean = true,
    staticAccentColor: Color = Purple40, // Varsayılan statik renk
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    // 1. Nihai "karanlık mod aktif mi?" durumunu belirle
    val useDarkTheme = when (appThemeMode) {
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        AppTheme.SYSTEM_DEFAULT -> isSystemInDarkTheme()
    }

    // 2. ColorScheme'i oluştur
    val colorScheme = when {
        isDynamicColorsEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            // Dinamik renkler etkin ve cihaz destekliyor (Android 12+)
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> {
            // Dinamik renkler kapalı veya cihaz desteklemiyor.
            // Seçilen statik ana rengi kullanarak bir ColorScheme oluştur.
            val baseColorScheme = if (useDarkTheme) DarkDefaultColorScheme else LightDefaultColorScheme
            baseColorScheme.copy(
                primary = staticAccentColor,
                onPrimary = calculateOnColor(staticAccentColor),
                primaryContainer = staticAccentColor.copy(alpha = 0.3f),
                onPrimaryContainer = calculateOnColor(staticAccentColor.copy(alpha = 0.3f)),
                secondary = staticAccentColor.copy(alpha = 0.7f),
                onSecondary = calculateOnColor(staticAccentColor.copy(alpha = 0.7f)),
                tertiary = staticAccentColor.copy(alpha = 0.5f),
                onTertiary = calculateOnColor(staticAccentColor.copy(alpha = 0.5f))
            )
        }
    }

    // 3. Sistem çubuğu renklerini ayarla (StatusBar, NavigationBar)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.surfaceColorAtElevation(3.dp).toArgb()

            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !useDarkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !useDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // ui/theme/Type.kt dosyanızdan
        shapes = AppShapes,      // ui/theme/Shape.kt dosyanızdan
        content = content
    )
}