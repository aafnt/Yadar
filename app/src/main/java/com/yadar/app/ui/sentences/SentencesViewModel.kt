package com.yadar.app.ui.sentences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yadar.app.domain.model.Collection
import com.yadar.app.domain.model.Sentence
import com.yadar.app.domain.model.SentenceSortOrder
import com.yadar.app.domain.repository.CollectionRepository
import com.yadar.app.domain.repository.SentenceRepository
import com.yadar.app.domain.usecase.DeleteSentenceUseCase
import com.yadar.app.domain.usecase.DuplicateSentenceUseCase
import com.yadar.app.domain.usecase.MoveSentenceToCollectionUseCase
import com.yadar.app.domain.usecase.ReorderSentencesManuallyUseCase
import com.yadar.app.domain.usecase.ToggleSentenceActiveUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SentencesViewModel(
    private val sentenceRepository: SentenceRepository,
    private val collectionRepository: CollectionRepository,
    private val deleteSentenceUseCase: DeleteSentenceUseCase,
    private val duplicateSentenceUseCase: DuplicateSentenceUseCase,
    private val toggleSentenceActiveUseCase: ToggleSentenceActiveUseCase,
    private val moveSentenceToCollectionUseCase: MoveSentenceToCollectionUseCase,
    private val reorderSentencesManuallyUseCase: ReorderSentencesManuallyUseCase
) : ViewModel() {

    val searchQuery = MutableStateFlow("")
    val sortOrder = MutableStateFlow(SentenceSortOrder.NEWEST)

    private val rawSentences = searchQuery.flatMapLatest { query ->
        if (query.isBlank()) sentenceRepository.observeAll() else sentenceRepository.search(query)
    }

    val sentences: StateFlow<List<Sentence>> = combine(rawSentences, sortOrder) { list, order ->
        when (order) {
            SentenceSortOrder.NEWEST -> list.sortedByDescending { it.createdAt }
            SentenceSortOrder.OLDEST -> list.sortedBy { it.createdAt }
            SentenceSortOrder.ALPHABETIC -> list.sortedBy { it.text }
            SentenceSortOrder.LAST_SHOWN -> list.sortedByDescending { it.lastShownAt ?: 0L }
            SentenceSortOrder.MANUAL -> list.sortedBy { it.manualSortOrder }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val collections: StateFlow<List<Collection>> = collectionRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setSortOrder(order: SentenceSortOrder) {
        sortOrder.value = order
    }

    fun delete(sentence: Sentence) = viewModelScope.launch { deleteSentenceUseCase(sentence) }

    fun duplicate(sentence: Sentence) = viewModelScope.launch { duplicateSentenceUseCase(sentence) }

    fun toggleActive(sentence: Sentence) = viewModelScope.launch {
        toggleSentenceActiveUseCase(sentence.id, !sentence.isActive)
    }

    fun moveToCollection(sentence: Sentence, collectionId: Long?) = viewModelScope.launch {
        moveSentenceToCollectionUseCase(sentence, collectionId)
    }

    fun reorderManually(orderedIds: List<Long>) = viewModelScope.launch {
        reorderSentencesManuallyUseCase(orderedIds)
    }
}
