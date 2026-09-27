package com.yadar.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * تاریخچه نمایش هر جمله روی هر Widget (بند ۳۵ سند پروژه).
 * فقط برای حالت RANDOM_WITHOUT_REPEAT استفاده می‌شود تا بداند کدام جمله‌ها
 * از «دور فعلی» قبلاً نمایش داده شده‌اند. با «بازنشانی ترتیب جمله‌ها» یا با
 * تمام‌شدن یک دور کامل، رکوردهای مربوط به آن Widget پاک می‌شوند.
 */
@Entity(
    tableName = "selection_history",
    indices = [Index("widgetId"), Index("sentenceId")]
)
data class SelectionHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sentenceId: Long,
    val widgetId: Int,
    val shownAt: Long
)
