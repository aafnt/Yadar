package com.yadar.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yadar.app.data.local.entity.DisplayRuleEntity

@Dao
interface DisplayRuleDao {

    @Query("SELECT * FROM display_rules WHERE sentenceId = :sentenceId")
    suspend fun getForSentence(sentenceId: Long): DisplayRuleEntity?

    @Query("SELECT * FROM display_rules WHERE sentenceId IN (:sentenceIds)")
    suspend fun getForSentences(sentenceIds: List<Long>): List<DisplayRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(rule: DisplayRuleEntity): Long

    @Update
    suspend fun update(rule: DisplayRuleEntity)

    @Delete
    suspend fun delete(rule: DisplayRuleEntity)

    @Query("DELETE FROM display_rules WHERE sentenceId = :sentenceId")
    suspend fun deleteForSentence(sentenceId: Long)

    @Query("DELETE FROM display_rules")
    suspend fun deleteAll()
}
