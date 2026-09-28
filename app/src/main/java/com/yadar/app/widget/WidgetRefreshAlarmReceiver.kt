package com.yadar.app.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WidgetRefreshAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                YadarWidget.updateAll(context)
                AlarmRefreshScheduler(context).scheduleNext()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
