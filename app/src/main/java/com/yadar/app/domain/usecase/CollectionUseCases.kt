package com.yadar.app.domain.usecase

import com.yadar.app.domain.model.Collection
import com.yadar.app.domain.repository.CollectionRepository

class AddCollectionUseCase(private val repository: CollectionRepository) {
    suspend operator fun invoke(name: String, description: String?): Result<Long> {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return Result.failure(IllegalArgumentException("empty_name"))
        return runCatching {
            repository.add(Collection(name = trimmed, description = description?.trim()?.ifEmpty { null }))
        }
    }
}

class UpdateCollectionUseCase(private val repository: CollectionRepository) {
    suspend operator fun invoke(collection: Collection): Result<Unit> {
        if (collection.name.trim().isEmpty()) return Result.failure(IllegalArgumentException("empty_name"))
        return runCatching { repository.update(collection.copy(name = collection.name.trim())) }
    }
}

class SetCollectionActiveUseCase(private val repository: CollectionRepository) {
    suspend operator fun invoke(collection: Collection, isActive: Boolean) {
        repository.update(collection.copy(isActive = isActive))
    }
}
