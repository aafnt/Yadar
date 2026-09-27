package com.yadar.app.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class PersianDateTest {

    // مقدار مرجع شناخته‌شده: ۱ ژانویه ۱۹۷۰ میلادی برابر است با ۱۱ دی ۱۳۴۸.
    @Test
    fun `unix epoch converts to the known reference Jalali date`() {
        val jalali = PersianDate.toJalali(LocalDate.of(1970, 1, 1))
        assertEquals(1348, jalali.year)
        assertEquals(10, jalali.month)
        assertEquals(11, jalali.day)
    }

    // نوروز ۱۴۰۵ باید ۲۱ مارس ۲۰۲۶ باشد.
    @Test
    fun `nowruz 1405 falls on 2026-03-21`() {
        val jalali = PersianDate.toJalali(LocalDate.of(2026, 3, 21))
        assertEquals(1405, jalali.year)
        assertEquals(1, jalali.month)
        assertEquals(1, jalali.day)
    }

    @Test
    fun `round trip conversion is stable over a wide date range`() {
        var date = LocalDate.of(1950, 1, 1)
        val end = LocalDate.of(2050, 1, 1)
        while (date.isBefore(end)) {
            val jalali = PersianDate.toJalali(date)
            val back = PersianDate.toGregorian(jalali)
            assertEquals("mismatch for $date", date, back)
            date = date.plusDays(137) // نمونه‌گیری پراکنده برای سرعت، نه هر روز
        }
    }

    @Test
    fun `parseJalaliString round trips with formatShort`() {
        val original = LocalDate.of(2026, 9, 27)
        val jalali = PersianDate.toJalali(original)
        val text = "${jalali.year}/${jalali.month}/${jalali.day}"
        val parsed = PersianDate.parseJalaliString(text)
        assertEquals(original, parsed)
    }

    @Test
    fun `parseJalaliString accepts Persian digits`() {
        val parsed = PersianDate.parseJalaliString("۱۴۰۵/۰۷/۰۱")
        assertEquals(LocalDate.of(2026, 9, 23), parsed)
    }
}
