package com.yadar.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yadar.app.domain.model.FontChoice
import com.yadar.app.domain.model.SelectionMode
import com.yadar.app.domain.model.WidgetBackgroundMode
import com.yadar.app.domain.model.WidgetDateDisplay
import com.yadar.app.domain.model.WidgetTextAlignment
import com.yadar.app.domain.model.WidgetTouchAction

/**
 * تنظیمات مستقل هر Instance از Widget (بند ۲۱ و ۲۲ سند پروژه).
 *
 * [widgetId] همان AppWidgetId واقعی اندروید است (نه autoGenerate)، چون هر
 * Instance از AppWidget یک شناسه یکتا از سیستم می‌گیرد و همین شناسه کلید
 * جداسازی تنظیمات هر Widget از بقیه است.
 *
 * فیلدهای انتهایی (`current*`) وضعیت داخلی Selection Engine برای همین Widget
 * را نگه می‌دارند تا رفتار در Restart/Force-stop/Update و بازسازی Widget
 * کاملاً Deterministic بماند (بند ۲۳ و ۲۴ سند پروژه):
 * - [currentSentenceId] آخرین جمله‌ای که روی این Widget نمایش داده شده.
 * - [lastSelectionEpochDay] روزی (بر اساس [java.time.LocalDate.toEpochDay]) که
 *   [currentSentenceId] در حالت DAILY برای آن انتخاب شده؛ اگر روز جاری با این
 *   مقدار برابر بود، Render مجدد باید همان جمله را نشان دهد، نه انتخاب تازه.
 * - [sequentialCursorSentenceId] آخرین جمله‌ای که در حالت SEQUENTIAL نمایش
 *   داده شده، تا نوبت بعدی مشخص باشد.
 */
@Entity(tableName = "widget_configs")
data class WidgetConfigEntity(
    @PrimaryKey
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

    /** نمایش معنی زیر جمله، با فونت/اندازه جدا (فقط وقتی جمله معنی داشته باشد نمایش داده می‌شود). */
    val showMeaning: Boolean = true,
    val meaningFont: FontChoice = FontChoice.VAZIRMATN,
    val meaningFontSizeSp: Int = 14,

    val touchAction: WidgetTouchAction = WidgetTouchAction.NEXT_SENTENCE,

    val currentSentenceId: Long? = null,
    val lastSelectionEpochDay: Long? = null,
    val sequentialCursorSentenceId: Long? = null,

    val createdAt: Long
)
