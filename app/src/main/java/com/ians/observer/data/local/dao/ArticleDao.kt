package com.ians.observer.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.ians.observer.data.local.entity.ArticleEntity
import com.ians.observer.data.local.entity.FavoriteEntity
import com.ians.observer.data.local.projection.ArticleDetailsProjection
import com.ians.observer.data.local.projection.FavoriteArticleProjection
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {

    @Upsert
    suspend fun upsertArticle(article: ArticleEntity)

    @Upsert
    suspend fun upsertArticles(articles: List<ArticleEntity>)

    @Query("SELECT * FROM articles ORDER BY published_at DESC, id DESC")
    fun getAllArticles(): Flow<List<ArticleEntity>>

    @Query(
        """
        SELECT 
            articles.*,
            favorites.source_provider_id AS provider_id,
            favorites.category AS favorite_category
        FROM articles
        INNER JOIN favorites
            ON favorites.article_id = articles.id
        ORDER BY favorites.saved_at DESC
    """
    )
    fun getFavoriteArticles(): Flow<List<FavoriteArticleProjection>>

    @Query(
        """
        SELECT favorites.* FROM favorites
        INNER JOIN articles ON articles.id = favorites.article_id
        WHERE articles.url = :url LIMIT 1
    """
    )
    suspend fun getFavoriteByUrl(url: String): FavoriteEntity?

    @Query(
        """
        SELECT
            articles.*,
            favorites.source_provider_id AS provider_id,
            favorites.category AS favorite_category
        FROM articles
        INNER JOIN favorites
            ON favorites.article_id = articles.id
        WHERE favorites.category = :category
        ORDER BY favorites.saved_at DESC
    """
    )
    fun getFavoriteArticlesForCategory(category: String): Flow<List<FavoriteArticleProjection>>

    @Query(
        """
        SELECT articles.url FROM articles
        INNER JOIN favorites ON favorites.article_id = articles.id
        """
    )
    fun getFavoriteArticlesUrls(): Flow<List<String>>

    @Query("SELECT * FROM articles WHERE url = :url")
    suspend fun getArticleByUrl(url: String): ArticleEntity?

    @Query(
        """
        SELECT
            articles.*,
            COALESCE(
                (SELECT source_provider_id FROM favorites
                    WHERE article_id = articles.id),
                (SELECT provider_id FROM article_feed_cross_refs
                    WHERE article_id = articles.id LIMIT 1),
                articles.article_provider_id
            ) AS details_provider_id,
            COALESCE(
                (SELECT category FROM favorites
                    WHERE article_id = articles.id),
                (SELECT feeds.category
                    FROM article_feed_cross_refs AS refs
                    INNER JOIN feeds
                        ON feeds.feed_key = refs.feed_key
                        AND feeds.provider_id = refs.provider_id
                    WHERE refs.article_id = articles.id
                        AND feeds.category IS NOT NULL
                    LIMIT 1),
                articles.article_category
            ) AS details_category,
            EXISTS(
                SELECT 1 FROM favorites WHERE article_id = articles.id
            ) AS is_favorite
        FROM articles
        WHERE articles.id = :id
        LIMIT 1
        """
    )
    fun getArticleById(id: String): Flow<ArticleDetailsProjection?>

    @Query(
        """
        SELECT * FROM articles
        WHERE title LIKE '%' || :query || '%' COLLATE NOCASE
        OR description LIKE '%' || :query || '%' COLLATE NOCASE
        ORDER BY published_at DESC
    """
    )
    fun searchArticles(query: String): Flow<List<ArticleEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query(
        """
        DELETE FROM favorites
        WHERE article_id = (SELECT id FROM articles WHERE url = :url LIMIT 1)
    """
    )
    suspend fun deleteFavoriteByUrl(url: String)

    @Transaction
    suspend fun addToFavorites(
        article: ArticleEntity,
        category: String?,
        providerId: String,
    ) {
        upsertArticle(article)

        insertFavorite(
            FavoriteEntity(
                articleId = article.id,
                savedAt = System.currentTimeMillis(),
                category = category,
                sourceProviderId = providerId
            )
        )
    }

    @Query(
        """
        DELETE FROM favorites
        WHERE article_id = (SELECT id FROM articles WHERE id = :id LIMIT 1)
        """
    )
    suspend fun removeFromFavorites(id: String)

    @Delete
    suspend fun deleteArticle(article: ArticleEntity)

    @Query("DELETE FROM articles WHERE url = :url")
    suspend fun deleteByUrl(url: String)

    @Query(
        """
        DELETE FROM articles
        WHERE NOT EXISTS(
            SELECT 1 FROM favorites
            WHERE favorites.article_id = articles.id
        )
        AND NOT EXISTS(
            SELECT 1 FROM article_feed_cross_refs
            WHERE article_feed_cross_refs.article_id = articles.id
        )
        AND NOT EXISTS(
            SELECT 1 FROM notification_articles
            WHERE notification_articles.article_id = articles.id
        )
    """
    )
    suspend fun deleteOrphanedArticles()

    @Query(
        """
        DELETE FROM articles
        WHERE id = :articleId
        AND NOT EXISTS(
            SELECT 1 FROM favorites
            WHERE favorites.article_id = articles.id
        )
        AND NOT EXISTS(
            SELECT 1 FROM article_feed_cross_refs
            WHERE article_feed_cross_refs.article_id = articles.id
        )
        AND NOT EXISTS(
            SELECT 1 FROM notification_articles
            WHERE notification_articles.article_id = articles.id
        )
        """
    )
    suspend fun deleteArticleIfOrphan(articleId: String)

    @Query("DELETE FROM articles")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM favorites")
    suspend fun getFavoriteCount(): Int

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM favorites 
            WHERE article_id = (SELECT id FROM articles WHERE url = :url LIMIT 1)
        ) 
    """
    )
    suspend fun isFavorite(url: String): Boolean

    @Query(
        """
        SELECT DISTINCT category FROM favorites
        WHERE category IS NOT NULL
        ORDER BY category
    """
    )
    fun getFavoriteCategories(): Flow<List<String>>
}
