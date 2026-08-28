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
        parentColumns = ["id"],
        childColumns = ["article_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("article_id")]
)
data class FavoriteEntity(
    @PrimaryKey
    @ColumnInfo(name = "article_id")
    val articleId: String,

    @ColumnInfo(name = "saved_at")
    val savedAt: Long,

    @ColumnInfo(name = "category")
    val category: String?,

    @ColumnInfo("source_provider_id")
    val sourceProviderId: String
)
