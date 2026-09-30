package com.yadar.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * یک جمله‌ای که کاربر خودش وارد کرده است (بند ۸ و ۳۴ سند پروژه).
 *
 * [collectionId] == null یعنی جمله در «بدون مجموعه» قرار دارد.
 * [manualSortOrder] فقط زمانی معنا دارد که کاربر مرتب‌سازی «دستی» را انتخاب کرده باشد.
 * [lastShownAt] برای مرتب‌سازی «آخرین نمایش» و همچنین Selection Engine استفاده می‌شود.
 */
@Entity(
    tableName = "sentences",
    foreignKeys = [
        ForeignKey(
            entity = CollectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("collectionId"), Index("isActive")]
)
data class SentenceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val normalizedText: String,
    /** معنی/ترجمه اختیاری (مثلاً برای آیه‌های قرآن) - بند درخواست کاربر. */
    val meaning: String? = null,
    val collectionId: Long? = null,
    val isActive: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long,
    val manualSortOrder: Long = 0,
    val lastShownAt: Long? = null
)
