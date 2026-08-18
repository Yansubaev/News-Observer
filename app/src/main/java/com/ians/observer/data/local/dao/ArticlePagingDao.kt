package com.ians.observer.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import com.ians.observer.data.local.projection.FeedArticleProjection

@Dao
interface ArticlePagingDao {

    @Query(
        """
        SELECT 
            articles.*,
            refs.provider_id AS provider_id,
            feeds.category AS feed_category,
            CASE
                WHEN favorites.article_url IS NULL THEN 0
                ELSE 1
            END AS is_favorite
        FROM articles
        INNER JOIN article_feed_cross_refs AS refs
            ON refs.article_url = articles.url
        INNER JOIN feeds
            ON feeds.feed_key = refs.feed_key
            AND feeds.provider_id = refs.provider_id
        LEFT JOIN favorites
            ON favorites.article_url = articles.url
        WHERE refs.feed_key = :feedKey
            AND refs.provider_id = :providerId
        ORDER BY refs.position
            
    """
    )
    fun pagingSource(
        feedKey: String,
        providerId: String
    ): PagingSource<Int, FeedArticleProjection>

}