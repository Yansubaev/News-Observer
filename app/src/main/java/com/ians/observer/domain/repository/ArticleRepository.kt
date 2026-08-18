package com.ians.observer.domain.repository

import androidx.paging.PagingData
import com.ians.observer.domain.model.Article
import com.ians.observer.domain.model.Category
import com.ians.observer.domain.model.NewsLanguage
import com.ians.observer.domain.model.NewsCountry
import kotlinx.coroutines.flow.Flow

interface ArticleRepository {

    fun getTopHeadlinesPaging(
        category: Category?,
        country: NewsCountry,
        language: NewsLanguage,
        syncInterval: Long,
    ): Flow<PagingData<Article>>

    fun searchNewsPaging(
        query: String,
        language: NewsLanguage? = null,
        country: NewsCountry? = null,
        category: Category? = null
    ): Flow<PagingData<Article>>

    fun getFavoriteArticles(): Flow<List<Article>>

    fun observeFavoriteUrls(): Flow<Set<String>>

    fun getFavoriteArticlesForCategory(category: Category): Flow<List<Article>>

    fun getFavoriteCategories(): Flow<List<String>>

    suspend fun addToFavorites(article: Article, category: Category?)

    suspend fun removeFromFavorites(url: String)

}