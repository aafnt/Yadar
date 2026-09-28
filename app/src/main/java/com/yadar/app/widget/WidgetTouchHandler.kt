package com.yadar.app.widget

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.yadar.app.YadarApplication
import com.yadar.app.ui.MainActivity

/** منطق مشترک عملکردهای لمس Widget (بند ۲۰ سند پروژه). */
object WidgetTouchHandler {

    suspend fun handleTouch(context: Context, glanceId: GlanceId) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(glanceId)
        val container = (context.applicationContext as YadarApplication).container
        container.handleWidgetTouchUseCase(appWidgetId)
        YadarWidget.updateOne(context, appWidgetId)
    }

    fun openApp(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        context.startActivity(intent)
    }
}
