package com.yadar.app.domain.model

import java.time.DayOfWeek

data class Collection(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * برچسبی که کاربر هنگام افزودن جمله انتخاب می‌کند (بند ۹ سند پروژه).
 * فقط تعیین می‌کند کدام فیلدهای ورودی (روز هفته/ساعت/تاریخ) در فرم نمایش
 * داده شوند؛ واجد شرایط بودن واقعی همیشه بر اساس مقادیر واقعی [DisplayRule]
 * محاسبه می‌شود (بند ۵۸، تصمیم فنی مستندشده).
 */
enum class DisplayRuleType {
    DAILY,
    ON_CHANGE,
    SEQUENTIAL,
    RANDOM,
    BY_WEEKDAY,
    BY_TIME,
    BY_DATE,
    COMBINED
}

/** null یعنی «بدون محدودیت» برای هر فیلد؛ چند فیلد هم‌زمان یعنی حالت ترکیبی (AND). */
data class DisplayRule(
    val type: DisplayRuleType = DisplayRuleType.ON_CHANGE,
    val weekdays: Set<DayOfWeek>? = null,
    val timeStartMinute: Int? = null,
    val timeEndMinute: Int? = null,
    val startDateEpochDay: Long? = null,
    val endDateEpochDay: Long? = null
) {
    companion object {
        val NONE = DisplayRule()
    }
}

data class Sentence(
    val id: Long = 0,
    val text: String,
    val collectionId: Long? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val manualSortOrder: Long = 0,
    val lastShownAt: Long? = null,
    val displayRule: DisplayRule = DisplayRule.NONE,
    /** معنی/ترجمه اختیاری؛ اگر خالی باشد فقط خود جمله در Widget نشان داده می‌شود. */
    val meaning: String? = null
)

data class WidgetConfig(
    val widgetId: Int,
    val collectionId: Long? = null,
    val selectionMode: SelectionMode = SelectionMode.ON_CHANGE,
    val font: FontChoice = FontChoice.VAZIRMATN,
    val fontSizeSp: Int = 18,
    val fontWeight: Int = 400,
    val fontColorArgb: Int = 0xFFFFFFFF.toInt(),
    val opacityPercent: Int = 100,
    val textAlignment: WidgetTextAlignment = WidgetTextAlignment.CENTER,
    val lineSpacingMultiplier: Float = 1.0f,
    val maxLines: Int = 4,
    val backgroundMode: WidgetBackgroundMode = WidgetBackgroundMode.TRANSPARENT,
    val backgroundColorArgb: Int = 0xFF000000.toInt(),
    val backgroundAlphaPercent: Int = 40,
    val showDate: Boolean = false,
    val dateDisplay: WidgetDateDisplay = WidgetDateDisplay.OFF,
    val showMeaning: Boolean = true,
    val meaningFont: FontChoice = FontChoice.VAZIRMATN,
    val meaningFontSizeSp: Int = 14,
    val touchAction: WidgetTouchAction = WidgetTouchAction.NEXT_SENTENCE,
    val currentSentenceId: Long? = null,
    val lastSelectionEpochDay: Long? = null,
    val sequentialCursorSentenceId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
