package com.yadar.app.util

import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Utility مرکزی برای تقویم شمسی.
 *
 * تمام تبدیل‌های میلادی↔شمسی در برنامه باید فقط از این کلاس عبور کنند
 * (بند ۱۴ سند پروژه) تا از تبدیل دستی و شکننده در جاهای مختلف جلوگیری شود.
 *
 * ذخیره‌سازی در Database همیشه بر اساس [LocalDate.toEpochDay] (عدد روز، بدون
 * زمان و بدون Time Zone) انجام می‌شود تا تغییر منطقه زمانی هرگز باعث
 * جابه‌جایی روز نشود (بند ۱۳ و ۲۳ سند پروژه). تبدیل به شمسی فقط برای نمایش
 * در UI انجام می‌شود.
 *
 * الگوریتم تبدیل بر اساس روش قطعی Kazimierz M. Borkowski است (همان الگوریتم
 * استفاده‌شده در کتابخانه‌ی معروف jalaali-js)، که یک الگوریتم ریاضی دقیق و
 * بدون جدول lookup محدود است.
 */
object PersianDate {

    data class Jalali(val year: Int, val month: Int, val day: Int)

    private val breaks = intArrayOf(
        -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181, 1210,
        1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178
    )

    // معادل ~~(a/b) در جاوااسکریپت: تقسیم صحیح با truncation به سمت صفر.
    // تقسیم اعداد صحیح در Kotlin هم دقیقاً همین رفتار را دارد.
    private fun div(a: Int, b: Int): Int = a / b

    private fun mod(a: Int, b: Int): Int = a - (a / b) * b

    private data class JalCalResult(val leap: Int, val gy: Int, val march: Int)

    private fun jalCal(jy: Int): JalCalResult {
        val bl = breaks.size
        val gy = jy + 621
        var leapJ = -14
        var jp = breaks[0]
        var jm: Int
        var jump = 0
        var n: Int
        var i = 1

        require(jy >= jp && jy < breaks[bl - 1]) { "سال جلالی نامعتبر: $jy" }

        while (i < bl) {
            jm = breaks[i]
            jump = jm - jp
            if (jy < jm) break
            leapJ += div(jump, 33) * 8 + div(mod(jump, 33), 4)
            jp = jm
            i += 1
        }
        n = jy - jp

        leapJ += div(n, 33) * 8 + div(mod(n, 33) + 3, 4)
        if (mod(jump, 33) == 4 && jump - n == 4) {
            leapJ += 1
        }

        val leapG = div(gy, 4) - div((div(gy, 100) + 1) * 3, 4) - 150
        val march = 20 + leapJ - leapG

        if (jump - n < 6) {
            n = n - jump + div(jump + 4, 33) * 33
        }
        var leap = mod(mod(n + 1, 33) - 1, 4)
        if (leap == -1) leap = 4

        return JalCalResult(leap, gy, march)
    }

    private fun g2d(gy: Int, gm: Int, gd: Int): Int {
        var d = div((gy + div(gm - 8, 6) + 100100) * 1461, 4) +
            div(153 * mod(gm + 9, 12) + 2, 5) +
            gd - 34840408
        d -= div(div(gy + 100100 + div(gm - 8, 6), 100) * 3, 4) - 752
        return d
    }

    private data class GregorianResult(val gy: Int, val gm: Int, val gd: Int)

    private fun d2g(jdn: Int): GregorianResult {
        var j = 4 * jdn + 139361631
        j += div(div(4 * jdn + 183187720, 146097) * 3, 4) * 4 - 3908
        val i = div(mod(j, 1461), 4) * 5 + 308
        val gd = div(mod(i, 153), 5) + 1
        val gm = mod(div(i, 153), 12) + 1
        val gy = div(j, 1461) - 100100 + div(8 - gm, 6)
        return GregorianResult(gy, gm, gd)
    }

    private fun j2d(jy: Int, jm: Int, jd: Int): Int {
        val r = jalCal(jy)
        return g2d(r.gy, 3, r.march) + (jm - 1) * 31 - div(jm, 7) * (jm - 7) + jd - 1
    }

