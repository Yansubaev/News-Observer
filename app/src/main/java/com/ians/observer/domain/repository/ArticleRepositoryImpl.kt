package com.ians.observer.domain.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.ians.observer.data.local.dao.ArticleDao
import com.ians.observer.data.local.entity.toArticle
import com.ians.observer.data.local.entity.toEntity
import com.ians.observer.data.paging.ArticlePagingSource
import com.ians.observer.data.remote.api.NewsApi
import com.ians.observer.data.remote.dto.toArticle
import com.ians.observer.domain.model.Article
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArticleRepositoryImpl @Inject constructor(
    private val newsApi: NewsApi,
    private val articleDao: ArticleDao
) : ArticleRepository {

    override fun getTopHeadlines(
        country: String,
        category: String?
    ): Flow<Result<List<Article>>> = flow {

        val cachedArticles = articleDao.getAllArticles()
            .map { entities -> entities.map { it.toArticle() } }
            .first()

        if (cachedArticles.isNotEmpty()) {
            emit(Result.success(cachedArticles))
        }

        try {
            val response = newsApi.getHeadlines(
                country = country,
                category = category
            )

            if (response.status == "ok") {

                val freshArticles = response.articles.map { it.toArticle(isFavorite(it.url)) }
                articleDao.deleteNonFavorites()

                articleDao.insertArticles(
                    freshArticles.map { it.toEntity() }
                )

                emit(Result.success(freshArticles))
            } else {
                emit(Result.failure(Exception("API error: ${response.status}")))
            }
        } catch (e: Exception) {
            print(e.message)

            val cachedData = articleDao.getAllArticles()
                .map { it.map { entity -> entity.toArticle() } }

            cachedData.collect { cached ->
                if (cached.isEmpty()) {
                    emit(Result.failure(e))
                }
            }
        }
    }

    override fun getTopHeadlinesPaging(
        country: String,
        category: String?
    ): Flow<PagingData<Article>> = Pager(
        config = PagingConfig(
            pageSize = ArticlePagingSource.ITEMS_PER_PAGE,
            prefetchDistance = 3,
            enablePlaceholders = false,
            initialLoadSize = ArticlePagingSource.ITEMS_PER_PAGE,
            maxSize = 200
        ),
        pagingSourceFactory = {
            ArticlePagingSource(
                newsApi = newsApi,
                articleDao = articleDao,
                country = country,
                category = category
            )
        }
    ).flow

    override fun searchNews(
        query: String,
        language: String?
    ): Flow<Result<List<Article>>> = flow {

        val cachedResults = articleDao.searchArticles(query)
            .map { entities -> entities.map { it.toArticle() } }
            .first()

        if (cachedResults.isNotEmpty()) {
            emit(Result.success(cachedResults))
        }

        try {
            val response = newsApi.searchNews(
                query = query,
                language = language
            )

            if (response.status == "ok") {
                val freshArticles = response.articles.map { it.toArticle(isFavorite(it.url)) }

                articleDao.insertArticles(
                    freshArticles.map { it.toEntity() }
                )

                emit(Result.success(freshArticles))
            } else {
                emit(Result.failure(Exception("Search failed. API returned: \${response.status}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }

    }

    override fun searchNews(
        query: String,
        language: String?,
        page: Int,
        pageSize: Int
    ): Flow<PagingData<Article>> = flow {

    }

    override fun getFavoriteArticles(): Flow<List<Article>> {
        return articleDao.getFavoriteArticles()
            .map { entities -> entities.map { it.toArticle() } }
    }

    override suspend fun toggleFavorite(article: Article) {
        val existingArticle = articleDao.getArticleByUrl(article.url)

        if (existingArticle != null) {
            articleDao.toggleFavorite(article.url)
        } else {
            val favoriteArticle = article.copy(isFavorite = true)
            articleDao.insertArticle(favoriteArticle.toEntity())
        }
    }

    override suspend fun isFavorite(articleUrl: String): Boolean {
        return articleDao.isFavorite(articleUrl)
    }

    override suspend fun getArticleByUrl(articleUrl: String): Article? {
        return articleDao.getArticleByUrl(articleUrl)?.toArticle()
    }

}