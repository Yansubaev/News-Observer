package com.ians.observer.data.local.projection

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.ians.observer.data.local.entity.ArticleEntity

data class FavoriteArticleProjection(
    @Embedded
    val article: ArticleEntity,

    @ColumnInfo(name = "provider_id")
    val providerId: String,

    @ColumnInfo(name = "favorite_category")
    val category: String?,
)