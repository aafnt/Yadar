package com.yadar.app.backup

import com.yadar.app.data.local.dao.CollectionDao
import com.yadar.app.data.local.dao.DisplayRuleDao
import com.yadar.app.data.local.dao.SentenceDao
import com.yadar.app.data.local.entity.CollectionEntity
import com.yadar.app.data.local.entity.DisplayRuleEntity
import com.yadar.app.data.local.entity.SentenceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * پیاده‌سازی‌های ساختگیِ DAOها فقط برای تست مسیرهای اعتبارسنجی
 * [com.yadar.app.data.repository.BackupRepositoryImpl] (JSON خراب / Version
 * ناشناخته) که اصلاً به دیتابیس دست نمی‌زنند. برای تست موفقیت‌آمیز کامل
 * Import/Export با دیتابیس واقعی، به androidTest مراجعه کنید
 * (BackupRepositoryInstrumentedTest) چون Room در تست JVM خالص قابل اجرا نیست.
 */
private fun notNeeded(): Nothing = throw UnsupportedOperationException("در این تست استفاده نمی‌شود")

class FakeSentenceDao : SentenceDao {
    override fun observeAll(): Flow<List<SentenceEntity>> = flowOf(emptyList())
    override suspend fun getAllSnapshot(): List<SentenceEntity> = notNeeded()
    override suspend fun getActive(): List<SentenceEntity> = notNeeded()
    override suspend fun getActiveByCollection(collectionId: Long?): List<SentenceEntity> = notNeeded()
    override suspend fun getById(id: Long): SentenceEntity? = notNeeded()
    override suspend fun getByIds(ids: List<Long>): List<SentenceEntity> = notNeeded()
    override fun search(query: String): Flow<List<SentenceEntity>> = flowOf(emptyList())
    override fun observeActiveCount(): Flow<Int> = flowOf(0)
    override suspend fun insert(sentence: SentenceEntity): Long = notNeeded()
    override suspend fun update(sentence: SentenceEntity) = notNeeded()
    override suspend fun delete(sentence: SentenceEntity) = notNeeded()
    override suspend fun deleteByCollection(collectionId: Long) = notNeeded()
    override suspend fun clearCollectionReference(collectionId: Long) = notNeeded()
    override suspend fun markShown(sentenceId: Long, shownAt: Long) = notNeeded()
    override suspend fun updateManualOrder(sentenceId: Long, order: Long) = notNeeded()
    override suspend fun deleteAll() = notNeeded()
}

class FakeCollectionDao : CollectionDao {
    override fun observeAll(): Flow<List<CollectionEntity>> = flowOf(emptyList())
    override suspend fun observeAllSnapshot(): List<CollectionEntity> = notNeeded()
    override suspend fun getActive(): List<CollectionEntity> = notNeeded()
    override suspend fun getById(id: Long): CollectionEntity? = notNeeded()
    override fun observeCount(): Flow<Int> = flowOf(0)
    override suspend fun insert(collection: CollectionEntity): Long = notNeeded()
    override suspend fun update(collection: CollectionEntity) = notNeeded()
    override suspend fun delete(collection: CollectionEntity) = notNeeded()
    override suspend fun deleteAll() = notNeeded()
}

class FakeDisplayRuleDao : DisplayRuleDao {
    override suspend fun getForSentence(sentenceId: Long): DisplayRuleEntity? = notNeeded()
    override suspend fun getForSentences(sentenceIds: List<Long>): List<DisplayRuleEntity> = notNeeded()
    override suspend fun upsert(rule: DisplayRuleEntity): Long = notNeeded()
    override suspend fun update(rule: DisplayRuleEntity) = notNeeded()
    override suspend fun delete(rule: DisplayRuleEntity) = notNeeded()
    override suspend fun deleteForSentence(sentenceId: Long) = notNeeded()
    override suspend fun deleteAll() = notNeeded()
}
