package com.yadar.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.action.ActionParameters
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontFamily
import androidx.glance.text.FontStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
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
        val sentence = if (config != null) {
            container.refreshWidgetSentenceUseCase(appWidgetId, LocalDateTime.now())
        } else null

        provideContent {
            GlanceTheme {
                WidgetContent(config = config, sentence = sentence)
            }
        }
    }

    companion object {
        /** برای فراخوانی از UseCase/Updater بدون وابستگی مستقیم UI به این کلاس. */
        suspend fun updateAll(context: Context) {
            YadarWidget().updateAll(context)
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
private fun WidgetContent(config: WidgetConfig?, sentence: Sentence?) {
    if (config == null) {
        // Widget هنوز Configure نشده (نباید معمولاً رخ دهد چون Config اجباری است).
        Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "")
        }
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

    val alignment = when (config.textAlignment) {
        WidgetTextAlignment.RIGHT -> Alignment.CenterEnd
        WidgetTextAlignment.CENTER -> Alignment.Center
        WidgetTextAlignment.LEFT -> Alignment.CenterStart
    }
    val textAlign = when (config.textAlignment) {
        WidgetTextAlignment.RIGHT -> TextAlign.Right
        WidgetTextAlignment.CENTER -> TextAlign.Center
        WidgetTextAlignment.LEFT -> TextAlign.Left
    }

    Box(
        modifier = backgroundModifier.then(touchModifier).padding(12.dp),
        contentAlignment = alignment
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = sentence?.text ?: "",
                maxLines = config.maxLines,
                style = TextStyle(
                    color = ColorProvider(
                        ComposeColor(config.fontColorArgb).copy(alpha = config.opacityPercent / 100f)
                    ),
                    fontSize = config.fontSizeSp.sp,
                    fontWeight = if (config.fontWeight >= 700) FontWeight.Bold else FontWeight.Normal,
                    fontStyle = FontStyle.Normal,
                    fontFamily = fontFamilyForWidget(config.font),
                    textAlign = textAlign
                )
            )
            if (config.showDate && config.dateDisplay != WidgetDateDisplay.OFF) {
                val today = LocalDate.now()
                val dateText = if (config.dateDisplay == WidgetDateDisplay.SHORT_WITH_WEEKDAY) {
                    PersianDate.formatShortWithWeekday(today)
                } else {
                    PersianDate.formatShort(today)
                }
                Text(
                    text = dateText,
                    style = TextStyle(
                        color = ColorProvider(
                            ComposeColor(config.fontColorArgb).copy(alpha = (config.opacityPercent * 0.7f) / 100f)
                        ),
                        fontSize = (config.fontSizeSp * 0.6f).sp,
                        fontFamily = fontFamilyForWidget(config.font),
                        textAlign = textAlign
                    )
                )
            }
        }
    }
}

private fun fontFamilyForWidget(choice: FontChoice): FontFamily = when (choice) {
    // نام هر FontFamily باید دقیقاً با نام فایل XML داخل res/font مطابقت داشته باشد
    // تا Glance بتواند فونت سفارشی را در زمان اجرا Resolve کند.
    FontChoice.VAZIRMATN -> FontFamily("vazirmatn")
    FontChoice.ESTEDAD -> FontFamily("estedad")
    FontChoice.SAHEL -> FontFamily("sahel")
    FontChoice.SHABNAM -> FontFamily("shabnam")
    FontChoice.SAMIM -> FontFamily("samim")
    FontChoice.LALEZAR -> FontFamily("lalezar")
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
