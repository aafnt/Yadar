package com.yadar.app.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.yadar.app.YadarApplication
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * برنامه‌ریز Refresh مبتنی بر Alarm (بند ۲۵، ۲۶، ۲۷ سند پروژه).
 *
 * به‌جای Timer دائمی یا Polling، فقط یک Alarm دقیق برای «نزدیک‌ترین لحظه‌ای که
 * ممکن است واجد شرایط بودن یک جمله عوض شود» تنظیم می‌شود: شروع/پایان بازه
 * زمانی جمله‌های فعال، یا نیمه‌شب بعدی (برای رول‌آور روزانه). بعد از هر بار
 * اجرا، خودش Alarm بعدی را دوباره زمان‌بندی می‌کند (Chained Alarm) به‌جای یک
 * Alarm تکرارشونده ثابت که به تغییرات داده حساس نیست.
 */
class AlarmRefreshScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    suspend fun scheduleNext() {
        val container = (context.applicationContext as YadarApplication).container
        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        val nowMinute = now.hour * 60 + now.minute

        val boundaryMinutesToday = mutableListOf<Int>()
        val activeSentences = container.sentenceRepository.getActiveEligiblePool(null)
        activeSentences.forEach { sentence ->
            val rule = sentence.displayRule
            rule.timeStartMinute?.let { if (it > nowMinute) boundaryMinutesToday.add(it) }
            rule.timeEndMinute?.let {
                // یک دقیقه بعد از پایان بازه، چون تا همان دقیقه پایان هنوز واجد شرایط است.
                val boundary = (it + 1) % 1440
                if (boundary > nowMinute && boundary != 0) boundaryMinutesToday.add(boundary)
            }
        }

        val nextTriggerTime: LocalDateTime = if (boundaryMinutesToday.isNotEmpty()) {
            val nextMinute = boundaryMinutesToday.min()
            LocalDateTime.of(today, LocalTime.of(nextMinute / 60, nextMinute % 60))
        } else {
            // اگر امروز مرز دیگری نمانده، نیمه‌شب بعدی (برای رول‌آور Daily) کافی است.
            LocalDateTime.of(today.plusDays(1), LocalTime.MIDNIGHT)
        }

        scheduleAt(nextTriggerTime)
    }

    private fun scheduleAt(dateTime: LocalDateTime) {
        val triggerAtMillis = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val pendingIntent = alarmPendingIntent()
        // عمداً از Alarm دقیق (setExactAndAllowWhileIdle) استفاده نمی‌شود چون نیاز به مجوز
        // ویژه «Alarms & reminders» دارد که برای یک اپ نمایش جمله توجیه‌پذیر نیست؛ چند دقیقه
        // تأخیر احتمالی در تعویض جمله زمان‌بندی‌شده قابل قبول است (بند ۲۶ و ۲۷: اولویت با
        // مصرف باتری و کمترین مجوز است، نه دقت لحظه‌ای).
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
    }

    private fun alarmPendingIntent(): PendingIntent {
        val intent = Intent(context, WidgetRefreshAlarmReceiver::class.java)
        return PendingIntent.getBroadcast(
            context, ALARM_REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun cancel() {
        alarmManager.cancel(alarmPendingIntent())
    }

    companion object {
        private const val ALARM_REQUEST_CODE = 4210
    }
}
