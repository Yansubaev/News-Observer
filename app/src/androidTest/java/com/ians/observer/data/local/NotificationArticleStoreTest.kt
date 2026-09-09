package com.ians.observer.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ians.observer.data.local.dao.NewsDatabase
import com.ians.observer.data.local.entity.ArticleEntity
import com.ians.observer.data.local.entity.NotificationArticleEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationArticleStoreTest {

    private lateinit var database: NewsDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, NewsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun notificationArticleSurvivesOrphanCleanup() = runTest {
        val article = articleEntity("current")

        database.replaceNotificationArticle(article)
        database.articleDao().deleteOrphanedArticles()

        assertNotNull(database.articleDao().getArticleById(article.id).first())
        assertEquals(
            article.id,
            database.notificationArticleDao()
                .getArticleId(NotificationArticleEntity.DAILY_NEWS_KEY),
        )
    }

    @Test
    fun replacingNotificationArticleDeletesPreviousOrphan() = runTest {
        val previousArticle = articleEntity("previous")
        val currentArticle = articleEntity("current")

        database.replaceNotificationArticle(previousArticle)
        database.replaceNotificationArticle(currentArticle)

        assertNull(database.articleDao().getArticleById(previousArticle.id).first())
        assertNotNull(database.articleDao().getArticleById(currentArticle.id).first())
        assertEquals(
            currentArticle.id,
            database.notificationArticleDao()
                .getArticleId(NotificationArticleEntity.DAILY_NEWS_KEY),
        )
    }

    @Test
    fun replacingNotificationArticleKeepsPreviousFavorite() = runTest {
        val favoriteArticle = articleEntity("favorite")
        val currentArticle = articleEntity("current")
        database.replaceNotificationArticle(favoriteArticle)
        database.articleDao().addToFavorites(
            article = favoriteArticle,
            category = "general",
            providerId = favoriteArticle.providerId,
        )

        database.replaceNotificationArticle(currentArticle)

        assertNotNull(database.articleDao().getArticleById(favoriteArticle.id).first())
        assertNotNull(database.articleDao().getArticleById(currentArticle.id).first())
    }

    @Test
    fun replacingNotificationArticleDoesNotDeleteUnrelatedOrphans() = runTest {
        val previousArticle = articleEntity("previous")
        val currentArticle = articleEntity("current")
        val unrelatedArticle = articleEntity("unrelated")
        database.replaceNotificationArticle(previousArticle)
        database.articleDao().upsertArticle(unrelatedArticle)

        database.replaceNotificationArticle(currentArticle)

        assertNull(database.articleDao().getArticleById(previousArticle.id).first())
        assertNotNull(database.articleDao().getArticleById(currentArticle.id).first())
        assertNotNull(database.articleDao().getArticleById(unrelatedArticle.id).first())
    }

    private fun articleEntity(id: String) = ArticleEntity(
        id = id,
        url = "https://example.com/$id",
        providerId = "news-data",
        category = "general",
        publisherId = "publisher-id",
        publisherName = "Publisher",
        publisherWebsiteUrl = "https://example.com",
        authors = setOf("Author"),
        title = "Article $id",
        description = "Description",
        imageUrl = null,
        publishedAt = 1_000L,
    )
}
