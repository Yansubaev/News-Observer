package com.ians.observer.domain.repository

import androidx.paging.PagingData
import com.ians.observer.domain.model.Article
import kotlinx.coroutines.flow.Flow

interface ArticleRepository {
    fun getTopHeadlines(
        country: String,
        category: String? = null
    ): Flow<Result<List<Article>>>

    fun getTopHeadlinesPaging(
        country: String,
        category: String? = null
    ): Flow<PagingData<Article>>

    fun searchNews(
        query: String,
        language: String? = null
    ): Flow<Result<List<Article>>>

    fun searchNews(
        query: String,
        language: String? = null,
        page: Int = 1,
        pageSize: Int = 20
    ): Flow<PagingData<Article>>

    fun getFavoriteArticles(): Flow<List<Article>>

    fun getFavoriteArticlesForCategory(category: String): Flow<List<Article>>

    suspend fun toggleFavorite(article: Article)

    suspend fun getFavoriteCategories(): Flow<List<String>>

    suspend fun isFavorite(articleUrl: String): Boolean

    suspend fun getArticleByUrl(articleUrl: String): Article?
}