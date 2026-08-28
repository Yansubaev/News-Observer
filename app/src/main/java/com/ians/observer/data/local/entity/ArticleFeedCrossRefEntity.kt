package com.ians.observer.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "article_feed_cross_refs",
    primaryKeys = ["feed_key", "provider_id", "article_id"],
    foreignKeys = [
        ForeignKey(
            entity = ArticleEntity::class,
            parentColumns = ["id"],
            childColumns = ["article_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = FeedEntity::class,
            parentColumns = ["feed_key", "provider_id"],
            childColumns = ["feed_key", "provider_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index("article_id"),
        Index(value = ["feed_key", "provider_id"]),
        Index(value = ["feed_key", "provider_id", "position"], unique = true),
    ]
)
data class ArticleFeedCrossRefEntity(
    @ColumnInfo(name = "feed_key")
    val feedKey: String,

    @ColumnInfo(name = "provider_id")
    val providerId: String,

    @ColumnInfo(name = "article_id")
    val articleId: String,

    @ColumnInfo(name = "position")
    val position: Int
)
