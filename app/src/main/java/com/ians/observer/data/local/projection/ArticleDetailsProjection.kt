package com.ians.observer.data.local.projection

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.ians.observer.data.local.entity.ArticleEntity

data class ArticleDetailsProjection(
    @Embedded
    val article: ArticleEntity,

    @ColumnInfo(name = "details_provider_id")
    val providerId: String,

    @ColumnInfo(name = "details_category")
    val category: String?,

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean,
)
