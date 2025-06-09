// File: ui/settings/SettingsViewModel.kt
package com.babelsoftware.lifetools.ui.settings

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.babelsoftware.lifetools.data.AppTheme
import com.babelsoftware.lifetools.data.UserPreferencesRepository
import com.babelsoftware.lifetools.model.AppLanguage
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine // combine operatörü için import
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// İYİLEŞTİRME 1: Tüm UI state'lerini tek bir data class altında topluyoruz.
data class SettingsUiState(
    val appTheme: AppTheme = AppTheme.SYSTEM_DEFAULT,
    val isDynamicColorsEnabled: Boolean = true,
    val staticAccentColor: Color = Color(0xFF4285F4), // Varsayılan renk
    val appLanguage: AppLanguage = AppLanguage.TURKISH,
    val isOnboardingCompleted: Boolean? = null
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferencesRepository = UserPreferencesRepository(application)

    // Tüm ayrı Flow'ları tek bir UI State Flow'unda birleştiriyoruz.
    val uiState: StateFlow<SettingsUiState> = combine(
        userPreferencesRepository.themePreference,
        userPreferencesRepository.dynamicColorsEnabledPreference,
        userPreferencesRepository.staticAccentColorPreference,
        userPreferencesRepository.languagePreferenceCode,
        userPreferencesRepository.onboardingCompleted
    ) { theme, isDynamic, accentColorArgb, langCode, onBoardingComplete ->
        SettingsUiState(
            appTheme = theme,
            isDynamicColorsEnabled = isDynamic,
            staticAccentColor = Color(accentColorArgb),
            appLanguage = AppLanguage.fromCode(langCode),
            isOnboardingCompleted = onBoardingComplete
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState(isOnboardingCompleted = null)
    )

    // Ayrı ayrı StateFlow'lar artık kaldırıldı.
    // val appTheme: StateFlow<AppTheme> = ...
    // val isOnboardingCompleted: StateFlow<Boolean> = ...
    // ... vb.


    // Yazma/güncelleme fonksiyonları aynı kalır, çünkü bunlar hala
    // repository'deki ilgili save fonksiyonlarını çağırmalıdır.
    fun updateTheme(newTheme: AppTheme) {
        viewModelScope.launch {
            userPreferencesRepository.saveThemePreference(newTheme)
        }
    }

    fun updateDynamicColorsEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.saveDynamicColorsPreference(isEnabled)
        }
    }

    fun updateStaticAccentColor(color: Color) {
        viewModelScope.launch {
            userPreferencesRepository.saveStaticAccentColorPreference(color.toArgb())
        }
    }

    fun updateLanguage(newLanguage: AppLanguage) {
        viewModelScope.launch {
            userPreferencesRepository.saveLanguagePreference(newLanguage.code)
        }
    }

    fun setOnboardingCompleted() {
        viewModelScope.launch {
            userPreferencesRepository.saveOnboardingCompleted(true)
        }
    }
}