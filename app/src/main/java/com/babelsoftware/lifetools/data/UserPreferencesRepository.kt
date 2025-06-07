// File: data/UserPreferencesRepository.kt
package com.babelsoftware.lifetools.data

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.babelsoftware.lifetools.model.AppLanguage
import com.babelsoftware.lifetools.data.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "lifetool_user_settings")

class UserPreferencesRepository(private val context: Context) {

    // İYİLEŞTİRME 1: Tüm anahtarları ve varsayılan değerleri tek bir private object içinde topladık.
    private object PreferencesKeys {
        // Anahtarlar
        val APP_THEME = stringPreferencesKey("app_theme")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val APP_LANGUAGE_CODE = stringPreferencesKey("app_language_code")
        val DYNAMIC_COLORS_ENABLED = booleanPreferencesKey("dynamic_colors_enabled")
        val STATIC_ACCENT_COLOR_ARGB = intPreferencesKey("static_accent_color_argb")

        // Varsayılan Değerler
        val DEFAULT_THEME_NAME = AppTheme.SYSTEM_DEFAULT.name
        val DEFAULT_ONBOARDING_COMPLETED = false
        val DEFAULT_LANGUAGE_CODE = AppLanguage.TURKISH.code
        val DEFAULT_DYNAMIC_COLORS_ENABLED = true
        val DEFAULT_STATIC_COLOR_ARGB = Color(0xFF4285F4).toArgb() // Google Blue ARGB
    }

    // Okuma akışlarına (Flow) bir de .catch bloğu ekleyerek olası I/O hatalarını yakalayabiliriz.
    private fun <T> Flow<T>.handleReadExceptions(): Flow<T> =
        catch { exception ->
            if (exception is IOException) {
                // Hata olduğunda boş bir Preferences yayarak uygulamanın çökmesini önle
                // ve varsayılan değerlerin kullanılmasını sağla.
                // Loglama da eklenebilir.
                exception.printStackTrace()
            } else {
                throw exception
            }
        }


    // --- Okuma İşlemleri (Flow'lar) ---

    val themePreference: Flow<AppTheme> = context.dataStore.data
        .map { preferences ->
            val themeName = preferences[PreferencesKeys.APP_THEME] ?: PreferencesKeys.DEFAULT_THEME_NAME
            try {
                AppTheme.valueOf(themeName)
            } catch (e: IllegalArgumentException) {
                AppTheme.SYSTEM_DEFAULT
            }
        }.handleReadExceptions()

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: PreferencesKeys.DEFAULT_ONBOARDING_COMPLETED
        }.handleReadExceptions()

    val languagePreferenceCode: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.APP_LANGUAGE_CODE] ?: PreferencesKeys.DEFAULT_LANGUAGE_CODE
        }.handleReadExceptions()

    val dynamicColorsEnabledPreference: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DYNAMIC_COLORS_ENABLED] ?: PreferencesKeys.DEFAULT_DYNAMIC_COLORS_ENABLED
        }.handleReadExceptions()

    val staticAccentColorPreference: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.STATIC_ACCENT_COLOR_ARGB] ?: PreferencesKeys.DEFAULT_STATIC_COLOR_ARGB
        }.handleReadExceptions()


    // --- Yazma İşlemleri (Suspend Fonksiyonlar) ---

    private suspend fun <T> saveData(key: Preferences.Key<T>, value: T) {
        try {
            context.dataStore.edit { preferences ->
                preferences[key] = value
            }
        } catch (e: IOException) {
            // Gelecekte buraya loglama eklenebilir.
            e.printStackTrace()
        }
    }

    suspend fun saveThemePreference(theme: AppTheme) = saveData(PreferencesKeys.APP_THEME, theme.name)

    suspend fun saveOnboardingCompleted(completed: Boolean) = saveData(PreferencesKeys.ONBOARDING_COMPLETED, completed)

    suspend fun saveLanguagePreference(languageCode: String) = saveData(PreferencesKeys.APP_LANGUAGE_CODE, languageCode)

    suspend fun saveDynamicColorsPreference(isEnabled: Boolean) = saveData(PreferencesKeys.DYNAMIC_COLORS_ENABLED, isEnabled)

    suspend fun saveStaticAccentColorPreference(colorArgb: Int) = saveData(PreferencesKeys.STATIC_ACCENT_COLOR_ARGB, colorArgb)

}