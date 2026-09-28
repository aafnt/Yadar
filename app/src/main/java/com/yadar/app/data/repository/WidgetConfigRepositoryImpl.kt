package com.yadar.app.data.repository

import com.yadar.app.data.local.dao.SelectionHistoryDao
import com.yadar.app.data.local.dao.WidgetConfigDao
import com.yadar.app.data.local.entity.SelectionHistoryEntity
import com.yadar.app.domain.model.WidgetConfig
import com.yadar.app.domain.repository.WidgetConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WidgetConfigRepositoryImpl(
    private val widgetConfigDao: WidgetConfigDao,
    private val selectionHistoryDao: SelectionHistoryDao
) : WidgetConfigRepository {

    override fun observeAll(): Flow<List<WidgetConfig>> =
        widgetConfigDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeCount(): Flow<Int> = widgetConfigDao.observeCount()

    override suspend fun getById(widgetId: Int): WidgetConfig? = widgetConfigDao.getById(widgetId)?.toDomain()

    override suspend fun save(config: WidgetConfig) = widgetConfigDao.upsert(config.toEntity())

    override suspend fun updateCurrentSelection(widgetId: Int, sentenceId: Long?, epochDay: Long?) =
        widgetConfigDao.updateCurrentSelection(widgetId, sentenceId, epochDay)

    override suspend fun updateSequentialCursor(widgetId: Int, sentenceId: Long?) =
        widgetConfigDao.updateSequentialCursor(widgetId, sentenceId)

    override suspend fun delete(widgetId: Int) {
        widgetConfigDao.deleteById(widgetId)
        selectionHistoryDao.clearForWidget(widgetId)
    }

    override suspend fun getShownHistory(widgetId: Int): List<Long> =
        selectionHistoryDao.getShownSentenceIds(widgetId)

    override suspend fun recordShown(widgetId: Int, sentenceId: Long, shownAt: Long) =
        selectionHistoryDao.insert(SelectionHistoryEntity(sentenceId = sentenceId, widgetId = widgetId, shownAt = shownAt))

    override suspend fun clearHistory(widgetId: Int) = selectionHistoryDao.clearForWidget(widgetId)
}
