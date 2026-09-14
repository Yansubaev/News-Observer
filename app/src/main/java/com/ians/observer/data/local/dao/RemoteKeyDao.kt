package com.ians.observer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ians.observer.data.local.entity.RemoteKeyEntity

@Dao
interface RemoteKeyDao {

    @Query(
        """
        SELECT * FROM remote_keys
            WHERE feed_key = :feedKey
            AND provider_id = :providerId
        """
    )
    suspend fun get(
        feedKey: String,
        providerId: String,
    ): RemoteKeyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(remoteKey: RemoteKeyEntity)

    @Query("DELETE FROM remote_keys WHERE feed_key = :feedKey")
    suspend fun deleteByFeed(feedKey: String)

    @Query("DELETE FROM remote_keys")
    suspend fun deleteAllRemoteKeys()

    @Query("DELETE FROM remote_keys WHERE updated_at < :threshold")
    suspend fun deleteUpdatedBefore(threshold: Long)
}