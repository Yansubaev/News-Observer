package com.ians.observer.data.paging

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.ians.observer.data.local.dao.NewsDatabase
import com.ians.observer.data.local.entity.ArticleFeedCrossRefEntity
import com.ians.observer.data.local.entity.FeedEntity
import com.ians.observer.data.local.entity.RemoteKeyEntity
import com.ians.observer.data.local.projection.FeedArticleProjection
import com.ians.observer.data.paging.model.PagingSourceSpec
import com.ians.observer.data.remote.provider.NewsProvider
import com.ians.observer.data.remote.provider.model.FeedRequest
import com.ians.observer.data.remote.provider.model.PageToken
import com.ians.observer.data.remote.provider.model.toEntity
import com.ians.observer.domain.model.Category
import com.ians.observer.domain.model.NewsCountry
import com.ians.observer.domain.model.NewsLanguage
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class ArticleRemoteMediatorFactory @Inject constructor(
    private val database: NewsDatabase,
) {
    fun create(
        newsProvider: NewsProvider,
        category: Category? = null,
        country: NewsCountry = NewsCountry.US,
        language: NewsLanguage = NewsLanguage.EN,
        syncInterval: Long = 30 * 60 * 1_000L
    ): ArticleRemoteMediator = ArticleRemoteMediator(
        newsProvider = newsProvider,
        database = database,
        country = country,
        language = language,
        category = category,
        syncInterval = syncInterval,
    )
}

@OptIn(ExperimentalPagingApi::class)
class ArticleRemoteMediator(
    private val newsProvider: NewsProvider,
    private val database: NewsDatabase,
    private val country: NewsCountry = NewsCountry.US,
    private val language: NewsLanguage = NewsLanguage.EN,
    private val category: Category? = null,
    private val syncInterval: Long = 30 * 60 * 1_000L,
) : RemoteMediator<Int, FeedArticleProjection>() {

    private val articleDao = database.articleDao()
    private val remoteKeyDao = database.remoteKeyDao()
    private val articleFeedCrossRefDao = database.articleFeedCrossRefDao()
    private val feedDao = database.feedDao()

    companion object {
        private const val TAG = "ArticleRemoteMediator"
    }

    override suspend fun initialize(): InitializeAction {
        val currentTime = System.currentTimeMillis()
        val feedKey = FeedKeyFactory.create(
            PagingSourceSpec.Feed(
                country = country,
                language = language,
                category = category
            )
        )
        val remoteKey = remoteKeyDao.get(feedKey, newsProvider.id.value)
        val lastSyncTime = remoteKey?.updatedAt ?: 0

        return if (currentTime - lastSyncTime > syncInterval) {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else {
            InitializeAction.SKIP_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType, state: PagingState<Int, FeedArticleProjection>
    ): MediatorResult {
        return try {
            val feedKey = FeedKeyFactory.create(
                PagingSourceSpec.Feed(
                    country = country,
                    language = language,
                    category = category
                )
            )
            val pageToken = when (loadType) {
                LoadType.APPEND -> {
                    val remoteKey = remoteKeyDao.get(
                        feedKey = feedKey, providerId = newsProvider.id.value
                    )

                    if (remoteKey?.endReached == true) {
                        return MediatorResult.Success(endOfPaginationReached = true)
                    }

                    remoteKey?.nextPageToken?.let(::PageToken)
                }

                LoadType.REFRESH -> null
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            }

            val providerPage = newsProvider.loadFeed(
                request = FeedRequest(
                    country = country, language = language, category = category
                ),
                pageToken = pageToken
            )

            val articleEntities = providerPage.articles.map { article ->

                article.toEntity()
            }

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    articleFeedCrossRefDao.deleteByFeed(
                        feedKey = feedKey,
                        providerId = newsProvider.id.value
                    )
                    articleDao.deleteOrphanedArticles()
                }

                articleDao.upsertArticles(articleEntities)

                val startPosition = if (loadType == LoadType.REFRESH) {
                    0
                } else {
                    articleFeedCrossRefDao.getMaxPosition(
                        feedKey = feedKey,
                        providerId = newsProvider.id.value
                    ) + 1
                }

                val refs = providerPage.articles
                    .distinctBy { it.id }
                    .mapIndexed { index, article ->
                        ArticleFeedCrossRefEntity(
                            feedKey = feedKey,
                            providerId = newsProvider.id.value,
                            articleId = article.id,
                            position = startPosition + index
                        )
                    }

                feedDao.upsert(
                    FeedEntity(
                        feedKey = feedKey,
                        providerId = newsProvider.id.value,
                        country = country.code,
                        language = language.code,
                        category = category?.value
                    )
                )

                articleFeedCrossRefDao.insertAll(refs)

                remoteKeyDao.upsert(
                    RemoteKeyEntity(
                        feedKey = feedKey,
                        providerId = newsProvider.id.value,
                        nextPageToken = providerPage.nextPageToken?.value,
                        endReached = providerPage.nextPageToken == null ||
                                providerPage.articles.isEmpty(),
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }

            val endOfPaginationReached =
                providerPage.nextPageToken == null || providerPage.nextPageToken == pageToken
            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load page for provider ${newsProvider.id.value}", e)
            MediatorResult.Error(e)
        }
    }
}
