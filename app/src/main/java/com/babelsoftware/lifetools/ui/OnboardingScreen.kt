package com.babelsoftware.lifetools.ui

import android.content.res.Configuration
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.babelsoftware.lifetools.R
import com.babelsoftware.lifetools.data.AppTheme
import com.babelsoftware.lifetools.model.AppLanguage
import com.babelsoftware.lifetools.ui.settings.SettingsViewModel
import com.babelsoftware.lifetools.ui.theme.LifeToolsTheme
import kotlin.math.roundToInt

// Kurulum adımlarını tanımlayan enum
enum class OnboardingStep {
    WELCOME,
    LANGUAGE_SETTINGS,
    THEME_SETTINGS,
    INFO_SUMMARY
}

val totalOnboardingSteps = OnboardingStep.values().size

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    settingsViewModel: SettingsViewModel = viewModel(),
    onOnboardingComplete: () -> Unit
) {
    var currentStep by remember { mutableStateOf(OnboardingStep.WELCOME) }
    val currentThemeBase by settingsViewModel.appTheme.collectAsStateWithLifecycle()
    val currentLanguage by settingsViewModel.appLanguage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var dynamicColorsEnabled by remember { mutableStateOf(true) }
    var selectedStaticAccentColor by remember { mutableStateOf(predefinedStaticColors.first().first) }

    // YENİ: ViewModel'dan dinamik renk ve statik accent renk state'lerini al
    val isDynamicColorsUserEnabled by settingsViewModel.isDynamicColorsEnabled.collectAsStateWithLifecycle()
    val currentStaticAccentColor by settingsViewModel.staticAccentColor.collectAsStateWithLifecycle()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Artık INFO_SUMMARY adımında da navigasyon barı görünecek (Bitir butonu için)
            if (currentStep != OnboardingStep.WELCOME) { // WELCOME hariç tüm adımlarda göster
                OnboardingNavigationBar(
                    currentStep = currentStep,
                    onBack = {
                        currentStep = when (currentStep) {
                            OnboardingStep.LANGUAGE_SETTINGS -> OnboardingStep.WELCOME
                            OnboardingStep.THEME_SETTINGS -> OnboardingStep.LANGUAGE_SETTINGS
                            OnboardingStep.INFO_SUMMARY -> OnboardingStep.THEME_SETTINGS // Bilgiden temaya geri dön
                            else -> currentStep // WELCOME veya beklenmedik durum
                        }
                    },
                    onNext = {
                        when (currentStep) {
                            OnboardingStep.LANGUAGE_SETTINGS -> currentStep = OnboardingStep.THEME_SETTINGS
                            OnboardingStep.THEME_SETTINGS -> currentStep = OnboardingStep.INFO_SUMMARY // Temadan bilgiye git
                            OnboardingStep.INFO_SUMMARY -> { // Son adımda "Bitir"
                                settingsViewModel.setOnboardingCompleted()
                                onOnboardingComplete()
                            }
                            else -> {} // WELCOME için FAB kullanılacak
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            // Sadece Hoş Geldin adımında FAB göster
            if (currentStep == OnboardingStep.WELCOME) {
                FloatingActionButton(
                    onClick = { currentStep = OnboardingStep.LANGUAGE_SETTINGS },
                    containerColor = MaterialTheme.colorScheme.primary, // Expressive renk
                    shape = RoundedCornerShape(16.dp) // Expressive şekil
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = stringResource(id = R.string.onboarding_start)
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End // FAB konumu
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) { // paddingValues'ı uygula
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState) // İçerik scroll edilebilir
                    .padding(16.dp) // Genel sayfa içi padding
                    .animateContentSize(), // Adımlar arası geçişte animasyon
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (currentStep) {
                    OnboardingStep.WELCOME -> WelcomeStepContent()
                    OnboardingStep.LANGUAGE_SETTINGS -> LanguageStepContent(
                        selectedLanguage = currentLanguage,
                        onLanguageSelected = { settingsViewModel.updateLanguage(it) }
                    )
                    OnboardingStep.THEME_SETTINGS -> ThemeStepContent(
                        currentTheme = currentThemeBase, // Base tema modu
                        onThemeSelected = { settingsViewModel.updateTheme(it) },
                        isDynamicColorsEnabled = isDynamicColorsUserEnabled, // ViewModel'dan
                        onDynamicColorsEnabledChanged = { settingsViewModel.updateDynamicColorsEnabled(it) }, // ViewModel'a
                        staticAccentColor = currentStaticAccentColor, // ViewModel'dan
                        onStaticAccentColorChanged = { settingsViewModel.updateStaticAccentColor(it) }, // ViewModel'a
                        availableStaticColors = predefinedStaticColors // Bu liste şimdilik UI'da kalabilir
                    )
                    OnboardingStep.INFO_SUMMARY -> InfoSummaryStepContent()
                }
            }
        }
    }
}

@Composable
fun OnboardingNavigationBar(
    currentStep: OnboardingStep,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp
    ) {
        Column {
            LinearProgressIndicator(
                // Progress artık (mevcut adımın sırası + 1) / toplam adım sayısı
                progress = { (currentStep.ordinal + 1).toFloat() / totalOnboardingSteps },
                modifier = Modifier.fillMaxWidth(),
                strokeCap = StrokeCap.Round,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onBack,
                    // WELCOME adımında geri butonu olmaz (nav bar orada görünmüyor)
                    // Diğer adımlarda (LANGUAGE_SETTINGS, THEME_SETTINGS, INFO_SUMMARY) geri butonu aktif
                    enabled = currentStep != OnboardingStep.WELCOME
                ) {
                    Text(stringResource(id = R.string.onboarding_back))
                }

                TextButton(onClick = onNext) {
                    Text(
                        // Son adım INFO_SUMMARY ise "Bitir" yaz, değilse "İleri"
                        if (currentStep == OnboardingStep.INFO_SUMMARY) stringResource(id = R.string.onboarding_finish)
                        else stringResource(id = R.string.onboarding_next)
                    )
                }
            }
        }
    }
}
@Composable
fun WelcomeStepContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxHeight() // Mümkün olduğunca alanı kapla
            .padding(bottom = 72.dp) // FAB için altta boşluk
    ) {
        // Uygulama Logosu (Şimdilik bir placeholder veya drawable kullanabiliriz)
        Image(
            painter = painterResource(id = R.drawable.movie_with_ai), // Örnek logo
            contentDescription = "App Logo",
            modifier = Modifier
                .size(128.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(24.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(id = R.string.welcome_to_lifetool),
            style = MaterialTheme.typography.displaySmall, // Daha büyük ve etkileyici
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(id = R.string.onboarding_description),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Özellikler listesi güncellenmiş FeatureListItem çağrılarıyla
        // Drawable isimlerinizin doğru olduğundan emin olun!
        FeatureListItem(
            iconResId = R.drawable.recipes_with_ai, // Kendi drawable ikonunuzun ID'si
            text = stringResource(id = R.string.list_recipe)
        )
        FeatureListItem(
            iconResId = R.drawable.movie_with_ai, // Kendi drawable ikonunuzun ID'si
            text = stringResource(id = R.string.list_movie)
        )
        FeatureListItem(
            iconResId = R.drawable.truth_or_dare_with_ai, // Kendi drawable ikonunuzun ID'si
            text = stringResource(id = R.string.list_ToD)
        )
        FeatureListItem(
            iconResId = R.drawable.todo_with_ai, // Kendi drawable ikonunuzun ID'si
            text = stringResource(id = R.string.list_ToDo_Maker)
        )
    }
}

@Composable
fun FeatureListItem(
    @DrawableRes iconResId: Int, // Parametreyi @DrawableRes Int olarak değiştiriyoruz
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 10.dp) // Dikey padding biraz artırıldı
    ) {
        Icon(
            painter = painterResource(id = iconResId), // painterResource ile drawable'ı yüklüyoruz
            contentDescription = text, // İkon için içerik açıklaması (erişilebilirlik için)
            tint = MaterialTheme.colorScheme.secondary, // İkon rengi temanızdan
            modifier = Modifier.size(28.dp) // İkon boyutu biraz daha belirgin olabilir
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, style = MaterialTheme.typography.titleMedium) // Stil bodyLarge'dan titleMedium'a yükseltildi
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageStepContent(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 16.dp) // Dikey padding biraz azaltıldı
            .clip(RoundedCornerShape(24.dp)), // Daha expressive bir köşe
        shadowElevation = 6.dp, // Gölge biraz artırıldı
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp) // Ton biraz daha belirgin
    ) {
        Column(
            modifier = Modifier.padding(16.dp), // Surface iç padding'i
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // YENİ: Dil Önizleme Paneli
            LanguagePreviewPane(selectedLanguage = selectedLanguage)

            Spacer(modifier = Modifier.height(24.dp)) // Önizleme ile seçici arasına boşluk

            // Mevcut LanguageSelector (içeriği aynı kalabilir veya başlığı buraya taşınabilir)
            LanguageSelector(
                selectedLanguage = selectedLanguage,
                onLanguageSelected = onLanguageSelected,
                // modifier = Modifier.padding(top = 8.dp) // LanguageSelector'ın kendi iç padding'i varsa
            )
        }
    }
}

