package com.yadar.app.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
    version = 1,
    exportSchema = true
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

        /**
         * برنامه کاملاً Local-First است؛ هیچ Migration به سرویس ابری وصل نیست.
         * از آنجا که این نسخه اول (version = 1) است، هنوز Migration واقعی لازم
         * نیست؛ برای نسخه‌های بعدی باید Migration صریح نوشته شود و از
         * fallbackToDestructiveMigration خودداری شود تا داده کاربر حفظ گردد.
         */
        fun getInstance(context: Context): YadarDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    YadarDatabase::class.java,
                    DB_NAME
                ).build().also { instance = it }
            }
        }
    }
}
