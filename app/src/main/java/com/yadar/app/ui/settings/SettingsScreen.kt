package com.yadar.app.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.border
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yadar.app.R
import com.yadar.app.domain.model.AppThemeMode
import com.yadar.app.domain.model.BackupConflictStrategy
import com.yadar.app.domain.repository.ImportResult
import com.yadar.app.ui.theme.PickerColors
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val settings by viewModel.settings.collectAsState()
    val pendingImportJson by viewModel.pendingImportJson.collectAsState()
    val lastImportResult by viewModel.lastImportResult.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val json = viewModel.exportJson()
            context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val text = context.contentResolver.openInputStream(uri)?.use { stream ->
            BufferedReader(InputStreamReader(stream)).readText()
        }
        if (text != null) viewModel.onFilePickedForImport(text)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(stringResource(R.string.settings_theme), style = MaterialTheme.typography.titleMedium)
        ThemeOption(AppThemeMode.LIGHT, R.string.settings_theme_light, settings.themeMode, viewModel::setThemeMode)
        ThemeOption(AppThemeMode.DARK, R.string.settings_theme_dark, settings.themeMode, viewModel::setThemeMode)
        ThemeOption(AppThemeMode.SYSTEM, R.string.settings_theme_system, settings.themeMode, viewModel::setThemeMode)

        Text(stringResource(R.string.settings_accent_color), style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PickerColors.forEach { (_, color) ->
                val selected = color.toArgb() == settings.accentColorArgb
                ColorSwatch(color = color, selected = selected) {
                    viewModel.setAccentColor(color.toArgb())
                }
            }
        }

        Text(stringResource(R.string.settings_backup_title), style = MaterialTheme.typography.titleMedium)
        Button(onClick = { exportLauncher.launch("yadar_backup.json") }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.settings_backup_export))
        }
        OutlinedButton(onClick = { importLauncher.launch(arrayOf("application/json")) }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.settings_backup_import))
        }

        lastImportResult?.let { result ->
            val message = when (result) {
                is ImportResult.Success -> "بازیابی موفق بود."
                is ImportResult.InvalidJson -> stringResource(R.string.error_import_invalid_json)
                is ImportResult.UnknownVersion -> stringResource(R.string.error_import_unknown_version)
            }
            Text(message, style = MaterialTheme.typography.bodyMedium)
        }
    }

    if (pendingImportJson != null) {
        AlertDialog(
            onDismissRequest = viewModel::cancelImport,
            title = { Text(stringResource(R.string.import_conflict_title)) },
            text = {},
            confirmButton = {
                TextButton(onClick = { viewModel.confirmImport(BackupConflictStrategy.REPLACE) }) {
                    Text(stringResource(R.string.import_conflict_replace))
                }
            },
            dismissButton = {
                Column {
                    TextButton(onClick = { viewModel.confirmImport(BackupConflictStrategy.MERGE) }) {
                        Text(stringResource(R.string.import_conflict_merge))
                    }
                    TextButton(onClick = viewModel::cancelImport) {
                        Text(stringResource(R.string.import_conflict_cancel))
                    }
                }
            }
        )
    }
}

@Composable
private fun ThemeOption(mode: AppThemeMode, labelRes: Int, current: AppThemeMode, onSelect: (AppThemeMode) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(mode) },
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        RadioButton(selected = current == mode, onClick = { onSelect(mode) })
        Text(stringResource(labelRes))
    }
}

@Composable
private fun ColorSwatch(color: Color, selected: Boolean, onClick: () -> Unit) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(36.dp)
            .background(color, CircleShape)
            .border(2.dp, borderColor, CircleShape)
            .clickable { onClick() }
    )
}