@Composable
fun LanguagePreviewPane(selectedLanguage: AppLanguage, modifier: Modifier = Modifier) {
    val previewTextHello = when (selectedLanguage) {
        AppLanguage.TURKISH -> "Merhaba!"
        AppLanguage.ENGLISH -> "Hello!"
    }
    val previewTextSample = when (selectedLanguage) {
        AppLanguage.TURKISH -> "Bu örnek bir metindir."
        AppLanguage.ENGLISH -> "This is a sample text."
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp) // Önizleme panelinin yüksekliği
            .clip(RoundedCornerShape(16.dp)), // İç panelin köşe yuvarlaklığı
        color = MaterialTheme.colorScheme.surface, // Ana yüzeyden farklı bir renk
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) // İnce bir kenarlık
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Sahte TopAppBar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        RoundedCornerShape(6.dp)
                    )
            )

            // Önizleme Metinleri
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = previewTextHello,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = previewTextSample,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Sahte Butonlar veya Navigasyon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(20.dp)
                        .padding(horizontal = 4.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            RoundedCornerShape(4.dp)
                        )
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(20.dp)
                        .padding(horizontal = 4.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            RoundedCornerShape(4.dp)
                        )
                )
            }
        }
    }
}

// Önceden tanımlanmış statik renk seçenekleri
val predefinedStaticColors = listOf(
    Color(0xFF4285F4) to R.string.default_color_name, // Google Blue
    Color(0xFF34A853) to R.string.green_color_name,   // Google Green
    Color(0xFF8A2BE2) to R.string.purple_color_name,  // BlueViolet
    Color(0xFFFFA500) to R.string.orange_color_name,  // Orange
    Color(0xFF20B2AA) to R.string.teal_color_name     // LightSeaGreen
)

