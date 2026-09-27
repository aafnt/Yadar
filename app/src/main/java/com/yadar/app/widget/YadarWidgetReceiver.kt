package com.yadar.app.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.yadar.app.YadarApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class YadarWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = YadarWidget()

    /**
     * وقتی کاربر یک Instance از Widget را از صفحه اصلی حذف می‌کند، تنظیمات و
     * تاریخچه مخصوص همان Widget هم پاک می‌شود تا داده یتیم در Room نماند
     * (بند ۲۱ و ۳۵ سند پروژه: هر Widget مستقل است، هم در ساخت و هم در حذف).
     */
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        val container = (context.applicationContext as YadarApplication).container
        CoroutineScope(Dispatchers.IO).launch {
            appWidgetIds.forEach { id -> container.deleteWidgetConfigUseCase(id) }
        }
    }
}
