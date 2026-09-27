package com.yadar.app.data.repository

import com.yadar.app.data.local.dao.DisplayRuleDao
import com.yadar.app.data.local.dao.SentenceDao
import com.yadar.app.domain.model.Sentence
import com.yadar.app.domain.repository.SentenceRepository
import com.yadar.app.util.TextNormalizer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SentenceRepositoryImpl(
    private val sentenceDao: SentenceDao,
    private val displayRuleDao: DisplayRuleDao
) : SentenceRepository {

    override fun observeAll(): Flow<List<Sentence>> =
        sentenceDao.observeAll().map { entities ->
            val rules = displayRuleDao.getForSentences(entities.map { it.id }).associateBy { it.sentenceId }
            entities.map { it.toDomain(rules[it.id]) }
        }

    override fun search(rawQuery: String): Flow<List<Sentence>> {
        val normalizedQuery = TextNormalizer.forSearch(rawQuery)
        return sentenceDao.search(normalizedQuery).map { entities ->
            val rules = displayRuleDao.getForSentences(entities.map { it.id }).associateBy { it.sentenceId }
            entities.map { it.toDomain(rules[it.id]) }
        }
    }

    override fun observeActiveCount(): Flow<Int> = sentenceDao.observeActiveCount()

    override suspend fun getActiveEligiblePool(collectionId: Long?): List<Sentence> {
        val entities = sentenceDao.getActiveByCollection(collectionId)
        val rules = displayRuleDao.getForSentences(entities.map { it.id }).associateBy { it.sentenceId }
        return entities.map { it.toDomain(rules[it.id]) }
    }

    override suspend fun getById(id: Long): Sentence? {
        val entity = sentenceDao.getById(id) ?: return null
        val rule = displayRuleDao.getForSentence(id)
        return entity.toDomain(rule)
    }

    override suspend fun add(sentence: Sentence): Long {
        val normalized = TextNormalizer.forSearch(sentence.text)
        val storageText = TextNormalizer.forStorage(sentence.text)
        val id = sentenceDao.insert(sentence.copy(text = storageText).toEntity(normalized))
        displayRuleDao.upsert(sentence.displayRule.toEntity(sentenceId = id))
        return id
    }

    override suspend fun update(sentence: Sentence) {
        val normalized = TextNormalizer.forSearch(sentence.text)
        val storageText = TextNormalizer.forStorage(sentence.text)
        sentenceDao.update(sentence.copy(text = storageText).toEntity(normalized))
        val existingRule = displayRuleDao.getForSentence(sentence.id)
        displayRuleDao.upsert(sentence.displayRule.toEntity(sentence.id, existingRule?.id ?: 0))
    }

    override suspend fun delete(sentence: Sentence) {
        sentenceDao.delete(sentence.toEntity(TextNormalizer.forSearch(sentence.text)))
    }

    override suspend fun duplicate(sentence: Sentence): Long {
        val now = System.currentTimeMillis()
        return add(sentence.copy(id = 0, createdAt = now, updatedAt = now, lastShownAt = null))
    }

    override suspend fun setActive(id: Long, isActive: Boolean) {
        val entity = sentenceDao.getById(id) ?: return
        sentenceDao.update(entity.copy(isActive = isActive, updatedAt = System.currentTimeMillis()))
    }

    override suspend fun markShown(id: Long, shownAt: Long) {
        sentenceDao.markShown(id, shownAt)
    }

    override suspend fun reorderManually(orderedIds: List<Long>) {
        sentenceDao.reorderManually(orderedIds)
    }

    override suspend fun handleCollectionDeleted(collectionId: Long, moveToUncategorized: Boolean) {
        if (moveToUncategorized) {
            sentenceDao.clearCollectionReference(collectionId)
        } else {
            sentenceDao.deleteByCollection(collectionId)
        }
    }
}
