package com.yadar.app.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.yadar.app.YadarApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * با هر بار باز شدن قفل صفحه (کاربر واقعاً وارد گوشی می‌شود)، Selection Engine
 * برای همه Widgetها دوباره اجرا می‌شود. برای هر Widget طبق روش انتخاب خودش
 * رفتار می‌کند: در «با هر تغییر» و «تصادفی» همیشه یک جمله تازه انتخاب می‌شود؛
 * در «روزانه» تا وقتی روز عوض نشده همان جمله می‌ماند؛ در «ترتیبی» یک قدم جلو
 * می‌رود؛ در «تصادفی بدون تکرار» طبق تاریخچه پیش می‌رود.
 */
class ScreenUnlockReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_USER_PRESENT) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val container = (context.applicationContext as YadarApplication).container
                val now = LocalDateTime.now()
                val currentWidgets = container.widgetConfigRepository.observeAll().first()
                currentWidgets.forEach { config ->
                    container.refreshWidgetSentenceUseCase(config.widgetId, now)
                }
                YadarWidget.updateAll(context)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
