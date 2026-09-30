# یادآر (Yadar)

اپلیکیشن اندروید شخصی و کاملاً Local-First برای نمایش جمله‌های خودت — دعا، شعر،
یادداشت، هر چیزی که دوست داری — روی یک Widget مینیمال و شفاف، درست روی
Wallpaper گوشی‌ات.

> «کاربر چیزی را به یادآر می‌سپارد تا در زمان مناسب آن را جلوی چشمش بیاورد.»
> **کمترین دخالت، بیشترین حضور.**

---

## امکانات

- کاملاً آفلاین، بدون Login، بدون Backend، بدون تبلیغ، بدون Analytics.
- مدیریت جمله‌ها و مجموعه‌ها (دعاها، مولانا، یادآوری، ...).
- موتور انتخاب جمله (Selection Engine) با ۵ روش: روزانه، با هر تغییر، ترتیبی،
  تصادفی، تصادفی بدون تکرار.
- قوانین زمانی برای هر جمله: روزهای هفته، بازه ساعتی (با پشتیبانی از بازه‌های
  عبوری از نیمه‌شب)، بازه تاریخ شمسی.
- Widget با Jetpack Glance، در سه اندازه (Small/Medium/Large)، پیش‌فرض کاملاً
  شفاف، بدون Card/Background/Icon اجباری.
- هر Widget تنظیمات کاملاً مستقل خودش را دارد (فونت، رنگ، اندازه، پس‌زمینه،
  عملکرد لمس، ...).
- تقویم شمسی مرکزی (`PersianDate`) با الگوریتم دقیق ریاضی (نه جدول lookup).
- Export/Import کامل داده‌ها به یک فایل JSON.
- پشتیبانی کامل از Dark/Light/System Theme.
- شش فونت فارسی داخلی: Vazirmatn، Estedad، Sahel، Shabnam، Samim، Lalezar.

---

## معماری

Clean Architecture + MVVM، بدون Hilt/Dagger (یک Container دستی ساده در
`di/AppContainer.kt` تا پروژه برای این حجم over-engineered نشود):

```
ui/          Compose + ViewModel (Home, Sentences, Collections, Schedule, Settings)
domain/      Model, Repository interface، Selector (Selection Engine)، UseCase
data/        Room (Entity/DAO/Database)، DataStore، Repository impl، Backup
widget/      Glance AppWidget، Widget Config Activity، Alarm Scheduler
util/        PersianDigits، PersianDate، TextNormalizer
```

`UI ← ViewModel ← UseCase ← Repository ← DAO ← Room`. هیچ UI مستقیماً منطق
انتخاب جمله را پیاده‌سازی نمی‌کند؛ Widget هم از همان UseCaseها استفاده می‌کند
که صفحات برنامه استفاده می‌کنند (`RefreshWidgetSentenceUseCase`,
`HandleWidgetTouchUseCase`).

---

## فونت‌های استفاده‌شده — **قدم ضروری قبل از Build**

فایل‌های XML مربوط به هر فونت در `app/src/main/res/font/*.xml` از قبل آماده‌اند
اما **فایل باینری `.ttf` هیچ فونتی داخل پروژه نیست** (محیطی که این پروژه در آن
ساخته شد به اینترنت دسترسی نداشت). قبل از Build باید این فایل‌ها را دانلود و
داخل همان پوشه `app/src/main/res/font/` با همین نام‌ها قرار بدهی:

| فونت | فایل‌های لازم | مجوز | منبع رسمی |
|---|---|---|---|
| Vazirmatn | `vazirmatn_regular.ttf`, `vazirmatn_medium.ttf`, `vazirmatn_bold.ttf` | SIL OFL 1.1 | github.com/rastikerdar/vazirmatn |
| Estedad | `estedad_regular.ttf`, `estedad_medium.ttf`, `estedad_bold.ttf` | SIL OFL 1.1 | github.com/aminabedi68/Estedad |
| Sahel | `sahel_regular.ttf`, `sahel_bold.ttf` | SIL OFL 1.1 | github.com/rastikerdar/sahel-font |
| Shabnam | `shabnam_regular.ttf`, `shabnam_bold.ttf` | بررسی کن (پروژه متوقف‌شده) | github.com/rastikerdar/shabnam-font |
| Samim | `samim_regular.ttf`, `samim_bold.ttf` | SIL OFL 1.1 | github.com/rastikerdar/samim-font |
| Lalezar | `lalezar_regular.ttf` | SIL OFL 1.1 | github.com/rastikerdar/lalezar-font |

