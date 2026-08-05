package com.ians.observer.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.ians.observer.data.local.dao.NewsDatabase
import com.ians.observer.data.local.entity.toArticle
import com.ians.observer.data.local.entity.toEntity
import com.ians.observer.data.paging.ArticleRemoteMediatorFactory
import com.ians.observer.data.paging.ArticleSearchPagingSource
import com.ians.observer.data.remote.api.NewsApi
import com.ians.observer.data.remote.dto.toArticle
import com.ians.observer.domain.model.Article
import com.ians.observer.domain.repository.ArticleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArticleRepositoryImpl @Inject constructor(
    private val newsApi: NewsApi,
    private val database: NewsDatabase,
    private val remoteMediatorFactory: ArticleRemoteMediatorFactory
) : ArticleRepository {

    override fun getTopHeadlines(
        country: String,
        category: String?
    ): Flow<Result<List<Article>>> = flow {

        val cachedArticles = database.articleDao().getAllArticles()
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

                val freshArticles = response.articles.map {
                    it.toArticle(
                        isFavorite(it.url),
                        page = 0,
                    )
                }
                database.articleDao().deleteNonFavorites()

                database.articleDao().insertArticles(
                    freshArticles.map { it.toEntity() }
                )

                emit(Result.success(freshArticles))
            } else {
                emit(Result.failure(Exception("API error: ${response.status}")))
            }
        } catch (e: Exception) {
            print(e.message)

            val cachedData = database.articleDao().getAllArticles()
                .map { it.map { entity -> entity.toArticle() } }
                .first()

            if (cachedData.isEmpty()) {
                emit(Result.failure(e))
            }
        }
    }

    @OptIn(ExperimentalPagingApi::class)
    override fun getTopHeadlinesPaging(
        country: String,
        category: String?
    ): Flow<PagingData<Article>> = flow {
        val remoteMediator = remoteMediatorFactory.create(category)

        val pager = Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 20
            ),
            remoteMediator = remoteMediator,
            pagingSourceFactory = {
                database.articlePagingDao().pagingSource(category = category)
            }
        )

        emitAll(pager.flow.map { pagingData ->
            pagingData.map { entity ->
                entity.toArticle()
            }
        })
    }

    override fun searchNews(
        query: String,
        language: String?
    ): Flow<Result<List<Article>>> = flow {

        val cachedResults = database.articleDao().searchArticles(query)
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
                val freshArticles = response.articles.map {
                    it.toArticle(
                        isFavorite(it.url),
                        page = 0,
                    )
                }

                database.articleDao().insertArticles(
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

    override fun searchNewsPaging(
        query: String,
        language: String?,
    ): Flow<PagingData<Article>> = Pager(
        config = PagingConfig(
            pageSize = 20,
            enablePlaceholders = false,
            initialLoadSize = 20
        ),
        pagingSourceFactory = {
            ArticleSearchPagingSource(
                api = newsApi,
                database = database,
                query = query,
                language = language
            )
        }
    ).flow

    override fun getFavoriteArticles(): Flow<List<Article>> {
        return database.articleDao().getFavoriteArticles()
            .map { entities -> entities.map { it.toArticle() } }
    }

    override fun getFavoriteArticlesForCategory(category: String): Flow<List<Article>> {
        return database.articleDao().getFavoriteArticlesForCategory(category)
            .map { entities -> entities.map { it.toArticle() } }
    }

    override suspend fun toggleFavorite(article: Article) {
        if (database.articleDao().getArticleByUrl(article.url) == null) {
            database.articleDao().insertArticle(article.toEntity())
        }

        database.articleDao().toggleFavorite(article.url)
    }

    override fun getFavoriteCategories(): Flow<List<String>> {
        return database.articleDao().getFavoriteCategories()
    }

    override suspend fun isFavorite(articleUrl: String): Boolean {
        return database.articleDao().isFavorite(articleUrl)
    }

    override suspend fun getArticleByUrl(articleUrl: String): Article? {
        return database.articleDao().getArticleByUrl(articleUrl)?.toArticle()
    }

}