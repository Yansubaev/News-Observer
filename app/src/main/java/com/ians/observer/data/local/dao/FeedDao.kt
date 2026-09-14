package com.ians.observer.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ians.observer.data.local.entity.FeedEntity

@Dao
interface FeedDao {

    @Upsert
    suspend fun upsert(feed: FeedEntity)

    @Query("DELETE FROM feeds")
    suspend fun deleteAllFeeds()

    // Cross-refs of deleted feeds are removed by the foreign key cascade.
    @Query(
        """
        DELETE FROM feeds
        WHERE EXISTS(
            SELECT 1 FROM remote_keys
            WHERE remote_keys.feed_key = feeds.feed_key
            AND remote_keys.provider_id = feeds.provider_id
            AND remote_keys.updated_at < :threshold
        )
        """
    )
    suspend fun deleteFeedsUpdatedBefore(threshold: Long)
}