package com.ians.observer.data.local.dao

import androidx.paging.PagingSource
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Query
import com.ians.observer.data.local.entity.ArticleEntity

@Dao
interface ArticlePagingDao {

    @Query(
        """
        SELECT * FROM articles
        WHERE (:showFavoritesOnly = 0 OR is_favorite = 1)
        AND (:category IS NULL OR category = :category)
        ORDER BY published_at DESC
    """
    )
    fun pagingSource(showFavoritesOnly: Int = 0, category: String? = null): PagingSource<Int, ArticleEntity>

}