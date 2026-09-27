package com.yadar.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.yadar.app.data.local.entity.SentenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SentenceDao {

    @Query("SELECT * FROM sentences ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<SentenceEntity>>

    @Query("SELECT * FROM sentences ORDER BY id ASC")
    suspend fun getAllSnapshot(): List<SentenceEntity>

    @Query("SELECT * FROM sentences WHERE isActive = 1")
    suspend fun getActive(): List<SentenceEntity>

    @Query("SELECT * FROM sentences WHERE isActive = 1 AND (collectionId = :collectionId OR :collectionId IS NULL)")
    suspend fun getActiveByCollection(collectionId: Long?): List<SentenceEntity>

    @Query("SELECT * FROM sentences WHERE id = :id")
    suspend fun getById(id: Long): SentenceEntity?

    @Query("SELECT * FROM sentences WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<SentenceEntity>

    @Query(
        "SELECT * FROM sentences WHERE normalizedText LIKE '%' || :query || '%' " +
            "OR collectionId IN (SELECT id FROM collections WHERE name LIKE '%' || :query || '%') " +
            "ORDER BY createdAt DESC"
    )
    fun search(query: String): Flow<List<SentenceEntity>>

    @Query("SELECT COUNT(*) FROM sentences WHERE isActive = 1")
    fun observeActiveCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(sentence: SentenceEntity): Long

    @Update
    suspend fun update(sentence: SentenceEntity)

    @Delete
    suspend fun delete(sentence: SentenceEntity)

    @Query("DELETE FROM sentences WHERE collectionId = :collectionId")
    suspend fun deleteByCollection(collectionId: Long)

    @Query("UPDATE sentences SET collectionId = NULL WHERE collectionId = :collectionId")
    suspend fun clearCollectionReference(collectionId: Long)

    @Query("UPDATE sentences SET lastShownAt = :shownAt WHERE id = :sentenceId")
    suspend fun markShown(sentenceId: Long, shownAt: Long)

    @Query("UPDATE sentences SET manualSortOrder = :order WHERE id = :sentenceId")
    suspend fun updateManualOrder(sentenceId: Long, order: Long)

    @Transaction
    suspend fun reorderManually(orderedIds: List<Long>) {
        orderedIds.forEachIndexed { index, id -> updateManualOrder(id, index.toLong()) }
    }

    @Query("DELETE FROM sentences")
    suspend fun deleteAll()
}
