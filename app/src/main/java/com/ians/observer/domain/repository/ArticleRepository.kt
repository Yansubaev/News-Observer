package com.ians.observer.domain.repository

import androidx.paging.PagingData
import com.ians.observer.domain.model.Article
import com.ians.observer.domain.model.Category
import com.ians.observer.domain.model.FeedSpec
import com.ians.observer.domain.model.SearchSpec
import kotlinx.coroutines.flow.Flow

interface ArticleRepository {

    fun observeTopHeadlinesPaging(spec: FeedSpec, syncInterval: Long): Flow<PagingData<Article>>

    fun searchNewsPaging(spec: SearchSpec): Flow<PagingData<Article>>

    fun observeFavoriteArticles(): Flow<List<Article>>

    fun observeFavoriteUrls(): Flow<Set<String>>

    fun observeFavoriteArticlesForCategory(category: Category): Flow<List<Article>>

    fun observeFavoriteCategories(): Flow<List<Category>>

    suspend fun addToFavorites(article: Article, category: Category?)

    suspend fun removeFromFavorites(id: String)

    fun observeArticleById(id: String): Flow<Article?>

    suspend fun clearCachedArticles()


}