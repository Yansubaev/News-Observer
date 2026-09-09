package com.ians.observer.data.local

import androidx.room.withTransaction
import com.ians.observer.data.local.dao.NewsDatabase
import com.ians.observer.data.local.entity.ArticleEntity
import com.ians.observer.data.local.entity.NotificationArticleEntity

suspend fun NewsDatabase.replaceNotificationArticle(article: ArticleEntity) {
    withTransaction {
        val previousArticleId = notificationArticleDao()
            .getArticleId(NotificationArticleEntity.DAILY_NEWS_KEY)

        articleDao().upsertArticle(article)
        notificationArticleDao().upsert(
            NotificationArticleEntity(
                notificationKey = NotificationArticleEntity.DAILY_NEWS_KEY,
                articleId = article.id,
            )
        )

        if (previousArticleId != null && previousArticleId != article.id) {
            articleDao().deleteArticleIfOrphan(previousArticleId)
        }
    }
}
