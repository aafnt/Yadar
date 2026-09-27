package com.yadar.app.rules

import com.yadar.app.domain.model.DisplayRule
import com.yadar.app.domain.selector.RuleEvaluator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class RuleEvaluatorTest {

    // سناریو ۶ (بند ۵۱): جمله فقط برای جمعه؛ روز شنبه نباید انتخاب شود.
    @Test
    fun `weekday-only rule excludes other days`() {
        val rule = DisplayRule(weekdays = setOf(DayOfWeek.FRIDAY))
        val friday = LocalDate.of(2026, 10, 2) // جمعه
        val saturday = LocalDate.of(2026, 10, 3) // شنبه

        assertTrue(RuleEvaluator.isEligible(rule, friday, minuteOfDay = 600))
        assertFalse(RuleEvaluator.isEligible(rule, saturday, minuteOfDay = 600))
    }

    // سناریو ۷: جمله فقط برای ۲۰:۰۰ تا ۲۳:۰۰؛ ساعت ۱۵:۰۰ نباید انتخاب شود.
    @Test
    fun `normal (non-wrapping) time range excludes times outside it`() {
        val rule = DisplayRule(timeStartMinute = 20 * 60, timeEndMinute = 23 * 60)
        val date = LocalDate.of(2026, 10, 2)

        assertFalse(RuleEvaluator.isEligible(rule, date, minuteOfDay = 15 * 60))
        assertTrue(RuleEvaluator.isEligible(rule, date, minuteOfDay = 21 * 60))
    }

    // بند ۱۳: بازه‌ای که از نیمه‌شب عبور می‌کند (۲۰:۰۰ تا ۰۱:۰۰) باید درست مدیریت شود.
    @Test
    fun `midnight-crossing time range is handled correctly`() {
        val rule = DisplayRule(timeStartMinute = 20 * 60, timeEndMinute = 60) // 20:00 تا 01:00
        val date = LocalDate.of(2026, 10, 2)

        assertTrue(RuleEvaluator.isEligible(rule, date, minuteOfDay = 23 * 60)) // 23:00 -> مجاز
        assertTrue(RuleEvaluator.isEligible(rule, date, minuteOfDay = 30)) // 00:30 -> مجاز
        assertFalse(RuleEvaluator.isEligible(rule, date, minuteOfDay = 10 * 60)) // 10:00 -> غیرمجاز
    }

    @Test
    fun `date range excludes dates outside start-end`() {
        val start = LocalDate.of(2026, 9, 22).toEpochDay() // ~ ۱۴۰۵/۰۷/۰۱
        val end = LocalDate.of(2026, 10, 1).toEpochDay() // ~ ۱۴۰۵/۰۷/۱۰
        val rule = DisplayRule(startDateEpochDay = start, endDateEpochDay = end)

        assertTrue(RuleEvaluator.isEligible(rule, LocalDate.of(2026, 9, 25), minuteOfDay = 600))
        assertFalse(RuleEvaluator.isEligible(rule, LocalDate.of(2026, 10, 5), minuteOfDay = 600))
        assertFalse(RuleEvaluator.isEligible(rule, LocalDate.of(2026, 9, 10), minuteOfDay = 600))
    }

    @Test
    fun `combined rule requires all dimensions to match (AND)`() {
        val rule = DisplayRule(
            weekdays = setOf(DayOfWeek.FRIDAY),
            timeStartMinute = 6 * 60,
            timeEndMinute = 12 * 60
        )
        val fridayMorning = LocalDate.of(2026, 10, 2)
        assertTrue(RuleEvaluator.isEligible(rule, fridayMorning, minuteOfDay = 7 * 60))
        assertFalse(RuleEvaluator.isEligible(rule, fridayMorning, minuteOfDay = 15 * 60)) // روز درست، ساعت غلط
        assertFalse(RuleEvaluator.isEligible(rule, fridayMorning.plusDays(1), minuteOfDay = 7 * 60)) // ساعت درست، روز غلط
    }
}
