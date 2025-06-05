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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "lifetool_user_settings")

// Default static color (if predefinedStaticColors is easily accessible it can be taken from there, otherwise a fixed value)
val defaultStaticColorArgb = Color(0xFF4285F4).toArgb() // Google Blue ARGB

class UserPreferencesRepository(private val context: Context) {

    private companion object {
        val APP_THEME_KEY = stringPreferencesKey("app_theme")
        val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
        val APP_LANGUAGE_KEY = stringPreferencesKey("app_language_code")
        val DYNAMIC_COLORS_ENABLED_KEY = booleanPreferencesKey("dynamic_colors_enabled")
        val STATIC_ACCENT_COLOR_KEY = intPreferencesKey("static_accent_color_argb")
    }

    // Theme saving and reading functions
    suspend fun saveThemePreference(theme: AppTheme) {
        try {
            context.dataStore.edit { preferences ->
                preferences[APP_THEME_KEY] = theme.name
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    val themePreference: Flow<AppTheme> = context.dataStore.data
        .map { preferences ->
            val themeName = preferences[APP_THEME_KEY] ?: AppTheme.SYSTEM_DEFAULT.name
            try {
                AppTheme.valueOf(themeName)
            } catch (e: IllegalArgumentException) {
                AppTheme.SYSTEM_DEFAULT
            }
        }

    suspend fun saveOnboardingCompleted(completed: Boolean) {
        try {
            context.dataStore.edit { preferences ->
                preferences[ONBOARDING_COMPLETED_KEY] = completed
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[ONBOARDING_COMPLETED_KEY] ?: false
        }

    // Function to save language preference
    suspend fun saveLanguagePreference(languageCode: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences[APP_LANGUAGE_KEY] = languageCode
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    val languagePreferenceCode: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[APP_LANGUAGE_KEY] ?: AppLanguage.TURKISH.code
        }

    // Save dynamic colors preference
    suspend fun saveDynamicColorsPreference(isEnabled: Boolean) {
        try {
            context.dataStore.edit { preferences ->
                preferences[DYNAMIC_COLORS_ENABLED_KEY] = isEnabled
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // Read your preference for dynamic colors
    val dynamicColorsEnabledPreference: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            // Dynamic colors on by default (true)
            preferences[DYNAMIC_COLORS_ENABLED_KEY] ?: true
        }

    // Save static primary color preference (as ARGB Int)
    suspend fun saveStaticAccentColorPreference(colorArgb: Int) {
        try {
            context.dataStore.edit { preferences ->
                preferences[STATIC_ACCENT_COLOR_KEY] = colorArgb
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // Read static primary color preference (as ARGB Int)
    val staticAccentColorPreference: Flow<Int> = context.dataStore.data
        .map { preferences ->
            // Set a default static color
            preferences[STATIC_ACCENT_COLOR_KEY] ?: defaultStaticColorArgb
        }
}