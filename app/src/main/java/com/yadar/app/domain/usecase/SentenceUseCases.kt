package com.yadar.app.domain.usecase

import com.yadar.app.domain.model.Sentence
import com.yadar.app.domain.repository.CollectionRepository
import com.yadar.app.domain.repository.SentenceRepository

/** حداکثر طولی که Widget می‌تواند منطقی نمایش دهد (بند ۹ سند پروژه). */
const val SENTENCE_MAX_LENGTH = 1000

sealed class SentenceValidationError {
    object EmptyText : SentenceValidationError()
    object TooLong : SentenceValidationError()
    object InvalidTimeRange : SentenceValidationError()
    object InvalidDateRange : SentenceValidationError()
}

object ValidateSentenceUseCase {
    operator fun invoke(sentence: Sentence): SentenceValidationError? {
        val trimmed = sentence.text.trim()
        if (trimmed.isEmpty()) return SentenceValidationError.EmptyText
        if (trimmed.length > SENTENCE_MAX_LENGTH) return SentenceValidationError.TooLong

        val rule = sentence.displayRule
        val start = rule.timeStartMinute
        val end = rule.timeEndMinute
        if ((start == null) != (end == null)) return SentenceValidationError.InvalidTimeRange
        if (start != null && end != null && (start !in 0..1439 || end !in 0..1439)) {
            return SentenceValidationError.InvalidTimeRange
        }

        val startDate = rule.startDateEpochDay
        val endDate = rule.endDateEpochDay
        if (startDate != null && endDate != null && startDate > endDate) {
            return SentenceValidationError.InvalidDateRange
        }
        return null
    }
}

class AddSentenceUseCase(private val repository: SentenceRepository) {
    suspend operator fun invoke(sentence: Sentence): Result<Long> {
        ValidateSentenceUseCase(sentence)?.let { return Result.failure(SentenceValidationException(it)) }
        return runCatching { repository.add(sentence) }
    }
}

class UpdateSentenceUseCase(private val repository: SentenceRepository) {
    suspend operator fun invoke(sentence: Sentence): Result<Unit> {
        ValidateSentenceUseCase(sentence)?.let { return Result.failure(SentenceValidationException(it)) }
        return runCatching { repository.update(sentence.copy(updatedAt = System.currentTimeMillis())) }
    }
}

class DeleteSentenceUseCase(private val repository: SentenceRepository) {
    suspend operator fun invoke(sentence: Sentence): Result<Unit> = runCatching { repository.delete(sentence) }
}

class DuplicateSentenceUseCase(private val repository: SentenceRepository) {
    suspend operator fun invoke(sentence: Sentence): Result<Long> = runCatching { repository.duplicate(sentence) }
}

class ToggleSentenceActiveUseCase(private val repository: SentenceRepository) {
    suspend operator fun invoke(id: Long, isActive: Boolean) = repository.setActive(id, isActive)
}

class MoveSentenceToCollectionUseCase(private val repository: SentenceRepository) {
    suspend operator fun invoke(sentence: Sentence, newCollectionId: Long?): Result<Unit> = runCatching {
        repository.update(sentence.copy(collectionId = newCollectionId, updatedAt = System.currentTimeMillis()))
    }
}

class ReorderSentencesManuallyUseCase(private val repository: SentenceRepository) {
    suspend operator fun invoke(orderedIds: List<Long>) = repository.reorderManually(orderedIds)
}

class SentenceValidationException(val error: SentenceValidationError) : Exception()

/** بند ۱۰: هنگام حذف مجموعه، تصمیم درباره جمله‌های آن باید از کاربر پرسیده شود. */
class DeleteCollectionUseCase(
    private val collectionRepository: CollectionRepository,
    private val sentenceRepository: SentenceRepository
) {
    suspend operator fun invoke(collectionId: Long, moveSentencesToUncategorized: Boolean): Result<Unit> = runCatching {
        sentenceRepository.handleCollectionDeleted(collectionId, moveSentencesToUncategorized)
        val collection = collectionRepository.getById(collectionId) ?: return@runCatching
        collectionRepository.delete(collection)
    }
}
