package com.ians.observer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ians.observer.data.local.entity.ArticleFeedCrossRefEntity

@Dao
interface ArticleFeedCrossRefDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(refs: List<ArticleFeedCrossRefEntity>)

    @Query(
        """
            DELETE FROM article_feed_cross_refs
            WHERE feed_key = :feedKey AND provider_id = :providerId
        """
    )
    suspend fun deleteByFeed(
        feedKey: String,
        providerId: String
    )

    @Query(
        """
            SELECT COALESCE(MAX(position), -1)
            FROM article_feed_cross_refs
            WHERE feed_key = :feedKey AND provider_id = :providerId
        """
    )
    suspend fun getMaxPosition(
        feedKey: String,
        providerId: String
    ): Int
}