package com.yadar.app.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yadar.app.YadarApplication
import com.yadar.app.ui.theme.YadarTheme
import kotlinx.coroutines.launch

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

        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                YadarTheme {
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
                            lifecycleScope.launch {
                                YadarWidget.updateOne(this@WidgetConfigActivity, appWidgetId)
                                AlarmRefreshScheduler(this@WidgetConfigActivity).scheduleNext()
                            }
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
