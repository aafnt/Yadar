package com.yadar.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.yadar.app.data.local.entity.SelectionHistoryEntity

@Dao
interface SelectionHistoryDao {

    @Query("SELECT sentenceId FROM selection_history WHERE widgetId = :widgetId")
    suspend fun getShownSentenceIds(widgetId: Int): List<Long>

    @Insert
    suspend fun insert(history: SelectionHistoryEntity)

    @Query("DELETE FROM selection_history WHERE widgetId = :widgetId")
    suspend fun clearForWidget(widgetId: Int)

    @Query("DELETE FROM selection_history")
    suspend fun clearAll()
}
