package com.yadar.app.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import android.text.TextUtils
import androidx.core.content.res.ResourcesCompat
import com.yadar.app.domain.model.WidgetTextAlignment
import kotlin.math.roundToInt

/**
 * محدودیت مهم فنی (تأییدشده از مستندات رسمی Android): Jetpack Glance برای
 * AppWidget فقط از فونت‌های سیستمی (sans-serif/serif/monospace/cursive)
 * پشتیبانی می‌کند و «فونت‌های سفارشی برنامه پشتیبانی نمی‌شوند»
 * (developer.android.com/develop/ui/compose/glance/build-ui). یعنی تنظیم
 * `TextStyle(fontFamily = ...)` در Glance.Text هرگز فونت‌های بسته‌بندی‌شده
 * یادآر (Vazirmatn، Lalezar، Persian Sols و ...) را واقعاً به کار نمی‌برد و
 * بی‌صدا به فونت پیش‌فرض برمی‌گردد.
 *
 * تنها راه شناخته‌شده برای نمایش فونت واقعی روی Widget این است که خودمان متن
 * را با همان Typeface روی یک Bitmap شفاف رسم کنیم و آن Bitmap را به‌جای Text
 * نمایش دهیم (همان تکنیکی که کتابخانه‌های دیگر مثل Voltra برای این محدودیت
 * دقیقاً همین‌طور دور می‌زنند).
 */
object WidgetTextBitmapRenderer {

    data class Result(val bitmap: Bitmap, val widthPx: Int, val heightPx: Int)

    fun render(
        context: Context,
        text: String,
        fontResId: Int,
        fontSizeSp: Float,
        colorArgb: Int,
        opacityPercent: Int,
        isBold: Boolean,
        alignment: Layout.Alignment,
        maxLines: Int,
        lineSpacingMultiplier: Float,
        maxWidthPx: Int
    ): Result? {
        if (text.isBlank() || maxWidthPx <= 1) return null

        val baseTypeface = runCatching { ResourcesCompat.getFont(context, fontResId) }.getOrNull()
        val typeface = if (isBold) {
            Typeface.create(baseTypeface ?: Typeface.DEFAULT, Typeface.BOLD)
        } else {
            baseTypeface ?: Typeface.DEFAULT
        }

        val scaledDensity = context.resources.displayMetrics.scaledDensity

        val paint = TextPaint(TextPaint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typeface
            textSize = (fontSizeSp * scaledDensity).coerceAtLeast(1f)
            val alpha = ((opacityPercent.coerceIn(0, 100) / 100f) * 255).roundToInt()
            color = Color.argb(alpha, Color.red(colorArgb), Color.green(colorArgb), Color.blue(colorArgb))
        }

        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, paint, maxWidthPx)
            .setAlignment(alignment)
            .setLineSpacing(0f, lineSpacingMultiplier.coerceAtLeast(0.1f))
            .setIncludePad(false)
            .setMaxLines(maxLines.coerceAtLeast(1))
            .setEllipsize(TextUtils.TruncateAt.END)
            // جهت را همیشه راست‌به‌چپ ثابت می‌گیریم تا تراز «راست/وسط/چپ» کاربر
            // همیشه همان جهت مطلق صفحه باشد، نه نسبت به اولین کاراکتر جمله
            // (که می‌تواند عدد یا حرف لاتین باشد). الگوریتم BiDi یونیکد همچنان
            // برای بخش‌های لاتین/عددی داخل متن به‌درستی اعمال می‌شود؛ این فقط
            // جهت کلی پاراگراف را مشخص می‌کند (بند ۴ سند پروژه).
            .setTextDirection(TextDirectionHeuristics.RTL)
            .build()

        val width = layout.width.coerceAtLeast(1)
        val height = layout.height.coerceAtLeast(1)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        layout.draw(canvas)
        return Result(bitmap, width, height)
    }

    /** تراز متن انتخابی کاربر -> Layout.Alignment، با جهت پاراگراف ثابت RTL (بالا). */
    fun layoutAlignmentFor(textAlignment: WidgetTextAlignment): Layout.Alignment =
        when (textAlignment) {
            WidgetTextAlignment.RIGHT -> Layout.Alignment.ALIGN_NORMAL
            WidgetTextAlignment.CENTER -> Layout.Alignment.ALIGN_CENTER
            WidgetTextAlignment.LEFT -> Layout.Alignment.ALIGN_OPPOSITE
        }
}
