package com.yadar.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.unit.ColorProvider
import com.yadar.app.R
import com.yadar.app.YadarApplication
import com.yadar.app.domain.model.FontChoice
import com.yadar.app.domain.model.Sentence
import com.yadar.app.domain.model.WidgetBackgroundMode
import com.yadar.app.domain.model.WidgetConfig
import com.yadar.app.domain.model.WidgetDateDisplay
import com.yadar.app.domain.model.WidgetTextAlignment
import com.yadar.app.domain.model.WidgetTouchAction
import com.yadar.app.util.PersianDate
import androidx.compose.ui.graphics.Color as ComposeColor
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Widget اصلی یادآر (بند ۱۵ تا ۲۰ سند پروژه).
 *
 * پیش‌فرض کاملاً شفاف است: بدون Card، بدون Background اجباری، بدون آیکون یا
 * عنوان — فقط متن جمله روی Wallpaper. اندازه با [SizeMode.Exact] واقعی خوانده
 * می‌شود تا محتوا متناسب با اندازه واقعی هر Instance از Widget باشد.
 */
class YadarWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val app = context.applicationContext as YadarApplication
        val container = app.container

        val config = container.widgetConfigRepository.getById(appWidgetId)

        // نکته مهم (رفع باگ «لمس فقط یک‌بار کار می‌کند»): اینجا هرگز دوباره
        // Selection Engine را برای انتخاب جمله تازه صدا نمی‌زنیم. آن منطق فقط در
        // لحظاتی که واقعاً باید انتخاب عوض شود اجرا می‌شود: لمس کاربر
        // (WidgetTouchHandler)، ذخیره تنظیمات Widget (WidgetConfigActivity)، رسیدن
        // به مرز زمانی یا نیمه‌شب (WidgetRefreshAlarmReceiver)، یا باز شدن قفل صفحه
        // (ScreenUnlockReceiver). اگر اینجا هم دوباره انتخاب می‌کردیم، همان Render
        // که بلافاصله بعد از هر لمس اتفاق می‌افتد، انتخاب لمس را فوری بازنویسی
        // می‌کرد و به نظر می‌رسید لمس فقط دفعه اول اثر دارد (بند ۲۳/۲۴ سند پروژه:
        // Render مجدد نباید بدون دلیل جمله را عوض کند).
        var sentence = config?.currentSentenceId?.let { container.sentenceRepository.getById(it) }
        if (config != null && (sentence == null || !sentence.isActive)) {
            // اولین بار بعد از ساخت Widget، یا جمله‌ی قبلی حذف/غیرفعال شده: یک انتخاب اولیه لازم است.
            sentence = container.refreshWidgetSentenceUseCase(appWidgetId, LocalDateTime.now())
        }

        provideContent {
            GlanceTheme {
                WidgetContent(context = context, config = config, sentence = sentence)
            }
        }
    }

    companion object {
        /** برای فراخوانی از UseCase/Updater بدون وابستگی مستقیم UI به این کلاس. */
        suspend fun updateAll(context: Context) {
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(YadarWidget::class.java)
            val widget = YadarWidget()
            glanceIds.forEach { glanceId -> widget.update(context, glanceId) }
        }

        suspend fun updateOne(context: Context, appWidgetId: Int) {
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(YadarWidget::class.java)
            glanceIds.firstOrNull { manager.getAppWidgetId(it) == appWidgetId }?.let { glanceId ->
                YadarWidget().update(context, glanceId)
            }
        }
    }
}

