package com.yadar.app.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.yadar.app.domain.model.AppSettings
import com.yadar.app.domain.model.AppThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "yadar_settings")

/** تنظیمات عمومی برنامه (نه تنظیمات هر Widget که مستقل و در Room است). */
class SettingsDataStore(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_COLOR = intPreferencesKey("accent_color_argb")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        val theme = prefs[Keys.THEME_MODE]?.let {
            runCatching { AppThemeMode.valueOf(it) }.getOrDefault(AppThemeMode.SYSTEM)
        } ?: AppThemeMode.SYSTEM
        val accent = prefs[Keys.ACCENT_COLOR] ?: AppSettings().accentColorArgb
        AppSettings(themeMode = theme, accentColorArgb = accent)
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setAccentColor(colorArgb: Int) {
        context.dataStore.edit { it[Keys.ACCENT_COLOR] = colorArgb }
    }
}
