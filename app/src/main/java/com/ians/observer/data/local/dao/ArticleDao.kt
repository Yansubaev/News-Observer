package com.ians.observer.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ians.observer.data.local.entity.ArticleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: ArticleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<ArticleEntity>)

    @Transaction
    suspend fun insertOrUpdateArticles(articles: List<ArticleEntity>) {
        articles.forEach { article ->
            val existing = getArticleByUrl(article.url)

            if (existing != null) {
                updateArticle(
                    article.copy(
                        isFavorite = existing.isFavorite
                    )
                )
            } else {
                insertArticle(article)
            }
        }
    }

    @Query("SELECT * FROM articles ORDER BY published_at DESC")
    fun getAllArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE page = :page ORDER BY published_at DESC")
    fun getArticlesByPage(page: Int): List<ArticleEntity>

    @Query("SELECT * FROM articles WHERE is_favorite = 1 ORDER BY saved_at DESC")
    fun getFavoriteArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT url FROM ARTICLES WHERE is_favorite = 1 ORDER BY saved_at DESC")
    fun getFavoriteArticlesUrls(): Flow<List<String>>

    @Query("SELECT * FROM articles WHERE url = :url")
    suspend fun getArticleByUrl(url: String): ArticleEntity?

    @Query("SELECT MAX(page) FROM articles")
    suspend fun getLastPage(): Int?

    @Query(
        """
        SELECT * FROM articles
        WHERE title LIKE '%' || :query || '%' COLLATE NOCASE
        OR description LIKE '%' || :query || '%' COLLATE NOCASE
        ORDER BY published_at DESC
    """
    )
    fun searchArticles(query: String): Flow<List<ArticleEntity>>

    @Update
    suspend fun updateArticle(article: ArticleEntity)

    @Query(
        """
        UPDATE articles
        SET is_favorite = CASE
            WHEN is_favorite = 1 THEN 0
            ELSE 1
        END
        WHERE url = :url
    """
    )
    suspend fun toggleFavorite(url: String)

    @Delete
    suspend fun deleteArticle(article: ArticleEntity)

    @Query("DELETE FROM articles WHERE page = :page")
    suspend fun deleteByPage(page: Int)

    @Query("DELETE FROM articles WHERE url = :url")
    suspend fun deleteByUrl(url: String)

    @Query("DELETE FROM articles WHERE is_favorite = 0")
    suspend fun deleteNonFavorites()

    @Query("DELETE FROM articles")
    suspend fun deleteAll()

    @Query("DELETE FROM articles WHERE saved_at < :timestamp AND is_favorite = 0")
    suspend fun deleteOldArticles(timestamp: Long)

    @Query("DELETE FROM articles WHERE page = :page AND is_favorite = 0")
    suspend fun deleteNonFavoritesByPage(page: Int)

    @Query("DELETE FROM articles WHERE category = :category AND is_favorite = 0")
    suspend fun deleteNonFavoritesByCategory(category: String?)

    @Query("SELECT COUNT(*) FROM articles")
    suspend fun getArticleCount(): Int

    @Query("SELECT COUNT(*) FROM articles WHERE is_favorite = 1")
    suspend fun getFavoriteCount(): Int

    @Query("SELECT EXISTS(SELECT 1 FROM articles WHERE url = :url AND is_favorite = 1)")
    suspend fun isFavorite(url: String): Boolean
}

