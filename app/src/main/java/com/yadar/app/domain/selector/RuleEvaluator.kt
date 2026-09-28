package com.yadar.app.domain.selector

import com.yadar.app.domain.model.DisplayRule
import java.time.LocalDate

/**
 * بررسی واجد شرایط بودن یک جمله در یک لحظه مشخص (بند ۱۱ و ۱۳ سند پروژه).
 * تمام قوانین Nullable هستند و هرکدام که مقدار داشته باشد باید رعایت شود (AND).
 * این کلاس کاملاً خالص است (بدون وابستگی به Android) تا در Unit Test ساده باشد.
 */
object RuleEvaluator {

    fun isEligible(rule: DisplayRule, date: LocalDate, minuteOfDay: Int): Boolean {
        if (!isWithinDateRange(rule, date)) return false
        if (!isAllowedWeekday(rule, date)) return false
        if (!isWithinTimeRange(rule, minuteOfDay)) return false
        return true
    }

    private fun isWithinDateRange(rule: DisplayRule, date: LocalDate): Boolean {
        val epochDay = date.toEpochDay()
        rule.startDateEpochDay?.let { if (epochDay < it) return false }
        rule.endDateEpochDay?.let { if (epochDay > it) return false }
        return true
    }

    private fun isAllowedWeekday(rule: DisplayRule, date: LocalDate): Boolean {
        val allowed = rule.weekdays ?: return true
        return date.dayOfWeek in allowed
    }

    /** بازه‌هایی که از نیمه‌شب عبور می‌کنند (مثل ۲۰:۰۰ تا ۰۱:۰۰) به‌درستی مدیریت می‌شوند. */
    private fun isWithinTimeRange(rule: DisplayRule, minuteOfDay: Int): Boolean {
        val start = rule.timeStartMinute
        val end = rule.timeEndMinute
        if (start == null || end == null) return true
        return if (start <= end) {
            minuteOfDay in start..end
        } else {
            minuteOfDay >= start || minuteOfDay <= end
        }
    }
}
