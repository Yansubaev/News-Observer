package com.ians.observer.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ians.observer.data.local.dao.NewsDatabase
import com.ians.observer.data.local.entity.ArticleEntity
import com.ians.observer.data.local.entity.ArticleFeedCrossRefEntity
import com.ians.observer.data.local.entity.FeedEntity
import com.ians.observer.data.local.entity.RemoteKeyEntity
import com.ians.observer.data.paging.ArticleRemoteMediatorFactory
import com.ians.observer.data.remote.provider.NewsProviderRegistry
import com.ians.observer.data.repository.ArticleRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StaleArticleCleanupTest {

    private lateinit var database: NewsDatabase
    private lateinit var repository: ArticleRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, NewsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ArticleRepositoryImpl(
            newsProviderRegistry = NewsProviderRegistry(emptyMap()),
            database = database,
            remoteMediatorFactory = ArticleRemoteMediatorFactory(database),
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun deletesOnlyArticlesOfStaleFeedsAndKeepsFavorites() = runTest {
        val stale = articleEntity("stale")
        val staleFavorite = articleEntity("stale-favorite")
        val fresh = articleEntity("fresh")
        insertFeed("old-feed", updatedAt = 100L, stale, staleFavorite)
        insertFeed("new-feed", updatedAt = 300L, fresh)
        database.articleDao().addToFavorites(staleFavorite, "general", staleFavorite.providerId)

        repository.deleteCachedArticlesOlderThan(threshold = 200L)

        val dao = database.articleDao()
        assertNull(dao.getArticleById(stale.id).first())
        assertNotNull(dao.getArticleById(staleFavorite.id).first())
        assertNotNull(dao.getArticleById(fresh.id).first())
        assertNull(database.remoteKeyDao().get("old-feed", PROVIDER))
        assertNotNull(database.remoteKeyDao().get("new-feed", PROVIDER))
    }

    private suspend fun insertFeed(feedKey: String, updatedAt: Long, vararg articles: ArticleEntity) {
        database.articleDao().upsertArticles(articles.toList())
        database.feedDao().upsert(FeedEntity(feedKey, PROVIDER, "us", "en", null))
        database.articleFeedCrossRefDao().insertAll(
            articles.mapIndexed { index, article ->
                ArticleFeedCrossRefEntity(feedKey, PROVIDER, article.id, index)
            }
        )
        database.remoteKeyDao().upsert(
            RemoteKeyEntity(feedKey, PROVIDER, nextPageToken = null, endReached = true, updatedAt = updatedAt)
        )
    }

    private fun articleEntity(id: String) = ArticleEntity(
        id = id,
        url = "https://example.com/$id",
        providerId = PROVIDER,
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

    private companion object {
        const val PROVIDER = "news-data"
    }
}
