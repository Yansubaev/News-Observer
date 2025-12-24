package com.ians.observer.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ians.observer.data.local.entity.ArticleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: ArticleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<ArticleEntity>)

    @Query("SELECT * FROM articles ORDER BY saved_at DESC")
    fun getAllArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE is_favorite = 1 ORDER BY saved_at DESC")
    fun getFavoriteArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE url = :url")
    suspend fun getArticleByUrl(url: String): ArticleEntity?

    @Query(
        """
        SELECT * FROM articles
        WHERE title LIKE '%' || :query || '%' COLLATE NOCASE
        OR description LIKE '%' || :query || '%' COLLATE NOCASE
        ORDER BY saved_at DESC
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

    @Query("DELETE FROM articles WHERE url = :url")
    suspend fun deleteByUrl(url: String)

    @Query("DELETE FROM articles WHERE is_favorite = 0")
    suspend fun deleteNonFavorites()

    @Query("DELETE FROM articles")
    suspend fun deleteAll()

    @Query("DELETE FROM articles WHERE saved_at < :timestamp AND is_favorite = 0")
    suspend fun deleteOldArticles(timestamp: Long)

    @Query("SELECT COUNT(*) FROM articles")
    suspend fun getArticleCount(): Int

    @Query("SELECT COUNT(*) FROM articles WHERE is_favorite = 1")
    suspend fun getFavoriteCount(): Int

    @Query("SELECT EXISTS(SELECT 1 FROM articles WHERE url = :url AND is_favorite = 1)")
    suspend fun isFavorite(url: String): Boolean
}

