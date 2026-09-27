package com.yadar.app.ui.sentences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yadar.app.data.local.entity.DisplayRuleType
import com.yadar.app.domain.model.Collection
import com.yadar.app.domain.model.DisplayRule
import com.yadar.app.domain.model.Sentence
import com.yadar.app.domain.repository.CollectionRepository
import com.yadar.app.domain.repository.SentenceRepository
import com.yadar.app.domain.usecase.AddSentenceUseCase
import com.yadar.app.domain.usecase.SentenceValidationException
import com.yadar.app.domain.usecase.UpdateSentenceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek

data class AddEditSentenceUiState(
    val isEditing: Boolean = false,
    val text: String = "",
    val collectionId: Long? = null,
    val ruleType: DisplayRuleType = DisplayRuleType.ON_CHANGE,
    val weekdays: Set<DayOfWeek> = emptySet(),
    val timeStartMinute: Int? = null,
    val timeEndMinute: Int? = null,
    val startDateEpochDay: Long? = null,
    val endDateEpochDay: Long? = null,
    val validationError: String? = null,
    val saved: Boolean = false
)

class AddEditSentenceViewModel(
    private val sentenceId: Long?,
    private val sentenceRepository: SentenceRepository,
    collectionRepository: CollectionRepository,
    private val addSentenceUseCase: AddSentenceUseCase,
    private val updateSentenceUseCase: UpdateSentenceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditSentenceUiState(isEditing = sentenceId != null))
    val uiState: StateFlow<AddEditSentenceUiState> = _uiState

    val collections: StateFlow<List<Collection>> = collectionRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var originalSentence: Sentence? = null

    init {
        if (sentenceId != null) {
            viewModelScope.launch {
                val existing = sentenceRepository.getById(sentenceId) ?: return@launch
                originalSentence = existing
                _uiState.update {
                    it.copy(
                        text = existing.text,
                        collectionId = existing.collectionId,
                        ruleType = existing.displayRule.type,
                        weekdays = existing.displayRule.weekdays ?: emptySet(),
                        timeStartMinute = existing.displayRule.timeStartMinute,
                        timeEndMinute = existing.displayRule.timeEndMinute,
                        startDateEpochDay = existing.displayRule.startDateEpochDay,
                        endDateEpochDay = existing.displayRule.endDateEpochDay
                    )
                }
            }
        }
    }

    fun setText(text: String) = _uiState.update { it.copy(text = text, validationError = null) }
    fun setCollection(id: Long?) = _uiState.update { it.copy(collectionId = id) }
    fun setRuleType(type: DisplayRuleType) = _uiState.update { it.copy(ruleType = type) }
    fun toggleWeekday(day: DayOfWeek) = _uiState.update {
        it.copy(weekdays = if (day in it.weekdays) it.weekdays - day else it.weekdays + day)
    }
    fun setTimeRange(startMinute: Int?, endMinute: Int?) =
        _uiState.update { it.copy(timeStartMinute = startMinute, timeEndMinute = endMinute) }
    fun setDateRange(startEpochDay: Long?, endEpochDay: Long?) =
        _uiState.update { it.copy(startDateEpochDay = startEpochDay, endDateEpochDay = endEpochDay) }

    fun save() {
        val state = _uiState.value
        val rule = DisplayRule(
            type = state.ruleType,
            weekdays = state.weekdays.takeIf { it.isNotEmpty() },
            timeStartMinute = state.timeStartMinute,
            timeEndMinute = state.timeEndMinute,
            startDateEpochDay = state.startDateEpochDay,
            endDateEpochDay = state.endDateEpochDay
        )
        val sentence = (originalSentence ?: Sentence(text = "")).copy(
            text = state.text,
            collectionId = state.collectionId,
            displayRule = rule
        )

        viewModelScope.launch {
            val result = if (state.isEditing) updateSentenceUseCase(sentence) else addSentenceUseCase(sentence)
            result.fold(
                onSuccess = { _uiState.update { it.copy(saved = true) } },
                onFailure = { error ->
                    val message = (error as? SentenceValidationException)?.error?.toString() ?: "error"
                    _uiState.update { it.copy(validationError = message) }
                }
            )
        }
    }
}