**فونت هفتم، «Persian Sols»**، فایلش را خودت داده‌ای و از قبل داخل
`res/font/persian_sols_regular.ttf` قرار دارد — نیازی به دانلود جداگانه ندارد.
فقط یک وزن (Regular) دارد و برای خط‌های تزئینی/کوتاه مناسب است، نه متن طولانی.
پیش از هر انتشار عمومی (نه فقط تست شخصی)، مجوز توزیع این فونت خاص را با
سازنده‌اش بررسی کن؛ منشأ و مجوز آن مثل ۶ فونت بالا مستند نیست.

فایل مجوز (LICENSE) هر فونت را هم کنار فایل‌های آن (یا در پوشه `fonts_licenses/`
در ریشه پروژه) نگه دار، طبق بند ۴۹ سند پروژه.

CI (`.github/workflows/build.yml`) وجود این فایل‌ها را قبل از Build بررسی
می‌کند و اگر چیزی کم باشد، Build با پیام روشن متوقف می‌شود.

---

## Build

> **نکته مهم:** فایل باینری `gradle/wrapper/gradle-wrapper.jar` عمداً داخل این
> ریپو نیست، چون محیطی که این پروژه در آن ساخته شد به اینترنت دسترسی نداشت و
> این فایل را نمی‌شد تولید کرد. این روی Build گیت‌هاب اثری ندارد (پایین را
> ببین)، اما برای Build محلی باید یک‌بار آن را بسازی.

### روی GitHub (روش پیشنهادی، بدون نیاز به Android Studio)

1. این ریپو را در GitHub بساز و Push کن (بعد از اضافه‌کردن فونت‌ها).
2. تب **Actions** را باز کن؛ Workflow «Build APK» خودکار اجرا می‌شود — این
   Workflow خودش Gradle 8.9 را مستقیم نصب می‌کند و به `gradle-wrapper.jar`
   نیازی ندارد.
3. بعد از پایان، از بخش **Artifacts** همان اجرا، فایل `yadar-debug-apk` را
   دانلود کن؛ داخلش `app-debug.apk` است.

### روی Android Studio

ساده‌ترین راه: پروژه را در Android Studio باز کن؛ خودش تشخیص می‌دهد Wrapper
جا افتاده و پیشنهاد می‌دهد با نسخه Gradle داخلی خودش Sync و آن را بسازد.

یا اگر از خط فرمان با یک نصب محلی Gradle استفاده می‌کنی:
```
gradle wrapper --gradle-version 8.9   # فقط یک‌بار، gradlew را می‌سازد
./gradlew assembleDebug
```
APK در `app/build/outputs/apk/debug/` قرار می‌گیرد.

---

## تست‌ها

```
./gradlew test                    # تست‌های واحد JVM (Selection Engine، Rule Evaluator، PersianDate، Backup)
./gradlew connectedAndroidTest    # تست Instrumented (Round-trip کامل Backup با Room واقعی)
```

پوشش تست‌ها طبق بند ۵۰ و ۵۱ سند پروژه:
- `SelectionEngineTest`: سناریوهای ۱ تا ۴ و ۵۱ (Daily پایدار در همان روز، تغییر
  در روز جدید، Random Without Repeat بدون تکرار در یک دور کامل، استقلال دو
  Widget، Sequential با Wrap-around).
- `RuleEvaluatorTest`: سناریوهای ۶ و ۷ (فیلتر روز هفته، فیلتر ساعت، بازه عبوری
  از نیمه‌شب، ترکیب چند قانون).
- `PersianDateTest`: تبدیل رفت‌وبرگشت میلادی↔شمسی روی بازه وسیع، به‌علاوه دو
  مقدار مرجع شناخته‌شده (Epoch یونیکس، نوروز ۱۴۰۵).
- `BackupRepositoryImplTest` + `BackupRepositoryInstrumentedTest`: JSON خراب،
  Version ناشناخته، و یک Round-trip کامل و موفق با دیتابیس واقعی.

---

## تصمیم‌های فنی مستندشده (بند ۵۸ سند پروژه)

چند جای سند پروژه دقیقاً یک ساختار داده را مشخص نکرده بود؛ این تصمیم‌ها گرفته
شد و همین‌جا مستند می‌شود:

