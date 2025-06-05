// File: ui/settings/SettingsViewModel.kt
package com.babelsoftware.lifetools.ui.settings

import android.app.Application
import androidx.compose.ui.graphics.Color // Color için import
import androidx.compose.ui.graphics.toArgb // toArgb için import
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.babelsoftware.lifetools.data.UserPreferencesRepository
import com.babelsoftware.lifetools.data.defaultStaticColorArgb
import com.babelsoftware.lifetools.model.AppLanguage // Yeni import
import com.babelsoftware.lifetools.data.AppTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map // map operatörü için import
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferencesRepository = UserPreferencesRepository(application)

    val appTheme: StateFlow<AppTheme> = userPreferencesRepository.themePreference
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppTheme.SYSTEM_DEFAULT
        )

    val isOnboardingCompleted: StateFlow<Boolean> = userPreferencesRepository.onboardingCompleted
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    // YENİ: Dil tercihini StateFlow olarak dışa aktarıyoruz
    val appLanguage: StateFlow<AppLanguage> = userPreferencesRepository.languagePreferenceCode
        .map { languageCode -> AppLanguage.fromCode(languageCode) } // Kodu AppLanguage enum'ına çevir
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppLanguage.TURKISH // Başlangıç değeri
        )

    // YENİ: Dinamik renkler etkin mi?
    val isDynamicColorsEnabled: StateFlow<Boolean> = userPreferencesRepository.dynamicColorsEnabledPreference
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true // Varsayılan olarak açık
        )

    // YENİ: Seçili statik ana renk
    val staticAccentColor: StateFlow<Color> = userPreferencesRepository.staticAccentColorPreference
        .map { argb -> Color(argb) } // Int ARGB'yi Color nesnesine çevir
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Color(defaultStaticColorArgb) // Varsayılan renk
        )

    // YENİ: Dinamik renkler tercihini güncelle
    fun updateDynamicColorsEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.saveDynamicColorsPreference(isEnabled)
        }
    }

    // YENİ: Statik ana renk tercihini güncelle
    fun updateStaticAccentColor(color: Color) {
        viewModelScope.launch {
            userPreferencesRepository.saveStaticAccentColorPreference(color.toArgb()) // Color'ı Int ARGB'ye çevir
        }
    }

    fun updateTheme(newTheme: AppTheme) {
        viewModelScope.launch {
            userPreferencesRepository.saveThemePreference(newTheme)
        }
    }

    fun setOnboardingCompleted() {
        viewModelScope.launch {
            userPreferencesRepository.saveOnboardingCompleted(true)
        }
    }

    // YENİ: Kullanıcının seçtiği yeni dili kaydeder.
    fun updateLanguage(newLanguage: AppLanguage) {
        viewModelScope.launch {
            userPreferencesRepository.saveLanguagePreference(newLanguage.code)
        }
    }
}