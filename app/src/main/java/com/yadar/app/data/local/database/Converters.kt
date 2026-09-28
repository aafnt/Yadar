package com.yadar.app.data.local.database

import androidx.room.TypeConverter
import com.yadar.app.domain.model.DisplayRuleType
import com.yadar.app.domain.model.FontChoice
import com.yadar.app.domain.model.SelectionMode
import com.yadar.app.domain.model.WidgetBackgroundMode
import com.yadar.app.domain.model.WidgetDateDisplay
import com.yadar.app.domain.model.WidgetTextAlignment
import com.yadar.app.domain.model.WidgetTouchAction

/** Room فقط انواع پایه را می‌شناسد؛ این کلاس Enumهای دامنه را به/از String تبدیل می‌کند. */
class Converters {

    @TypeConverter
    fun fromSelectionMode(value: SelectionMode): String = value.name

    @TypeConverter
    fun toSelectionMode(value: String): SelectionMode =
        runCatching { SelectionMode.valueOf(value) }.getOrDefault(SelectionMode.ON_CHANGE)

    @TypeConverter
    fun fromDisplayRuleType(value: DisplayRuleType): String = value.name

    @TypeConverter
    fun toDisplayRuleType(value: String): DisplayRuleType =
        runCatching { DisplayRuleType.valueOf(value) }.getOrDefault(DisplayRuleType.ON_CHANGE)

    @TypeConverter
    fun fromFontChoice(value: FontChoice): String = value.name

    @TypeConverter
    fun toFontChoice(value: String): FontChoice =
        runCatching { FontChoice.valueOf(value) }.getOrDefault(FontChoice.VAZIRMATN)

    @TypeConverter
    fun fromTextAlignment(value: WidgetTextAlignment): String = value.name

    @TypeConverter
    fun toTextAlignment(value: String): WidgetTextAlignment =
        runCatching { WidgetTextAlignment.valueOf(value) }.getOrDefault(WidgetTextAlignment.CENTER)

    @TypeConverter
    fun fromBackgroundMode(value: WidgetBackgroundMode): String = value.name

    @TypeConverter
    fun toBackgroundMode(value: String): WidgetBackgroundMode =
        runCatching { WidgetBackgroundMode.valueOf(value) }.getOrDefault(WidgetBackgroundMode.TRANSPARENT)

    @TypeConverter
    fun fromDateDisplay(value: WidgetDateDisplay): String = value.name

    @TypeConverter
    fun toDateDisplay(value: String): WidgetDateDisplay =
        runCatching { WidgetDateDisplay.valueOf(value) }.getOrDefault(WidgetDateDisplay.OFF)

    @TypeConverter
    fun fromTouchAction(value: WidgetTouchAction): String = value.name

    @TypeConverter
    fun toTouchAction(value: String): WidgetTouchAction =
        runCatching { WidgetTouchAction.valueOf(value) }.getOrDefault(WidgetTouchAction.NEXT_SENTENCE)
}