1. **«روش انتخاب» در برابر «روش نمایش»**: الگوریتم‌های Sequential/Random/
   Random-Without-Repeat/Daily (بند ۱۲) یک تنظیم سطح **Widget** هستند (چون باید
   بین چند جمله تصمیم بگیرند)، نه سطح تک‌جمله. «روش نمایش» انتخاب‌شده هنگام
   افزودن جمله (بند ۹) فقط برچسبی است که تعیین می‌کند کدام فیلدهای ورودی
   (روز هفته/ساعت/تاریخ) در فرم نشان داده شوند؛ مقادیر واقعی این فیلدها
   (`DisplayRule`) مستقل از این برچسب ذخیره و بررسی می‌شوند، پس حالت «ترکیبی»
   به‌طور طبیعی توسط هم‌زمان مقداردهی چند فیلد پشتیبانی می‌شود.
2. **تب «برنامه نمایش»**: محتوای آن را «مدیریت Widget» (بند ۲۲) قرار دادیم،
   چون دقیقاً همان چیزی است که این تب باید نشان دهد.
3. **ذخیره تاریخ**: همه‌جا `LocalDate.toEpochDay()` (عدد صحیح روز، بدون Time
   Zone) به‌جای رشته یا Timestamp، تا تغییر منطقه زمانی هرگز روز را جابه‌جا نکند.
4. **JSON Backup**: به‌جای افزودن `kotlinx.serialization` (و پلاگین آن) فقط
   برای Export/Import، از `org.json` استفاده شد که از قبل بخشی از Android SDK
   است.
5. **Alarm Refresh**: به‌جای `setExactAndAllowWhileIdle` (که نیاز به مجوز ویژه
   «Alarms & reminders» دارد)، از `setAndAllowWhileIdle` استفاده شد؛ چند دقیقه
   تأخیر احتمالی در تعویض جمله‌های زمان‌بندی‌شده قابل قبول است و اولویت با
   حداقل مجوز و مصرف باتری است (بند ۲۶ و ۲۷).
6. **Color Picker و انتخاب تاریخ**: به‌جای ساخت یک HSV Color Wheel یا تقویم
   شمسی گرافیکی از صفر، از ترکیب «رنگ‌های پیشنهادی + ورودی Hex» و «ورودی متنی
   با فرمت ۱۴۰۵/۰۷/۰۱ که با `PersianDate.parseJalaliString` اعتبارسنجی می‌شود»
   استفاده شد. این‌ها کاملاً کاربردی‌اند؛ اگر بعداً یک Picker گرافیکی‌تر
   خواستی، فقط این دو Composable نیاز به جایگزینی دارند.
7. **فونت سفارشی در Glance**: `androidx.glance.text.FontFamily(name: String)`
   با نام دقیق فایل XML فونت (بدون پسوند) فراخوانی شده تا Glance آن را در زمان
   Render از `res/font` پیدا کند.

---

## نحوه Backup

**تنظیمات ← پشتیبان‌گیری ← خروجی گرفتن**: یک فایل `yadar_backup.json` شامل تمام
جمله‌ها، مجموعه‌ها و قوانین زمانی (نه تنظیمات ظاهری Widgetها، که مخصوص همان
دستگاه هستند) ذخیره می‌شود.

**تنظیمات ← پشتیبان‌گیری ← بازیابی**: فایل JSON را انتخاب کن؛ اگر داده فعلی
وجود داشته باشد، بین «جایگزینی» (پاک‌کردن همه و بازیابی از فایل) یا «اضافه
کردن» (Merge با شناسه‌های تازه) یکی را انتخاب می‌کنی.

---

## نصب

نصب مستقیم از APK Build‌شده (خارج از Google Play):
1. تنظیمات گوشی → نصب از منابع ناشناس را برای مرورگر/فایل‌منیجر فعال کن.
2. APK دانلودشده از GitHub Actions Artifact را باز و نصب کن.
3. جمله اول را اضافه کن، یک Widget به صفحه اصلی اضافه کن، مجموعه و ظاهرش را
   تنظیم کن — تمام.

---

## License

فایل‌های کد این پروژه تحت [LICENSE](LICENSE) (MIT) منتشر می‌شوند. فونت‌های
داخل `res/font` هرکدام مجوز مستقل خودشان (عمدتاً SIL OFL 1.1) را دارند؛ فایل
مجوز هر فونت را کنار خودش نگه دار.
