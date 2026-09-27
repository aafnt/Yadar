# Room
-keep class com.yadar.app.data.local.entity.** { *; }

# Glance
-keep class androidx.glance.appwidget.** { *; }
-keep class com.yadar.app.widget.** { *; }

# Keep line numbers for crash reports in debug builds while still shrinking release
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