// ThemeStepContent Composable'ı güncellenerek ThemePreviewPane'e yeni parametreler geçecek
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeStepContent(
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    // Artık ViewModel'dan gelen değerler ve callback'ler parametre olarak alınıyor
    isDynamicColorsEnabled: Boolean,
    onDynamicColorsEnabledChanged: (Boolean) -> Unit,
    staticAccentColor: Color,
    onStaticAccentColorChanged: (Color) -> Unit,
    availableStaticColors: List<Pair<Color, Int>>
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 16.dp)
            .clip(RoundedCornerShape(24.dp)),
        shadowElevation = 6.dp,
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ThemePreviewPane(
                baseThemeMode = currentTheme,
                isDynamicColorsEnabled = isDynamicColorsEnabled, // Parametreden gelen değer
                staticAccentColor = staticAccentColor // Parametreden gelen değer
            )
            Spacer(modifier = Modifier.height(24.dp))

            DynamicColorSwitch(
                checked = isDynamicColorsEnabled,
                onCheckedChange = onDynamicColorsEnabledChanged
            )
            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(visible = !isDynamicColorsEnabled) {
                StaticAccentColorSelector(
                    availableColors = availableStaticColors,
                    selectedColor = staticAccentColor,
                    onColorSelected = onStaticAccentColorChanged
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            ThemeSelector(
                currentTheme = currentTheme,
                onThemeSelected = onThemeSelected
            )
        }
    }
}

