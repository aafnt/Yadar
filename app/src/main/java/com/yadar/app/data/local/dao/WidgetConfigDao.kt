package com.yadar.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yadar.app.data.local.entity.WidgetConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WidgetConfigDao {

    @Query("SELECT * FROM widget_configs ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<WidgetConfigEntity>>

    @Query("SELECT * FROM widget_configs WHERE widgetId = :widgetId")
    suspend fun getById(widgetId: Int): WidgetConfigEntity?

    @Query("SELECT COUNT(*) FROM widget_configs")
    fun observeCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(config: WidgetConfigEntity)

    @Update
    suspend fun update(config: WidgetConfigEntity)

    @Query("UPDATE widget_configs SET currentSentenceId = :sentenceId, lastSelectionEpochDay = :epochDay WHERE widgetId = :widgetId")
    suspend fun updateCurrentSelection(widgetId: Int, sentenceId: Long?, epochDay: Long?)

    @Query("UPDATE widget_configs SET sequentialCursorSentenceId = :sentenceId WHERE widgetId = :widgetId")
    suspend fun updateSequentialCursor(widgetId: Int, sentenceId: Long?)

    @Delete
    suspend fun delete(config: WidgetConfigEntity)

    @Query("DELETE FROM widget_configs WHERE widgetId = :widgetId")
    suspend fun deleteById(widgetId: Int)

    @Query("DELETE FROM widget_configs")
    suspend fun deleteAll()
}
