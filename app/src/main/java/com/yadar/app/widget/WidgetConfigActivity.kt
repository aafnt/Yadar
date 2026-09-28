package com.yadar.app.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yadar.app.YadarApplication
import com.yadar.app.ui.theme.YadarTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class WidgetConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // طبق مستندات AppWidget: تا وقتی نتیجه با موفقیت تنظیم نشده، CANCELED در نظر گرفته شود
        // تا اگر کاربر فرآیند را نیمه‌کاره رها کند، Widget اضافه نشود.
        setResult(Activity.RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val container = (application as YadarApplication).container

        // مقدار تم را قبل از ساخت UI همگام می‌خوانیم تا صفحه حتی لحظه‌ای با تم سیستم نمایش داده نشود.
        val initialSettings = runBlocking { container.settingsRepository.settings.first() }

        setContent {
            // تم و رنگ Accent را از تنظیمات ذخیره‌شده برنامه می‌خوانیم (نه از سیستم گوشی)
            val settings by container.settingsRepository.settings
                .collectAsState(initial = initialSettings)
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                YadarTheme(
                    themeMode = settings.themeMode,
                    accentColor = Color(settings.accentColorArgb)
                ) {
                  Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val vm = viewModel<WidgetConfigViewModel>(factory = viewModelFactory {
                        initializer {
                            WidgetConfigViewModel(
                                appWidgetId,
                                container.widgetConfigRepository,
                                container.collectionRepository,
                                container.saveWidgetConfigUseCase
                            )
                        }
                    })
                    WidgetConfigScreen(
                        viewModel = vm,
                        onSave = {
                            // باید پیش از finish() و داخل همین Coroutine اجرا شود؛ در غیر این صورت با بسته‌شدن
                            // Activity لغو می‌شود و Widget بعد از تغییر تنظیمات بازسازی نمی‌شود.
                            YadarWidget.updateOne(this@WidgetConfigActivity, appWidgetId)
                            AlarmRefreshScheduler(this@WidgetConfigActivity).scheduleNext()
                            val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                            setResult(Activity.RESULT_OK, resultValue)
                            finish()
                        }
                    )
                  }
                }
            }
        }
    }
}
