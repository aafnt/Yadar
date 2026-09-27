package com.yadar.app.widget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yadar.app.domain.model.Collection
import com.yadar.app.domain.model.WidgetConfig
import com.yadar.app.domain.repository.CollectionRepository
import com.yadar.app.domain.repository.WidgetConfigRepository
import com.yadar.app.domain.usecase.SaveWidgetConfigUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** ViewModel صفحه تنظیم Widget (بند ۱۷ تا ۱۹، ۵۳ تا ۵۷ سند پروژه). */
class WidgetConfigViewModel(
    private val appWidgetId: Int,
    widgetConfigRepository: WidgetConfigRepository,
    collectionRepository: CollectionRepository,
    private val saveWidgetConfigUseCase: SaveWidgetConfigUseCase
) : ViewModel() {

    val config = MutableStateFlow(WidgetConfig(widgetId = appWidgetId))
    val showWallpaperPreview = MutableStateFlow(true)

    val collections: StateFlow<List<Collection>> = collectionRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            widgetConfigRepository.getById(appWidgetId)?.let { config.value = it }
        }
    }

    fun update(transform: (WidgetConfig) -> WidgetConfig) {
        config.value = transform(config.value)
    }

    suspend fun save(): WidgetConfig {
        val toSave = config.value.copy(widgetId = appWidgetId)
        saveWidgetConfigUseCase(toSave)
        return toSave
    }
}
