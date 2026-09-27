package com.yadar.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yadar.app.domain.model.AppSettings
import com.yadar.app.domain.model.AppThemeMode
import com.yadar.app.domain.model.BackupConflictStrategy
import com.yadar.app.domain.repository.ImportResult
import com.yadar.app.domain.repository.SettingsRepository
import com.yadar.app.domain.usecase.ExportBackupUseCase
import com.yadar.app.domain.usecase.ImportBackupUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val exportBackupUseCase: ExportBackupUseCase,
    private val importBackupUseCase: ImportBackupUseCase
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    val lastImportResult = MutableStateFlow<ImportResult?>(null)
    val pendingImportJson = MutableStateFlow<String?>(null)

    fun setThemeMode(mode: AppThemeMode) = viewModelScope.launch { settingsRepository.setThemeMode(mode) }

    fun setAccentColor(colorArgb: Int) = viewModelScope.launch { settingsRepository.setAccentColor(colorArgb) }

    suspend fun exportJson(): String = exportBackupUseCase()

    /** فایل انتخاب‌شده برای Import را نگه می‌دارد تا کاربر ابتدا روش برخورد با تداخل را انتخاب کند. */
    fun onFilePickedForImport(json: String) {
        pendingImportJson.value = json
    }

    fun confirmImport(strategy: BackupConflictStrategy) {
        val json = pendingImportJson.value ?: return
        viewModelScope.launch {
            lastImportResult.value = importBackupUseCase(json, strategy)
            pendingImportJson.value = null
        }
    }

    fun cancelImport() {
        pendingImportJson.value = null
    }

    fun clearImportResult() {
        lastImportResult.value = null
    }
}
