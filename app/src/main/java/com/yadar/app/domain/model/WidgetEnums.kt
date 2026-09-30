package com.yadar.app.domain.model

/**
 * روش انتخاب جمله در Selection Engine (بند ۱۲ سند پروژه).
 * این یک تنظیم سطح Widget است چون باید در میان چند جمله واجد شرایط تصمیم بگیرد.
 */
enum class SelectionMode {
    /** یک جمله برای کل روز ثابت می‌ماند و با تغییر روز عوض می‌شود. */
    DAILY,

    /** با هر بار Refresh یک جمله تصادفی از میان واجد شرایط‌ها انتخاب می‌شود؛ بدون حافظه. */
    ON_CHANGE,

    /** به ترتیب ثابت (بر اساس ترتیب دستی یا id) پیش می‌رود و بعد از آخرین جمله به اول برمی‌گردد. */
    SEQUENTIAL,

    /** انتخاب کاملاً تصادفی، بدون هیچ محدودیتی روی تکرار. */
    RANDOM,

    /** تصادفی، اما تا زمانی که همه واجد شرایط‌ها نمایش داده نشده‌اند تکرار نمی‌شود. */
    RANDOM_WITHOUT_REPEAT
}

enum class FontChoice {
    VAZIRMATN, ESTEDAD, SAHEL, SHABNAM, SAMIM, LALEZAR, PERSIAN_SOLS
}

enum class WidgetTextAlignment { RIGHT, CENTER, LEFT }

enum class WidgetBackgroundMode { TRANSPARENT, SOLID, CUSTOM_ALPHA }

enum class WidgetDateDisplay { OFF, SHORT, SHORT_WITH_WEEKDAY }

enum class WidgetTouchAction { NEXT_SENTENCE, RANDOM_SENTENCE, OPEN_APP, NONE }

enum class SentenceSortOrder { NEWEST, OLDEST, ALPHABETIC, LAST_SHOWN, MANUAL }

enum class BackupConflictStrategy { REPLACE, MERGE }
