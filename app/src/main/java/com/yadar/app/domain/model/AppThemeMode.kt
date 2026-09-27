package com.yadar.app.domain.model

/** پوسته برنامه (بند ۳۲ سند پروژه). این تنظیم فقط روی UI برنامه اثر دارد، نه Widget. */
enum class AppThemeMode { LIGHT, DARK, SYSTEM }

/** تنظیمات عمومی برنامه که در DataStore نگه‌داری می‌شوند (نه Room، چون Key-Value ساده هستند). */
data class AppSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val accentColorArgb: Int = 0xFF4A7C7C.toInt()
)
