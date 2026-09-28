package com.yadar.app.util

/**
 * تبدیل اعداد لاتین به فارسی و برعکس.
 * تمام اعداد نمایش داده‌شده در UI باید از این Utility عبور کنند
 * تا در سراسر برنامه یکدست باشند (بند ۴ سند پروژه).
 */
object PersianDigits {

    private val latinDigits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    /** هر رشته حاوی عدد لاتین را به معادل فارسی تبدیل می‌کند؛ سایر کاراکترها دست‌نخورده می‌مانند. */
    fun toPersian(input: String): String {
        val builder = StringBuilder(input.length)
        for (ch in input) {
            val index = latinDigits.indexOf(ch)
            builder.append(if (index >= 0) persianDigits[index] else ch)
        }
        return builder.toString()
    }

    /** معادل فارسی یک عدد صحیح. */
    fun toPersian(number: Int): String = toPersian(number.toString())

    /** اعداد فارسی/عربی داخل متن را به لاتین برمی‌گرداند (مثلاً برای Parse کردن ورودی کاربر). */
    fun toLatin(input: String): String {
        val builder = StringBuilder(input.length)
        for (ch in input) {
            val persianIndex = persianDigits.indexOf(ch)
            when {
                persianIndex >= 0 -> builder.append(latinDigits[persianIndex])
                // اعداد عربی هم که گاهی از صفحه‌کلید وارد می‌شوند پشتیبانی شود: ٠١٢٣٤٥٦٧٨٩
                ch in '\u0660'..'\u0669' -> builder.append(latinDigits[ch - '\u0660'])
                else -> builder.append(ch)
            }
        }
        return builder.toString()
    }
}
