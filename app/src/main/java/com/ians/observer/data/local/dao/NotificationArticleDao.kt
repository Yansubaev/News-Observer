package com.ians.observer.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ians.observer.data.local.entity.NotificationArticleEntity

@Dao
interface NotificationArticleDao {

    @Upsert
    suspend fun upsert(notificationArticle: NotificationArticleEntity)

    @Query(
        """
        SELECT article_id
        FROM notification_articles
        WHERE notification_key = :notificationKey
        """
    )
    suspend fun getArticleId(notificationKey: String): String?
}
