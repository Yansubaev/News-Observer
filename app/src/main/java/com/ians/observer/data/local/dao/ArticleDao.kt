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
import com.ians.observer.data.local.projection.FavoriteArticleProjection
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {

    @Upsert
    suspend fun upsertArticle(article: ArticleEntity)

    @Upsert
    suspend fun upsertArticles(articles: List<ArticleEntity>)

    @Query("SELECT * FROM articles ORDER BY published_at DESC, url DESC")
    fun getAllArticles(): Flow<List<ArticleEntity>>

    @Query(
        """
        SELECT 
            articles.*,
            favorites.source_provider_id AS provider_id,
            favorites.category AS favorite_category
        FROM articles
        INNER JOIN favorites
            ON favorites.article_url = articles.url
        ORDER BY favorites.saved_at DESC
    """
    )
    fun getFavoriteArticles(): Flow<List<FavoriteArticleProjection>>

    @Query(
        """
        SELECT * FROM favorites
        WHERE article_url = :url LIMIT 1
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
            ON favorites.article_url = articles.url 
        WHERE favorites.category = :category
        ORDER BY favorites.saved_at DESC
    """
    )
    fun getFavoriteArticlesForCategory(category: String): Flow<List<FavoriteArticleProjection>>

    @Query("SELECT article_url FROM favorites")
    fun getFavoriteArticlesUrls(): Flow<List<String>>

    @Query("SELECT * FROM articles WHERE url = :url")
    suspend fun getArticleByUrl(url: String): ArticleEntity?

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
        WHERE article_url = :url
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
                articleUrl = article.url,
                savedAt = System.currentTimeMillis(),
                category = category,
                sourceProviderId = providerId
            )
        )
    }

    @Query("DELETE FROM favorites WHERE article_url = :url")
    suspend fun remoteFromFavorites(url: String)

    @Delete
    suspend fun deleteArticle(article: ArticleEntity)

    @Query("DELETE FROM articles WHERE url = :url")
    suspend fun deleteByUrl(url: String)

    @Query(
        """
        DELETE FROM articles
        WHERE NOT EXISTS(
            SELECT 1 FROM favorites
            WHERE favorites.article_url = articles.url
        )
        AND NOT EXISTS(
            SELECT 1 FROM article_feed_cross_refs
            WHERE article_feed_cross_refs.article_url = articles.url
        )
    """
    )
    suspend fun deleteOrphanedArticles()

    @Query("DELETE FROM articles")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM favorites")
    suspend fun getFavoriteCount(): Int

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM favorites 
            WHERE article_url = :url
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

