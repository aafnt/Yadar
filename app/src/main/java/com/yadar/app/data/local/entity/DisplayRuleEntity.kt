package com.yadar.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.yadar.app.domain.model.DisplayRuleType

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
 *
 * [DisplayRuleType] در domain.model تعریف شده (نه اینجا) چون یک مفهوم دامنه‌ای
 * است که ViewModelها و UseCaseها هم مستقیماً از آن استفاده می‌کنند.
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