@Composable
private fun WidgetContent(context: Context, config: WidgetConfig?, sentence: Sentence?) {
    if (config == null) {
        Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {}
        return
    }

    val backgroundModifier = when (config.backgroundMode) {
        WidgetBackgroundMode.TRANSPARENT -> GlanceModifier.fillMaxSize()
        WidgetBackgroundMode.SOLID -> GlanceModifier.fillMaxSize()
            .background(ColorProvider(ComposeColor(config.backgroundColorArgb)))
        WidgetBackgroundMode.CUSTOM_ALPHA -> GlanceModifier.fillMaxSize()
            .background(
                ColorProvider(
                    ComposeColor(config.backgroundColorArgb).copy(alpha = config.backgroundAlphaPercent / 100f)
                )
            )
    }

    val touchModifier = when (config.touchAction) {
        WidgetTouchAction.NEXT_SENTENCE -> GlanceModifier.clickable(actionRunCallback<NextSentenceActionCallback>())
        WidgetTouchAction.RANDOM_SENTENCE -> GlanceModifier.clickable(actionRunCallback<RandomSentenceActionCallback>())
        WidgetTouchAction.OPEN_APP -> GlanceModifier.clickable(actionRunCallback<OpenAppActionCallback>())
        WidgetTouchAction.NONE -> GlanceModifier
    }

    val boxAlignment = when (config.textAlignment) {
        WidgetTextAlignment.RIGHT -> Alignment.CenterEnd
        WidgetTextAlignment.CENTER -> Alignment.Center
        WidgetTextAlignment.LEFT -> Alignment.CenterStart
    }
    val layoutAlignment = WidgetTextBitmapRenderer.layoutAlignmentFor(config.textAlignment)

    // بند «بالا و پایین جمله خیلی خالی است»: فونت را متناسب با ارتفاع واقعی
    // Widget کمی بزرگ‌تر می‌کنیم (هیچ‌وقت از اندازه انتخابی کاربر کوچک‌تر نمی‌شود).
    val density = context.resources.displayMetrics.density
    val availableHeightDp = LocalSize.current.height.value
    val availableWidthDp = LocalSize.current.width.value
    val horizontalPaddingDp = 24f // ۱۲dp از هر طرف
    val maxWidthPx = (((availableWidthDp - horizontalPaddingDp) * density).toInt()).coerceAtLeast(1)

    val referenceHeightDp = 64f
    val textLength = sentence?.text?.length ?: 0
    val maxScaleForLength = when {
        textLength > 160 -> 1f
        textLength > 80 -> 1.25f
        else -> 1.6f
    }
    val heightScale = (availableHeightDp / referenceHeightDp).coerceIn(1f, maxScaleForLength)
    val effectiveFontSizeSp = config.fontSizeSp * heightScale

    val meaningText = sentence?.meaning?.takeIf { it.isNotBlank() }
    val showMeaning = config.showMeaning && meaningText != null

    val mainResult = sentence?.text?.let {
        WidgetTextBitmapRenderer.render(
            context = context,
            text = it,
            fontResId = fontResIdFor(config.font),
            fontSizeSp = effectiveFontSizeSp,
            colorArgb = config.fontColorArgb,
            opacityPercent = config.opacityPercent,
            isBold = config.fontWeight >= 700,
            alignment = layoutAlignment,
            maxLines = config.maxLines,
            lineSpacingMultiplier = config.lineSpacingMultiplier,
            maxWidthPx = maxWidthPx
        )
    }

    val meaningResult = if (showMeaning) {
        WidgetTextBitmapRenderer.render(
            context = context,
            text = meaningText.orEmpty(),
            fontResId = fontResIdFor(config.meaningFont),
            fontSizeSp = config.meaningFontSizeSp * heightScale,
            colorArgb = config.fontColorArgb,
            opacityPercent = (config.opacityPercent * 0.85f).toInt(),
            isBold = false,
            alignment = layoutAlignment,
            maxLines = config.maxLines,
            lineSpacingMultiplier = config.lineSpacingMultiplier,
            maxWidthPx = maxWidthPx
        )
    } else null

    val dateResult = if (config.showDate && config.dateDisplay != WidgetDateDisplay.OFF) {
        val today = LocalDate.now()
        val dateText = if (config.dateDisplay == WidgetDateDisplay.SHORT_WITH_WEEKDAY) {
            PersianDate.formatShortWithWeekday(today)
        } else {
            PersianDate.formatShort(today)
        }
        WidgetTextBitmapRenderer.render(
            context = context,
            text = dateText,
            fontResId = fontResIdFor(config.font),
            fontSizeSp = config.fontSizeSp * 0.6f,
            colorArgb = config.fontColorArgb,
            opacityPercent = (config.opacityPercent * 0.7f).toInt(),
            isBold = false,
            alignment = layoutAlignment,
            maxLines = 1,
            lineSpacingMultiplier = 1f,
            maxWidthPx = maxWidthPx
        )
    } else null

    Box(
        modifier = backgroundModifier.then(touchModifier).padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = boxAlignment
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            mainResult?.let { result ->
                Image(
                    provider = ImageProvider(result.bitmap),
                    contentDescription = null,
                    modifier = GlanceModifier
                        .width((result.widthPx / density).dp)
                        .height((result.heightPx / density).dp)
                )
            }
            meaningResult?.let { result ->
                Image(
                    provider = ImageProvider(result.bitmap),
                    contentDescription = null,
                    modifier = GlanceModifier
                        .width((result.widthPx / density).dp)
                        .height((result.heightPx / density).dp)
                )
            }
            dateResult?.let { result ->
                Image(
                    provider = ImageProvider(result.bitmap),
                    contentDescription = null,
                    modifier = GlanceModifier
                        .width((result.widthPx / density).dp)
                        .height((result.heightPx / density).dp)
                )
            }
        }
    }
}

/** هر FontChoice به فایل XML خانواده فونت مربوط به خودش در res/font نگاشت می‌شود. */
private fun fontResIdFor(choice: FontChoice): Int = when (choice) {
    FontChoice.VAZIRMATN -> R.font.vazirmatn
    FontChoice.ESTEDAD -> R.font.estedad
    FontChoice.SAHEL -> R.font.sahel
    FontChoice.SHABNAM -> R.font.shabnam
    FontChoice.SAMIM -> R.font.samim
    FontChoice.LALEZAR -> R.font.lalezar
    FontChoice.PERSIAN_SOLS -> R.font.persian_sols
}

class NextSentenceActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        WidgetTouchHandler.handleTouch(context, glanceId)
    }
}

class RandomSentenceActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        WidgetTouchHandler.handleTouch(context, glanceId)
    }
}

class OpenAppActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        WidgetTouchHandler.openApp(context)
    }
}
