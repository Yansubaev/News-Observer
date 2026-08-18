package com.ians.observer.data.local.projection

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.ians.observer.data.local.entity.ArticleEntity

data class FeedArticleProjection(
    @Embedded
    val article: ArticleEntity,

    @ColumnInfo(name = "provider_id")
    val providerId: String,

    @ColumnInfo(name = "feed_category")
    val category: String?,

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean
)

