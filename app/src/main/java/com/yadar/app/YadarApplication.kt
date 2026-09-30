package com.yadar.app

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import com.yadar.app.di.AppContainer
import com.yadar.app.widget.ScreenUnlockReceiver

class YadarApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer.getInstance(this)

        // ACTION_USER_PRESENT (باز شدن قفل صفحه) جزو Implicit Broadcastهایی است که از
        // اندروید ۸ به بعد دیگر با AndroidManifest قابل ثبت نیست؛ فقط با ثبت در کد
        // (در طول عمر Process برنامه) قابل دریافت است. همین Receiver پشت تنظیم
        // «روش انتخاب: با هر تغییر» است.
        //
        // از ContextCompat.registerReceiver با RECEIVER_NOT_EXPORTED استفاده می‌شود، نه
        // registerReceiver خام: از اندروید ۱۳ (API 33) به بعد، برای targetSdk 33+، فراخوانی
        // registerReceiver بدون مشخص کردن صریح Exported/NotExported یک SecurityException در
        // زمان اجرا پرتاب می‌کند و کل برنامه از همان لحظه شروع Crash می‌کند. چون این پیام
        // فقط از خود سیستم‌عامل می‌آید (نه از اپ دیگری)، NOT_EXPORTED درست است.
        ContextCompat.registerReceiver(
            this,
            ScreenUnlockReceiver(),
            IntentFilter(Intent.ACTION_USER_PRESENT),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }
}
