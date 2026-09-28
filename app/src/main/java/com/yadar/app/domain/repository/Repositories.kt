package com.yadar.app.domain.repository

import com.yadar.app.domain.model.AppSettings
import com.yadar.app.domain.model.AppThemeMode
import com.yadar.app.domain.model.BackupConflictStrategy
import com.yadar.app.domain.model.Collection
import com.yadar.app.domain.model.Sentence
import com.yadar.app.domain.model.WidgetConfig
import kotlinx.coroutines.flow.Flow

interface SentenceRepository {
    fun observeAll(): Flow<List<Sentence>>
    fun search(rawQuery: String): Flow<List<Sentence>>
    fun observeActiveCount(): Flow<Int>
    suspend fun getActiveEligiblePool(collectionId: Long?): List<Sentence>
    suspend fun getById(id: Long): Sentence?
    suspend fun add(sentence: Sentence): Long
    suspend fun update(sentence: Sentence)
    suspend fun delete(sentence: Sentence)
    suspend fun duplicate(sentence: Sentence): Long
    suspend fun setActive(id: Long, isActive: Boolean)
    suspend fun markShown(id: Long, shownAt: Long)
    suspend fun reorderManually(orderedIds: List<Long>)
    suspend fun handleCollectionDeleted(collectionId: Long, moveToUncategorized: Boolean)
}

interface CollectionRepository {
    fun observeAll(): Flow<List<Collection>>
    fun observeCount(): Flow<Int>
    suspend fun getActive(): List<Collection>
    suspend fun getById(id: Long): Collection?
    suspend fun add(collection: Collection): Long
    suspend fun update(collection: Collection)
    suspend fun delete(collection: Collection)
}

interface WidgetConfigRepository {
    fun observeAll(): Flow<List<WidgetConfig>>
    fun observeCount(): Flow<Int>
    suspend fun getById(widgetId: Int): WidgetConfig?
    suspend fun save(config: WidgetConfig)
    suspend fun updateCurrentSelection(widgetId: Int, sentenceId: Long?, epochDay: Long?)
    suspend fun updateSequentialCursor(widgetId: Int, sentenceId: Long?)
    suspend fun delete(widgetId: Int)
    suspend fun getShownHistory(widgetId: Int): List<Long>
    suspend fun recordShown(widgetId: Int, sentenceId: Long, shownAt: Long)
    suspend fun clearHistory(widgetId: Int)
}

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun setThemeMode(mode: AppThemeMode)
    suspend fun setAccentColor(colorArgb: Int)
}

/** خروجی JSON کامل Export/Import (بند ۲۹/۳۰ سند پروژه). */
interface BackupRepository {
    suspend fun exportToJson(): String
    suspend fun importFromJson(json: String, strategy: BackupConflictStrategy): ImportResult
}

sealed class ImportResult {
    data class Success(val sentencesImported: Int, val collectionsImported: Int) : ImportResult()
    data class InvalidJson(val reason: String) : ImportResult()
    data class UnknownVersion(val version: Int) : ImportResult()
}
