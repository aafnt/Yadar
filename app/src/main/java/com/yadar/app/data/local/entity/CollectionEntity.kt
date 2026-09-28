package com.yadar.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** مجموعه‌ای از جمله‌ها، مثل «دعاها» یا «مولانا» (بند ۱۰ و ۳۴ سند پروژه). */
@Entity(tableName = "collections")
data class CollectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long
)
