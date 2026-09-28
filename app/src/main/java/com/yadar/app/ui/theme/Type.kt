package com.yadar.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.yadar.app.R
import com.yadar.app.domain.model.FontChoice

// از فایل‌های ttf منفرد با وزن صریح استفاده می‌کنیم (نه XML خانواده فونت) تا Compose
// به‌طور قطعی وزن‌ها را تطبیق بدهد. XML خانواده‌ها فقط برای Widget (Glance) لازم‌اند.
val VazirmatnFontFamily = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_bold, FontWeight.Bold)
)
val EstedadFontFamily = FontFamily(
    Font(R.font.estedad_regular, FontWeight.Normal),
    Font(R.font.estedad_medium, FontWeight.Medium),
    Font(R.font.estedad_bold, FontWeight.Bold)
)
val SahelFontFamily = FontFamily(
    Font(R.font.sahel_regular, FontWeight.Normal),
    Font(R.font.sahel_bold, FontWeight.Bold)
)
val ShabnamFontFamily = FontFamily(
    Font(R.font.shabnam_regular, FontWeight.Normal),
    Font(R.font.shabnam_bold, FontWeight.Bold)
)
val SamimFontFamily = FontFamily(
    Font(R.font.samim_regular, FontWeight.Normal),
    Font(R.font.samim_bold, FontWeight.Bold)
)
val LalezarFontFamily = FontFamily(Font(R.font.lalezar_regular, FontWeight.Normal))

/** فونت مناسب هر [FontChoice] برای استفاده در Preview داخل برنامه و در Widget واقعی (Glance). */
fun fontFamilyFor(choice: FontChoice): FontFamily = when (choice) {
    FontChoice.VAZIRMATN -> VazirmatnFontFamily
    FontChoice.ESTEDAD -> EstedadFontFamily
    FontChoice.SAHEL -> SahelFontFamily
    FontChoice.SHABNAM -> ShabnamFontFamily
    FontChoice.SAMIM -> SamimFontFamily
    FontChoice.LALEZAR -> LalezarFontFamily
}

/** UI اصلی برنامه همیشه با Vazirmatn نمایش داده می‌شود؛ انتخاب فونت فقط برای Widget است. */
val YadarTypography = Typography(
    bodyLarge = TextStyle(fontFamily = VazirmatnFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = VazirmatnFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    titleLarge = TextStyle(fontFamily = VazirmatnFontFamily, fontWeight = FontWeight.Medium, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = VazirmatnFontFamily, fontWeight = FontWeight.Medium, fontSize = 18.sp),
    labelLarge = TextStyle(fontFamily = VazirmatnFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp)
)
