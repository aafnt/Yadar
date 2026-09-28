package com.yadar.app.data.repository

import com.yadar.app.data.local.datastore.SettingsDataStore
import com.yadar.app.domain.model.AppSettings
import com.yadar.app.domain.model.AppThemeMode
import com.yadar.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class SettingsRepositoryImpl(
    private val dataStore: SettingsDataStore
) : SettingsRepository {

    override val settings: Flow<AppSettings> = dataStore.settings

    override suspend fun setThemeMode(mode: AppThemeMode) = dataStore.setThemeMode(mode)

    override suspend fun setAccentColor(colorArgb: Int) = dataStore.setAccentColor(colorArgb)
}
