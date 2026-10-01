package com.yadar.app.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yadar.app.domain.model.WidgetConfig
import com.yadar.app.domain.repository.CollectionRepository
import com.yadar.app.domain.repository.SentenceRepository
import com.yadar.app.domain.repository.WidgetConfigRepository
import com.yadar.app.domain.selector.SelectionEngine
import com.yadar.app.domain.usecase.ClearWidgetHistoryUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime

data class WidgetSummaryItem(
    val config: WidgetConfig,
    val collectionName: String,
    /**
     * تعداد جمله‌هایی که همین الان برای همین Widget واجد شرایطند (بعد از فیلتر
     * مجموعه + قوانین زمانی هر جمله). این عدد ابزار تشخیص است: اگر کاربر چند
     * جمله فعال دارد ولی لمس Widget اثری ندارد، معمولاً این عدد ۱ یا صفر است
     * (یعنی باقی جمله‌ها به‌خاطر مجموعه یا قانون زمانی فعلاً حذف شده‌اند).
     */
    val eligibleNowCount: Int
)

/**
 * صفحه «برنامه نمایش» (بند ۶ سند پروژه): فهرست Widgetهای فعال و امکان مدیریت
 * سریع آن‌ها. تصمیم فنی مستندشده (بند ۵۸): بند ۲۲ سند («مدیریت Widget») را
 * همین تب قرار دادیم، چون محتوایش دقیقاً «برنامه نمایش» فعلی هر Widget است.
 */
class ScheduleViewModel(
    widgetConfigRepository: WidgetConfigRepository,
    collectionRepository: CollectionRepository,
    private val sentenceRepository: SentenceRepository,
    private val selectionEngine: SelectionEngine,
    private val clearWidgetHistoryUseCase: ClearWidgetHistoryUseCase
) : ViewModel() {

    val widgets: StateFlow<List<WidgetSummaryItem>> = combine(
        widgetConfigRepository.observeAll(),
        collectionRepository.observeAll(),
        // با هر تغییر جمله‌ها هم این شمارش به‌روز شود (مثلاً وقتی کاربر جمله‌ای را فعال/غیرفعال می‌کند).
        sentenceRepository.observeAll().map { }
    ) { configs, collections, _ ->
        val now = LocalDateTime.now()
        configs.map { config ->
            val name = collections.firstOrNull { it.id == config.collectionId }?.name
            val pool = sentenceRepository.getActiveEligiblePool(config.collectionId)
            val eligibleCount = selectionEngine.filterEligible(pool, now).size
            WidgetSummaryItem(config, name ?: "همه مجموعه‌ها", eligibleCount)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun resetHistory(widgetId: Int) = viewModelScope.launch { clearWidgetHistoryUseCase(widgetId) }
}
