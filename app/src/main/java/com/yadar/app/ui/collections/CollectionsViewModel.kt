package com.yadar.app.ui.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yadar.app.domain.model.Collection
import com.yadar.app.domain.repository.CollectionRepository
import com.yadar.app.domain.usecase.AddCollectionUseCase
import com.yadar.app.domain.usecase.DeleteCollectionUseCase
import com.yadar.app.domain.usecase.SetCollectionActiveUseCase
import com.yadar.app.domain.usecase.UpdateCollectionUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CollectionsViewModel(
    collectionRepository: CollectionRepository,
    private val addCollectionUseCase: AddCollectionUseCase,
    private val updateCollectionUseCase: UpdateCollectionUseCase,
    private val setCollectionActiveUseCase: SetCollectionActiveUseCase,
    private val deleteCollectionUseCase: DeleteCollectionUseCase
) : ViewModel() {

    val collections: StateFlow<List<Collection>> = collectionRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun add(name: String, description: String?) = viewModelScope.launch {
        addCollectionUseCase(name, description)
    }

    fun update(collection: Collection) = viewModelScope.launch {
        updateCollectionUseCase(collection)
    }

    fun setActive(collection: Collection, isActive: Boolean) = viewModelScope.launch {
        setCollectionActiveUseCase(collection, isActive)
    }

    /** بند ۱۰: باید قبل از حذف از کاربر پرسیده شود جمله‌ها چه شوند؛ [moveToUncategorized] پاسخ همان پرسش است. */
    fun delete(collectionId: Long, moveToUncategorized: Boolean) = viewModelScope.launch {
        deleteCollectionUseCase(collectionId, moveToUncategorized)
    }
}