    private fun d2j(jdn: Int): Jalali {
        val gy = d2g(jdn).gy
        var jy = gy - 621
        val r = jalCal(jy)
        val jdn1f = g2d(r.gy, 3, r.march)
        var k = jdn - jdn1f
        val jm: Int
        val jd: Int

        if (k >= 0) {
            if (k <= 185) {
                jm = 1 + div(k, 31)
                jd = mod(k, 31) + 1
                return Jalali(jy, jm, jd)
            } else {
                k -= 186
            }
        } else {
            jy -= 1
            k += 179
            if (r.leap == 1) k += 1
        }
        jm = 7 + div(k, 30)
        jd = mod(k, 30) + 1
        return Jalali(jy, jm, jd)
    }

    // Julian Day Number مبنای 1970-01-01 میلادی که معادل epoch day جاوا است.
    private const val EPOCH_JDN_OFFSET = 2440588

    /** [LocalDate] را به تاریخ شمسی تبدیل می‌کند. */
    fun toJalali(date: LocalDate): Jalali {
        val jdn = date.toEpochDay().toInt() + EPOCH_JDN_OFFSET
        return d2j(jdn)
    }

    /** تاریخ شمسی را به [LocalDate] (میلادی) تبدیل می‌کند. */
    fun toGregorian(jalali: Jalali): LocalDate {
        val jdn = j2d(jalali.year, jalali.month, jalali.day)
        val epochDay = (jdn - EPOCH_JDN_OFFSET).toLong()
        return LocalDate.ofEpochDay(epochDay)
    }

    val monthNames = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    val weekdayNames = mapOf(
        DayOfWeek.SATURDAY to "شنبه",
        DayOfWeek.SUNDAY to "یکشنبه",
        DayOfWeek.MONDAY to "دوشنبه",
        DayOfWeek.TUESDAY to "سه‌شنبه",
        DayOfWeek.WEDNESDAY to "چهارشنبه",
        DayOfWeek.THURSDAY to "پنجشنبه",
        DayOfWeek.FRIDAY to "جمعه"
    )

    /** مثلاً «جمعه ۴ مهر ۱۴۰۵» */
    fun formatFull(date: LocalDate): String {
        val jalali = toJalali(date)
        val weekday = weekdayNames.getValue(date.dayOfWeek)
        val day = PersianDigits.toPersian(jalali.day)
        val month = monthNames[jalali.month - 1]
        val year = PersianDigits.toPersian(jalali.year)
        return "$weekday $day $month $year"
    }

    /** مثلاً «۴ مهر ۱۴۰۵» بدون نام روز هفته، برای نمایش کوتاه روی Widget. */
    fun formatShort(date: LocalDate): String {
        val jalali = toJalali(date)
        val day = PersianDigits.toPersian(jalali.day)
        val month = monthNames[jalali.month - 1]
        val year = PersianDigits.toPersian(jalali.year)
        return "$day $month $year"
    }

    /** مثلاً «جمعه ۴ مهر» بدون سال، برای نمایش خیلی کوتاه روی Widget. */
    fun formatShortWithWeekday(date: LocalDate): String {
        val jalali = toJalali(date)
        val weekday = weekdayNames.getValue(date.dayOfWeek)
        val day = PersianDigits.toPersian(jalali.day)
        val month = monthNames[jalali.month - 1]
        return "$weekday $day $month"
    }

    /** رشته شمسی «۱۴۰۵/۰۷/۰۱» را به [LocalDate] Parse می‌کند؛ در صورت نامعتبر بودن null برمی‌گرداند. */
    fun parseJalaliString(input: String): LocalDate? {
        val normalized = PersianDigits.toLatin(input.trim())
        val parts = normalized.split("/", "-")
        if (parts.size != 3) return null
        return try {
            val y = parts[0].toInt()
            val m = parts[1].toInt()
            val d = parts[2].toInt()
            if (m !in 1..12 || d !in 1..31) return null
            toGregorian(Jalali(y, m, d))
        } catch (e: NumberFormatException) {
            null
        }
    }
}
