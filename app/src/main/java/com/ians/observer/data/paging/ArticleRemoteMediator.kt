package com.ians.observer.data.paging

import android.content.SharedPreferences
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.ians.observer.data.local.dao.NewsDatabase
import com.ians.observer.data.local.entity.ArticleEntity
import com.ians.observer.data.local.entity.toEntity
import com.ians.observer.data.remote.api.NewsApi
import com.ians.observer.data.remote.dto.toArticle
import com.ians.observer.domain.repository.SettingRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import kotlin.collections.emptyList

class ArticleRemoteMediatorFactory @Inject constructor(
    private val newsApi: NewsApi,
    private val database: NewsDatabase,
    private val settingRepository: SettingRepository
) {
    suspend fun create(category: String? = null): ArticleRemoteMediator {
        return ArticleRemoteMediator(
            newsApi = newsApi,
            database = database,
            country = settingRepository.getCountryPreference(),
            category = category,
            lastSyncTime = settingRepository.getLastSyncTime(),
            syncInterval = settingRepository.getSyncInterval()
        )
    }
}

@OptIn(ExperimentalPagingApi::class)
class ArticleRemoteMediator(
    private val newsApi: NewsApi,
    private val database: NewsDatabase,
    private val country: String = "us",
    private val category: String? = null,
    private val lastSyncTime: Long = System.currentTimeMillis(),
    private val syncInterval: Long = 10 * 60 * 1000 // 10 minutes
) : RemoteMediator<Int, ArticleEntity>() {

    private val articleDao = database.articleDao()

    override suspend fun initialize(): InitializeAction {
        val currentTime = System.currentTimeMillis()

        return if (currentTime - lastSyncTime > syncInterval) {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else {
            InitializeAction.SKIP_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType, state: PagingState<Int, ArticleEntity>
    ): MediatorResult {
        return try {
            val page = when (loadType) {
                LoadType.REFRESH -> {
                    1
                }

                LoadType.PREPEND -> {
                    return MediatorResult.Success(endOfPaginationReached = true)
                }

                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull()

                    if (lastItem == null) {
                        1
                    } else {
                        lastItem.page + 1
                    }
                }
            }

            val response = newsApi.getHeadlines(
                country = country,
                category = category,
                page = page,
                pageSize = state.config.pageSize
            )

            val existingFavoriteUrls = if (loadType == LoadType.REFRESH) {
                articleDao.getFavoriteArticlesUrls().first()
            } else {
                emptyList()
            }

            val articles = response.articles.map { dto ->
                val url = dto.url
                val isFavorite = url in existingFavoriteUrls

                dto.toArticle(page = page).toEntity().copy(
                    page = page,
                    isFavorite = isFavorite,
                    category = category
                )
            }

            database.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
                        articleDao.deleteNonFavoritesByCategory(category)
                    }

                    LoadType.APPEND -> {

                    }

                    else -> {}
                }

                articleDao.insertOrUpdateArticles(articles)
            }

            val endOfPaginationReached = articles.isEmpty()

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    private fun parseDateToMillis(dateString: String): Long? {
        return try {
            // (format: "2024-01-15T10:30:00Z")
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                .parse(dateString)
                ?.time
        } catch (e: Exception) {
            null
        }
    }
}