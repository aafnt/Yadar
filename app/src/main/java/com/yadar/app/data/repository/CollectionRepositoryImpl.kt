package com.yadar.app.data.repository

import com.yadar.app.data.local.dao.CollectionDao
import com.yadar.app.domain.model.Collection
import com.yadar.app.domain.repository.CollectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CollectionRepositoryImpl(
    private val collectionDao: CollectionDao
) : CollectionRepository {

    override fun observeAll(): Flow<List<Collection>> =
        collectionDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeCount(): Flow<Int> = collectionDao.observeCount()

    override suspend fun getActive(): List<Collection> =
        collectionDao.getActive().map { it.toDomain() }

    override suspend fun getById(id: Long): Collection? = collectionDao.getById(id)?.toDomain()

    override suspend fun add(collection: Collection): Long = collectionDao.insert(collection.toEntity())

    override suspend fun update(collection: Collection) = collectionDao.update(collection.toEntity())

    override suspend fun delete(collection: Collection) = collectionDao.delete(collection.toEntity())
}
