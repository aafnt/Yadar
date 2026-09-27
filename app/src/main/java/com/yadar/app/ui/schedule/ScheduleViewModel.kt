package com.yadar.app.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yadar.app.domain.model.Collection
import com.yadar.app.domain.model.WidgetConfig
import com.yadar.app.domain.repository.CollectionRepository
import com.yadar.app.domain.repository.WidgetConfigRepository
import com.yadar.app.domain.usecase.ClearWidgetHistoryUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WidgetSummaryItem(val config: WidgetConfig, val collectionName: String)

/**
 * صفحه «برنامه نمایش» (بند ۶ سند پروژه): فهرست Widgetهای فعال و امکان مدیریت
 * سریع آن‌ها. تصمیم فنی مستندشده (بند ۵۸): بند ۲۲ سند («مدیریت Widget») را
 * همین تب قرار دادیم، چون محتوایش دقیقاً «برنامه نمایش» فعلی هر Widget است.
 */
class ScheduleViewModel(
    widgetConfigRepository: WidgetConfigRepository,
    collectionRepository: CollectionRepository,
    private val clearWidgetHistoryUseCase: ClearWidgetHistoryUseCase
) : ViewModel() {

    val widgets: StateFlow<List<WidgetSummaryItem>> = combine(
        widgetConfigRepository.observeAll(),
        collectionRepository.observeAll()
    ) { configs, collections ->
        configs.map { config ->
            val name = collections.firstOrNull { it.id == config.collectionId }?.name
            WidgetSummaryItem(config, name ?: "همه مجموعه‌ها")
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun resetHistory(widgetId: Int) = viewModelScope.launch { clearWidgetHistoryUseCase(widgetId) }
}
