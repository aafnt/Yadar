package com.yadar.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yadar.app.data.local.entity.CollectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {

    @Query("SELECT * FROM collections ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<CollectionEntity>>

    @Query("SELECT * FROM collections ORDER BY id ASC")
    suspend fun observeAllSnapshot(): List<CollectionEntity>

    @Query("SELECT * FROM collections WHERE isActive = 1")
    suspend fun getActive(): List<CollectionEntity>

    @Query("SELECT * FROM collections WHERE id = :id")
    suspend fun getById(id: Long): CollectionEntity?

    @Query("SELECT COUNT(*) FROM collections")
    fun observeCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(collection: CollectionEntity): Long

    @Update
    suspend fun update(collection: CollectionEntity)

    @Delete
    suspend fun delete(collection: CollectionEntity)

    @Query("DELETE FROM collections")
    suspend fun deleteAll()
}