@Composable
fun DynamicColorSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier // <<<--- ALINAN MODIFIER BURAYA ATANDI
            .fillMaxWidth()
            .padding(vertical = 8.dp), // Bu padding fonksiyonun kendi iç padding'i olarak kalabilir
        // veya dışarıdan gelen modifier ile birleştirilebilir/kaldırılabilir.
        // Şimdilik kendi iç padding'ini koruyalım.
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(id = R.string.dynamic_colors_title),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(id = R.string.dynamic_colors_summary),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(0.8f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StaticAccentColorSelector(
    availableColors: List<Pair<Color, Int>>, // Renk ve string resource ID'si
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = R.string.static_accent_color_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly, // Renkleri eşit dağıt
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            availableColors.forEach { (color, nameResId) ->
                ColorSwatch(
                    color = color,
                    colorName = stringResource(id = nameResId),
                    isSelected = color == selectedColor,
                    onClick = { onColorSelected(color) }
                )
            }
        }
    }
}

@Composable
fun ColorSwatch(
    color: Color,
    colorName: String, // Erişilebilirlik ve Tooltip için
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val expressiveShape = RoundedCornerShape(12.dp) // Expressive köşe
    Box(
        modifier = Modifier
            .size(56.dp) // Buton boyutu
            .clip(expressiveShape)
            .background(color)
            .clickable(onClick = onClick)
            .border( // Seçiliyse belirgin bir kenarlık
                width = if (isSelected) 2.dp else Dp.Hairline, // Dp.Hairline çok ince bir çizgi
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = expressiveShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.CheckCircle, // Seçili olduğunu gösteren ikon
                contentDescription = "$colorName seçildi",
                tint = calculateOnColor(color), // Arka plan rengine göre ikon rengi
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// Arka plan rengine göre uygun bir "onColor" (siyah veya beyaz) hesaplayan yardımcı fonksiyon
// Daha karmaşık bir "onColor" hesabı için Material kütüphanelerindeki benzer fonksiyonlara bakılabilir.
fun calculateOnColor(backgroundColor: Color): Color {
    // Basit bir parlaklık kontrolü (0.0 ile 1.0 arası)
    // Luminance = 0.2126 * R + 0.7152 * G + 0.0722 * B
    val luminance = (0.2126f * backgroundColor.red + 0.7152f * backgroundColor.green + 0.0722f * backgroundColor.blue)
    return if (luminance > 0.5f) Color.Black else Color.White
}

@Composable
fun InfoSummaryStepContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center, // İçeriği dikeyde ortala
        modifier = Modifier
            .fillMaxSize() // Adımın tüm yüksekliğini kullan
            .padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle, // Onay ikonu
            contentDescription = stringResource(id = R.string.onboarding_step_info_title),
            tint = MaterialTheme.colorScheme.primary, // Temanın ana rengi
            modifier = Modifier.size(80.dp) // Büyük bir ikon
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(id = R.string.onboarding_step_info_title),
            style = MaterialTheme.typography.headlineLarge, // Etkileyici başlık
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(id = R.string.onboarding_step_info_subtitle),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Opsiyonel: Temel özelliklerin tekrar kısa bir özeti
        Text(
            text = stringResource(id = R.string.onboarding_app_feature_summary_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        FeatureListItem(
            iconResId = R.drawable.recipes_with_ai, // Kendi drawable ikonunuzun ID'si
            text = stringResource(id = R.string.list_recipe)
        )
        FeatureListItem(
            iconResId = R.drawable.movie_with_ai, // Kendi drawable ikonunuzun ID'si
            text = stringResource(id = R.string.list_movie)
        )
        FeatureListItem(
            iconResId = R.drawable.truth_or_dare_with_ai, // Kendi drawable ikonunuzun ID'si
            text = stringResource(id = R.string.list_ToD)
        )
        FeatureListItem(
            iconResId = R.drawable.todo_with_ai, // Kendi drawable ikonunuzun ID'si
            text = stringResource(id = R.string.list_ToDo_Maker)
        )
        // Bu adımda ayrıca bir "Hadi Başlayalım" butonu göstermek yerine,
        // OnboardingNavigationBar'daki "Bitir" butonu bu işlevi görecek.
    }
}

@Composable
fun ThemePreviewPane(
    baseThemeMode: AppTheme,
    isDynamicColorsEnabled: Boolean,
    staticAccentColor: Color,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val previewColorScheme = remember(baseThemeMode, isDynamicColorsEnabled, staticAccentColor) {
        val isDarkForPreview = when (baseThemeMode) {
            AppTheme.LIGHT -> false
            AppTheme.DARK -> true
            AppTheme.SYSTEM_DEFAULT -> {
                // Sistem temasının gece modu olup olmadığını API seviyesine uygun şekilde kontrol et
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // API Level 30 (Android 11)
                    context.resources.configuration.isNightModeActive
                } else {
                    // API Level 30 altı için
                    val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                    currentNightMode == Configuration.UI_MODE_NIGHT_YES
                }
            }
        }

        if (isDynamicColorsEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (isDarkForPreview) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else {
            // Dinamik renkler kapalı veya cihaz desteklemiyor (Build.VERSION_CODES.S altı),
            // statik accent color kullanılacak.
            // Veya dinamik renkler açık ama cihaz S altı ise de buraya düşer, bu durumda
            // statik rengi veya varsayılan M3 renklerini kullanabiliriz.
            // Şimdilik statik rengi kullanalım.
            if (isDarkForPreview) {
                darkColorScheme(
                    primary = staticAccentColor,
                    onPrimary = calculateOnColor(staticAccentColor),
                    primaryContainer = staticAccentColor.copy(alpha = 0.3f),
                    onPrimaryContainer = calculateOnColor(staticAccentColor.copy(alpha = 0.3f))
                    // Diğer renkler varsayılan koyu tema renkleri olacak
                )
            } else {
                lightColorScheme(
                    primary = staticAccentColor,
                    onPrimary = calculateOnColor(staticAccentColor),
                    primaryContainer = staticAccentColor.copy(alpha = 0.3f),
                    onPrimaryContainer = calculateOnColor(staticAccentColor.copy(alpha = 0.3f))
                    // Diğer renkler varsayılan açık tema renkleri olacak
                )
            }
        }
    }

    MaterialTheme(colorScheme = previewColorScheme) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.background,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            // İçerik (Sahte UI Elemanları) öncekiyle aynı kalabilir...
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(0.dp)
            ) {
                // 1. Sahte TopAppBar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .height(12.dp)
                                .weight(0.6f)
                                .background(
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    RoundedCornerShape(4.dp)
                                )
                        )
                    }
                }

                // 2. Sahte İçerik Alanı
                Column(modifier = Modifier.padding(12.dp)) {
                    Box(
                        modifier = Modifier
                            .height(14.dp)
                            .fillMaxWidth(0.7f)
                            .background(
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                RoundedCornerShape(4.dp)
                            )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .height(10.dp)
                                .fillMaxWidth(if (it == 2) 0.5f else 0.9f)
                                .background(
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    RoundedCornerShape(4.dp)
                                )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }

                // 3. Sahte FAB
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "Sahte FAB",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelector(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(id = R.string.language_selection_title),
            style = MaterialTheme.typography.titleLarge, // Başlık stili
            modifier = Modifier.padding(bottom = 12.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant // Daha yumuşak bir başlık rengi
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            AppLanguage.values().forEachIndexed { index, language ->
                SegmentedButton(
                    selected = selectedLanguage == language,
                    onClick = { onLanguageSelected(language) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = AppLanguage.values().size),
                    icon = { /* İkon gerekirse eklenebilir */ },
                    colors = SegmentedButtonDefaults.colors( // Expressive renkler
                        activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        inactiveContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                        inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(stringResource(id = language.displayNameResId))
                }
            }
        }
    }
}

// AppTheme enum değerlerini slider için float değerlere ve geriye çeviren yardımcı fonksiyonlar
fun appThemeToFloat(theme: AppTheme): Float {
    return when (theme) {
        AppTheme.LIGHT -> 0f
        AppTheme.SYSTEM_DEFAULT -> 1f // Sistem Varsayılanı ortada
        AppTheme.DARK -> 2f
    }
}

fun floatToAppTheme(value: Float): AppTheme {
    return when (value.roundToInt()) {
        0 -> AppTheme.LIGHT
        1 -> AppTheme.SYSTEM_DEFAULT
        2 -> AppTheme.DARK
        else -> AppTheme.SYSTEM_DEFAULT // Beklenmedik bir durumda varsayılan
    }
}


@Composable
fun ThemeSelector(
    currentTheme: AppTheme, // ViewModel'dan gelen güncel tema
    onThemeSelected: (AppTheme) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true // Yükleme durumlarında devre dışı bırakmak için
) {
    // Slider'ın anlık pozisyonunu tutmak için lokal bir state.
    // Bu, kullanıcı kaydırırken anlık görsel geri bildirim (örn. metin etiketi) için kullanılır.
    // ViewModel'dan gelen currentTheme ile senkronize edilir.
    var sliderPosition by remember(currentTheme) { mutableStateOf(appThemeToFloat(currentTheme)) }

    // Slider'dan seçilen anlık tema (metin etiketi için)
    val DRAFT_currentSelectedThemeFromSlider = floatToAppTheme(sliderPosition)

    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(id = R.string.choose_your_theme),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp), // Başlık ile metin etiketi arasına boşluk
            color = MaterialTheme.colorScheme.onSurface
        )

        // Seçilen temayı gösteren metin etiketi
        Text(
            text = when (DRAFT_currentSelectedThemeFromSlider) {
                AppTheme.LIGHT -> stringResource(id = R.string.light_theme)
                AppTheme.DARK -> stringResource(id = R.string.dark_theme)
                AppTheme.SYSTEM_DEFAULT -> stringResource(id = R.string.system_default_theme)
            },
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary, // Vurgu rengi
            modifier = Modifier.padding(bottom = 16.dp) // Metin etiketi ile slider arasına boşluk
        )

        Slider(
            value = sliderPosition,
            onValueChange = { newValue ->
                // Kullanıcı kaydırdıkça lokal pozisyonu güncelle
                sliderPosition = newValue
            },
            onValueChangeFinished = {
                // Kullanıcı kaydırmayı bitirdiğinde, en yakın adıma yuvarla
                // ve ViewModel'ı güncelle.
                val finalTheme = floatToAppTheme(sliderPosition)
                // Eğer slider pozisyonu tam olarak bir adıma denk gelmediyse,
                // onu en yakın adıma zorlayarak UI'ı da güncelleyelim.
                sliderPosition = appThemeToFloat(finalTheme)
                onThemeSelected(finalTheme)
            },
            valueRange = 0f..2f, // 0f: Light, 1f: System, 2f: Dark
            steps = 1, // (2-0)/X - 1 = steps -> X = (2-0)/(steps+1) -> (2-0)/1 = 2 segment, 3 nokta
            // steps = (aralık sayısı - 1) -> 3 nokta için 2 aralık, yani 1 step.
            colors = SliderDefaults.colors( // Expressive renkler
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                activeTickColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                inactiveTickColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            ),
            enabled = enabled,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp) // Slider'a biraz yan boşluk
        )

        // Opsiyonel: Slider altında etiketler (Açık, Sistem, Koyu)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // DÜZELTİLMİŞ KISIM:
                .padding(start = 8.dp, end = 8.dp, top = 4.dp) // Slider ile aynı hizada olması için
                .padding(top = 4.dp), // Eğer sadece top padding'i eklemek istiyorsanız bu satırı silebilirsiniz.
            // Yukarıdaki satır zaten top=4dp içeriyor.
            // Eğer hem genel top hem de ek bir top padding istiyorsanız,
            // bu mantıksal bir hata olabilir.
            // Muhtemelen sadece start, end ve top yeterlidir.
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(id = R.string.light_theme), style = MaterialTheme.typography.bodySmall)
            Text(stringResource(id = R.string.system_default_theme), style = MaterialTheme.typography.bodySmall)
            Text(stringResource(id = R.string.dark_theme), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun ThemeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val expressiveShape = RoundedCornerShape(16.dp) // Daha belirgin yuvarlaklık

    if (isSelected) {
        ElevatedButton( // Seçili buton için yükseltilmiş ve ana renkte
            onClick = onClick,
            shape = expressiveShape,
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp, pressedElevation = 6.dp),
            modifier = Modifier.sizeIn(minWidth = 100.dp, minHeight = 48.dp) // Buton boyutunu ayarla
        ) {
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    } else {
        FilledTonalButton( // Seçili olmayan için daha yumuşak bir ton
            onClick = onClick,
            shape = expressiveShape,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            modifier = Modifier.sizeIn(minWidth = 100.dp, minHeight = 48.dp)
        ) {
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}


@Preview(showBackground = true, device = "spec:width=360dp,height=740dp", name = "Welcome Step")
@Composable
fun WelcomeStepContentPreview() {
    LifeToolsTheme {
        // WelcomeStepContent'i doğrudan çağırarak UI'ını kontrol et
        Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.Center) {
            WelcomeStepContent()
        }
    }
}

// Preview'lar güncellenmeli:
@Preview(showBackground = true, device = "spec:width=360dp,height=740dp", name = "Language Step with Preview Pane")
@Composable
fun LanguageStepContentPreview_WithPane() {
    LifeToolsTheme {
        var selectedLang by remember { mutableStateOf(AppLanguage.TURKISH) }
        Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.Center) {
            LanguageStepContent(
                selectedLanguage = selectedLang,
                onLanguageSelected = { selectedLang = it }
            )
        }
    }
}

