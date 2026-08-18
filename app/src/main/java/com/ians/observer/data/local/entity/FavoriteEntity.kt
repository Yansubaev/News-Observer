package com.ians.observer.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "favorites",
    foreignKeys = [ForeignKey(
        entity = ArticleEntity::class,
        parentColumns = ["url"],
        childColumns = ["article_url"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("article_url")]
)
data class FavoriteEntity(
    @PrimaryKey
    @ColumnInfo(name = "article_url")
    val articleUrl: String,

    @ColumnInfo(name = "saved_at")
    val savedAt: Long,

    @ColumnInfo(name = "category")
    val category: String?,

    @ColumnInfo("source_provider_id")
    val sourceProviderId: String
)
