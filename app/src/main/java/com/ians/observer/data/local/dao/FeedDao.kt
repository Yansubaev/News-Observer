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
}