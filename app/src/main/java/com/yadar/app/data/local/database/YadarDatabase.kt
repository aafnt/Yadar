package com.yadar.app.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration
import com.yadar.app.data.local.dao.CollectionDao
import com.yadar.app.data.local.dao.DisplayRuleDao
import com.yadar.app.data.local.dao.SelectionHistoryDao
import com.yadar.app.data.local.dao.SentenceDao
import com.yadar.app.data.local.dao.WidgetConfigDao
import com.yadar.app.data.local.entity.CollectionEntity
import com.yadar.app.data.local.entity.DisplayRuleEntity
import com.yadar.app.data.local.entity.SelectionHistoryEntity
import com.yadar.app.data.local.entity.SentenceEntity
import com.yadar.app.data.local.entity.WidgetConfigEntity

@Database(
    entities = [
        SentenceEntity::class,
        CollectionEntity::class,
        DisplayRuleEntity::class,
        WidgetConfigEntity::class,
        SelectionHistoryEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class YadarDatabase : RoomDatabase() {

    abstract fun sentenceDao(): SentenceDao
    abstract fun collectionDao(): CollectionDao
    abstract fun displayRuleDao(): DisplayRuleDao
    abstract fun widgetConfigDao(): WidgetConfigDao
    abstract fun selectionHistoryDao(): SelectionHistoryDao

    companion object {
        private const val DB_NAME = "yadar.db"

        @Volatile
        private var instance: YadarDatabase? = null

        /** بند ۲۹ ویژگی «معنی جمله»: افزودن ستون‌های جدید بدون حذف داده کاربر موجود. */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE sentences ADD COLUMN meaning TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE widget_configs ADD COLUMN showMeaning INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE widget_configs ADD COLUMN meaningFont TEXT NOT NULL DEFAULT 'VAZIRMATN'")
                db.execSQL("ALTER TABLE widget_configs ADD COLUMN meaningFontSizeSp INTEGER NOT NULL DEFAULT 14")
            }
        }

        /**
         * برنامه کاملاً Local-First است؛ هیچ Migration به سرویس ابری وصل نیست.
         * از fallbackToDestructiveMigration عمداً استفاده نمی‌شود تا با هر تغییر
         * ساختار دیتابیس، جمله‌ها و مجموعه‌های کاربر پاک نشوند؛ هر نسخه جدید باید
         * Migration صریح خودش را اینجا اضافه کند.
         */
        fun getInstance(context: Context): YadarDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    YadarDatabase::class.java,
                    DB_NAME
                )
                    .addMigrations(MIGRATION_1_2)
                    .build().also { instance = it }
            }
        }
    }
}
