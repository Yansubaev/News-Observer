package com.ians.observer.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "remote_keys",
    primaryKeys = ["feed_key", "provider_id"],
)
data class RemoteKeyEntity(
    @ColumnInfo(name = "feed_key")
    val feedKey: String,

    @ColumnInfo(name = "provider_id")
    val providerId: String,

    @ColumnInfo(name = "next_page_token")
    val nextPageToken: String?,

    @ColumnInfo(name = "end_reached")
    val endReached: Boolean,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)