@Preview(showBackground = true, device = "spec:width=360dp,height=740dp", name = "Theme Step Preview (Light)")
@Composable
fun ThemeStepContentPreview_Light() {
    LifeToolsTheme(darkTheme = false) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            ThemeStepContent(
                currentTheme = AppTheme.LIGHT,
                onThemeSelected = {},
                isDynamicColorsEnabled = true, // Örnek değer
                onDynamicColorsEnabledChanged = {}, // Boş lambda
                staticAccentColor = predefinedStaticColors.first().first, // Örnek renk
                onStaticAccentColorChanged = {}, // Boş lambda
                availableStaticColors = predefinedStaticColors // Tanımlı listeniz
            )
        }
    }
}

@Preview(showBackground = true, device = "spec:width=360dp,height=740dp", name = "Theme Step Preview (Dark, Static Green)")
@Composable
fun ThemeStepContentPreview_Dark_StaticGreen() {
    LifeToolsTheme(darkTheme = true) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            ThemeStepContent(
                currentTheme = AppTheme.DARK,
                onThemeSelected = {},
                isDynamicColorsEnabled = false, // Dinamik renkler kapalı
                onDynamicColorsEnabledChanged = {},
                staticAccentColor = predefinedStaticColors.getOrElse(1) { predefinedStaticColors.first() }.first, // Örnek Yeşil renk
                onStaticAccentColorChanged = {},
                availableStaticColors = predefinedStaticColors
            )
        }
    }
}

@Preview(showBackground = true, name = "Info Summary Step Preview")
@Composable
fun InfoSummaryStepContentPreview() {
    LifeToolsTheme {
        InfoSummaryStepContent()
    }
}