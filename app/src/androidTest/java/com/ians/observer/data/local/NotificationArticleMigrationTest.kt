package com.ians.observer.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ians.observer.data.local.dao.NewsDatabase
import com.ians.observer.data.local.entity.ArticleEntity
import com.ians.observer.data.local.entity.ArticleFeedCrossRefEntity
import com.ians.observer.data.local.entity.FavoriteEntity
import com.ians.observer.data.local.entity.FeedEntity
import com.ians.observer.data.local.entity.RemoteKeyEntity
import com.ians.observer.data.local.migration.MIGRATION_12_13
import com.ians.observer.data.local.migration.MIGRATION_13_14
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationArticleMigrationTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun setUp() {
        context.deleteDatabase(DATABASE_NAME)
    }

    @After
    fun tearDown() {
        context.deleteDatabase(DATABASE_NAME)
    }

    @Test
    fun migrationFrom12To13CreatesNotificationTableAndKeepsArticles() = runTest {
        val article = articleEntity("existing")
        val oldDatabase = Room
            .databaseBuilder(context, TestNewsDatabaseV12::class.java, DATABASE_NAME)
            .build()
        try {
            oldDatabase.articleDao().insert(article)
        } finally {
            oldDatabase.close()
        }

        val migratedDatabase = Room
            .databaseBuilder(context, NewsDatabase::class.java, DATABASE_NAME)
            .addMigrations(MIGRATION_12_13, MIGRATION_13_14)
            .build()
        try {
            assertNotNull(migratedDatabase.articleDao().getArticleById(article.id).first())
            assertNull(
                migratedDatabase.notificationArticleDao()
                    .getArticleId("missing_notification"),
            )
        } finally {
            migratedDatabase.close()
        }
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

    companion object {
        private const val DATABASE_NAME = "notification-migration-test"
    }
}

@Dao
internal interface TestArticleDaoV12 {
    @Insert
    suspend fun insert(article: ArticleEntity)
}

@Database(
    entities = [
        ArticleEntity::class,
        RemoteKeyEntity::class,
        ArticleFeedCrossRefEntity::class,
        FavoriteEntity::class,
        FeedEntity::class,
    ],
    version = 12,
    exportSchema = false,
)
internal abstract class TestNewsDatabaseV12 : RoomDatabase() {
    abstract fun articleDao(): TestArticleDaoV12
}
