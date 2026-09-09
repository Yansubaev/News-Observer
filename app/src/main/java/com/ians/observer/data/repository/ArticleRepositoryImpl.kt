package com.ians.observer.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.room.withTransaction
import com.ians.observer.data.local.replaceNotificationArticle
import com.ians.observer.data.local.dao.NewsDatabase
import com.ians.observer.data.local.entity.ArticleEntity
import com.ians.observer.data.local.entity.toEntity
import com.ians.observer.data.local.entity.toArticle
import com.ians.observer.data.paging.ArticleRemoteMediatorFactory
import com.ians.observer.data.paging.ArticleSearchPagingSource
import com.ians.observer.data.paging.FeedKeyFactory
import com.ians.observer.data.paging.model.PagingSourceSpec
import com.ians.observer.data.remote.provider.NewsProviderRegistry
import com.ians.observer.data.remote.provider.model.FeedRequest
import com.ians.observer.data.remote.provider.model.toArticle
import com.ians.observer.domain.model.Article
import com.ians.observer.domain.model.Category
import com.ians.observer.domain.model.FeedSpec
import com.ians.observer.domain.model.SearchSpec
import com.ians.observer.domain.repository.ArticleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArticleRepositoryImpl @Inject constructor(
    private val newsProviderRegistry: NewsProviderRegistry,
    private val database: NewsDatabase,
    private val remoteMediatorFactory: ArticleRemoteMediatorFactory
) : ArticleRepository {

    companion object {
        private const val PAGE_SIZE = 10
        private const val INITIAL_LOAD_SIZE = 10
        private const val PREFETCH_DISTANCE = 4
    }

    @OptIn(ExperimentalPagingApi::class)
    override fun observeTopHeadlinesPaging(
        spec: FeedSpec,
        syncInterval: Long,
    ): Flow<PagingData<Article>> = flow {
        val availableProviderId = spec.providerIds.find { it in newsProviderRegistry.availableIds }
            ?: throw IllegalArgumentException("No available provider for required found. ProviderId=${null}")

        val newsProvider = newsProviderRegistry.require(availableProviderId)

        val feedKey = FeedKeyFactory.create(
            PagingSourceSpec.Feed(
                country = spec.country,
                language = spec.language,
                category = spec.category
            )
        )

        val remoteMediator = remoteMediatorFactory.create(
            newsProvider = newsProvider,
            category = spec.category,
            country = spec.country,
            language = spec.language,
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

    override suspend fun fetchTopHeadlines(spec: FeedSpec): List<Article> {
        val availableProviderId = spec.providerIds.find { it in newsProviderRegistry.availableIds }
            ?: throw IllegalArgumentException("No available provider for required found. ProviderId=${spec.providerIds}")

        val newsProvider = newsProviderRegistry.require(availableProviderId)

        val pageResult = newsProvider.loadFeed(
            request = FeedRequest(
                country = spec.country,
                language = spec.language,
                category = spec.category
            ),
            pageToken = null
        )

        return pageResult.articles.map { it.toArticle(false) }
    }

    override fun searchNewsPaging(spec: SearchSpec): Flow<PagingData<Article>> {
        val availableProviderId = spec.providerIds.find { it in newsProviderRegistry.availableIds }
            ?: throw IllegalArgumentException("No available provider for required found. ProviderId=${null}")

        val newsProvider = newsProviderRegistry.require(availableProviderId)

        return Pager(
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
                    query = spec.query,
                    language = spec.language,
                    country = spec.country,
                    category = spec.category
                )
            }
        ).flow
    }

    override fun observeFavoriteArticles(): Flow<List<Article>> {
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

    override fun observeFavoriteArticlesForCategory(category: Category): Flow<List<Article>> {
        return if (category == Category.ALL) observeFavoriteArticles()
        else database.articleDao().getFavoriteArticlesForCategory(category.value)
            .map { entities ->
                entities.map {
                    it.toArticle()
                }
            }
    }

    override fun observeFavoriteCategories(): Flow<List<Category>> {
        return database.articleDao().getFavoriteCategories()
            .map { strings ->
                strings.mapNotNull {
                    Category.fromValue(it)
                }
            }
    }

    override suspend fun addToFavorites(
        article: Article,
        category: Category?,
    ) {
        database.articleDao().addToFavorites(
            article = ArticleEntity(
                id = article.id,
                url = article.originalUrl,
                providerId = article.providerId.value,
                category = article.category?.value,
                publisherId = article.publisher.id,
                publisherName = article.publisher.name,
                publisherWebsiteUrl = article.publisher.websiteUrl,
                authors = article.authors,
                title = article.title,
                description = article.description,
                imageUrl = article.imageUrl,
                publishedAt = article.publishedAt,
                content = article.content
            ),
            category = category?.value,
            providerId = article.providerId.value,
        )
    }

    override suspend fun removeFromFavorites(id: String) {
        database.articleDao().removeFromFavorites(id)
    }

    override fun observeArticleById(id: String): Flow<Article?> {
        return database.articleDao().getArticleById(id).map {
            it?.toArticle()
        }
    }

    override suspend fun clearCachedArticles() {
        database.withTransaction {
            database.remoteKeyDao().deleteAllRemoteKeys()
            database.feedDao().deleteAllFeeds()
            database.articleDao().deleteOrphanedArticles()
        }
    }

    override suspend fun replaceNotificationArticle(article: Article) {
        database.replaceNotificationArticle(article.toEntity())
    }
}
