package com.yadar.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * برچسبی که کاربر هنگام افزودن جمله انتخاب می‌کند (بند ۹ سند پروژه).
 * این فیلد فقط تعیین می‌کند کدام بخش‌های ورودی (روز هفته/ساعت/تاریخ) در فرم
 * افزودن جمله نمایش داده شوند؛ منطق واقعی واجد شرایط بودن همیشه بر اساس
 * مقادیر واقعی [DisplayRuleEntity] (نه این برچسب) محاسبه می‌شود، چون همه‌ی
 * این فیلدها می‌توانند هم‌زمان مقدار داشته باشند (حالت «ترکیبی»).
 *
 * تصمیم فنی مستندشده (بند ۵۸): «روش انتخاب» واقعی (Sequential/Random/...)
 * که در بند ۱۲ تعریف شده، یک تنظیم سطح Widget است (چون باید بین چند جمله
 * انتخاب کند)، نه سطح تک‌جمله؛ در [com.yadar.app.data.local.entity.WidgetConfigEntity]
 * نگه‌داری می‌شود. این برچسب فقط برای UI فرم افزودن جمله است.
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

/**
 * قوانین زمانی یک جمله (بند ۱۳ سند پروژه). رابطه یک‌به‌یک با [SentenceEntity].
 *
 * تمام فیلدهای فیلتر nullable هستند و «بدون محدودیت» به معنای null است.
 * چند فیلد می‌توانند هم‌زمان مقدار داشته باشند و با AND ترکیب می‌شوند (حالت ترکیبی).
 *
 * - [weekdays]: نام روزهای هفته با فرمت [java.time.DayOfWeek.name] جدا‌شده با کاما،
 *   مثلاً "FRIDAY,SATURDAY". null یعنی همه روزها مجاز است.
 * - [timeStartMinute] / [timeEndMinute]: دقیقه از نیمه‌شب (۰ تا ۱۴۳۹). اگر بازه از
 *   نیمه‌شب عبور کند (مثلاً ۲۰:۰۰ تا ۰۱:۰۰)، end کوچک‌تر از start خواهد بود و
 *   RuleEvaluator این حالت را جداگانه مدیریت می‌کند (بند ۱۳).
 * - [startDateEpochDay] / [endDateEpochDay]: بر اساس [java.time.LocalDate.toEpochDay]
 *   ذخیره می‌شوند، نه رشته یا timestamp با Time Zone، تا تغییر منطقه زمانی هرگز
 *   باعث جابه‌جایی روز نشود (بند ۱۳).
 */
@Entity(
    tableName = "display_rules",
    foreignKeys = [
        ForeignKey(
            entity = SentenceEntity::class,
            parentColumns = ["id"],
            childColumns = ["sentenceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sentenceId", unique = true)]
)
data class DisplayRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sentenceId: Long,
    val type: DisplayRuleType = DisplayRuleType.ON_CHANGE,
    val weekdays: String? = null,
    val timeStartMinute: Int? = null,
    val timeEndMinute: Int? = null,
    val startDateEpochDay: Long? = null,
    val endDateEpochDay: Long? = null
)
