package com.yadar.app.domain.usecase

import com.yadar.app.domain.model.BackupConflictStrategy
import com.yadar.app.domain.repository.BackupRepository
import com.yadar.app.domain.repository.ImportResult

class ExportBackupUseCase(private val repository: BackupRepository) {
    suspend operator fun invoke(): String = repository.exportToJson()
}

class ImportBackupUseCase(private val repository: BackupRepository) {
    suspend operator fun invoke(json: String, strategy: BackupConflictStrategy): ImportResult =
        repository.importFromJson(json, strategy)
}
