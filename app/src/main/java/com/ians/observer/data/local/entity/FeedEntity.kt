package com.ians.observer.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "feeds",
    primaryKeys = ["feed_key", "provider_id"]
)
data class FeedEntity(
    @ColumnInfo(name = "feed_key")
    val feedKey: String,

    @ColumnInfo(name = "provider_id")
    val providerId: String,

    @ColumnInfo(name = "country")
    val country: String?,

    @ColumnInfo(name = "language")
    val language: String?,

    @ColumnInfo(name = "category")
    val category: String?
)