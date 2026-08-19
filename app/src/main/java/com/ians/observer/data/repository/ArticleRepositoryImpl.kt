package com.ians.observer.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.ians.observer.data.local.dao.NewsDatabase
import com.ians.observer.data.local.entity.ArticleEntity
import com.ians.observer.data.local.entity.toArticle
//import com.ians.observer.data.local.entity.toArticle
import com.ians.observer.data.paging.ArticleRemoteMediatorFactory
import com.ians.observer.data.paging.ArticleSearchPagingSource
import com.ians.observer.data.paging.FeedKeyFactory
import com.ians.observer.data.paging.model.PagingSourceSpec
import com.ians.observer.data.remote.provider.NewsProvider
import com.ians.observer.domain.model.Article
import com.ians.observer.domain.model.Category
import com.ians.observer.domain.model.NewsCountry
import com.ians.observer.domain.model.NewsLanguage
import com.ians.observer.domain.repository.ArticleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArticleRepositoryImpl @Inject constructor(
    private val newsProvider: NewsProvider,
    private val database: NewsDatabase,
    private val remoteMediatorFactory: ArticleRemoteMediatorFactory
) : ArticleRepository {

    companion object {
        private const val PAGE_SIZE = 10
        private const val INITIAL_LOAD_SIZE = 10
        private const val PREFETCH_DISTANCE = 4
    }

    @OptIn(ExperimentalPagingApi::class)
    override fun getTopHeadlinesPaging(
        category: Category?,
        country: NewsCountry,
        language: NewsLanguage,
        syncInterval: Long,
    ): Flow<PagingData<Article>> = flow {
        val feedKey = FeedKeyFactory.create(
            PagingSourceSpec.Feed(
                country = country,
                language = language,
                category = category
            )
        )

        val remoteMediator = remoteMediatorFactory.create(
            category = category,
            country = country,
            language = language,
            syncInterval = syncInterval
        )

        val pager = Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = INITIAL_LOAD_SIZE,
                enablePlaceholders = false,
                prefetchDistance = PREFETCH_DISTANCE
            ),
            remoteMediator = remoteMediator,
            pagingSourceFactory = {
                database.articlePagingDao().pagingSource(
                    feedKey = feedKey,
                    providerId = newsProvider.id.value
                )
            }
        )

        emitAll(pager.flow.map { pagingData ->
            pagingData.map { entity ->
                entity.toArticle()
            }
        })
    }

    override fun searchNewsPaging(
        query: String,
        language: NewsLanguage?,
        country: NewsCountry?,
        category: Category?
    ): Flow<PagingData<Article>> = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            initialLoadSize = INITIAL_LOAD_SIZE,
            enablePlaceholders = false,
            prefetchDistance = PREFETCH_DISTANCE
        ),
        pagingSourceFactory = {
            ArticleSearchPagingSource(
                newsProvider = newsProvider,
                database = database,
                query = query,
                language = language,
                country = country,
                category = category
            )
        }
    ).flow

    override fun getFavoriteArticles(): Flow<List<Article>> {
        return database.articleDao().getFavoriteArticles()
            .map { entities ->
                entities.map {
                    it.toArticle()
                }
            }
    }

    override fun observeFavoriteUrls(): Flow<Set<String>> {
        return database.articleDao().getFavoriteArticlesUrls()
            .map { list ->
                list.toSet()
            }
    }

    override fun getFavoriteArticlesForCategory(category: Category): Flow<List<Article>> {
        return database.articleDao().getFavoriteArticlesForCategory(category.value)
            .map { entities ->
                entities.map {
                    it.toArticle()
                }
            }
    }

    override fun getFavoriteCategories(): Flow<List<String>> {
        return database.articleDao().getFavoriteCategories()
    }

    override suspend fun addToFavorites(
        article: Article,
        category: Category?,
    ) {
        database.articleDao().addToFavorites(
            article = ArticleEntity(
                url = article.originalUrl,
                publisherId = article.publisher.id,
                publisherName = article.publisher.name,
                authors = article.authors,
                title = article.title,
                description = article.description,
                imageUrl = article.imageUrl,
                publishedAt = article.publishedAt,
                content = article.content
            ),
            category = category?.value,
            providerId = newsProvider.id.value,
        )
    }

    override suspend fun removeFromFavorites(url: String) {
        database.articleDao().remoteFromFavorites(url)
    }
}