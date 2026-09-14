package com.ians.observer.domain.usecase.fake

import androidx.paging.PagingData
import com.ians.observer.domain.background.DailyNewsScheduler
import com.ians.observer.domain.model.Article
import com.ians.observer.domain.model.Category
import com.ians.observer.domain.model.FeedSpec
import com.ians.observer.domain.model.NewsCountry
import com.ians.observer.domain.model.NewsLanguage
import com.ians.observer.domain.model.ProviderId
import com.ians.observer.domain.model.Publisher
import com.ians.observer.domain.model.SearchSpec
import com.ians.observer.domain.repository.ArticleRepository
import com.ians.observer.domain.repository.NewsProvidersRepository
import com.ians.observer.domain.repository.SearchHistoryRepository
import com.ians.observer.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

internal data class TopHeadlinesRequest(
    val spec: FeedSpec,
    val syncInterval: Long,
)

internal class FakeArticleRepository : ArticleRepository {
    val topHeadlinesRequests = mutableListOf<TopHeadlinesRequest>()
    val fetchedTopHeadlinesRequests = mutableListOf<FeedSpec>()
    val searchRequests = mutableListOf<SearchSpec>()
    val addedFavorites = mutableListOf<Pair<Article, Category?>>()
    val removedFavoriteIds = mutableListOf<String>()
    val notificationArticles = mutableListOf<Article>()

    var topHeadlinesPagingFlow: Flow<PagingData<Article>> = flowOf(PagingData.empty())
    var fetchedTopHeadlines: List<Article> = listOf(testArticle())
    var fetchTopHeadlinesException: Throwable? = null
    var searchPagingFlow: Flow<PagingData<Article>> = flowOf(PagingData.empty())
    var searchPagingFlowFactory: (() -> Flow<PagingData<Article>>)? = null
    val favoriteArticles = MutableStateFlow<List<Article>>(emptyList())
    val favoriteUrls = MutableStateFlow<Set<String>>(emptySet())
    val favoriteCategories = MutableStateFlow<List<Category>>(emptyList())
    val favoriteArticlesByCategory = mutableMapOf<Category, Flow<List<Article>>>()
    val requestedFavoriteCategories = mutableListOf<Category>()
    val articlesById = mutableMapOf<String, Flow<Article?>>()

    var clearCacheCallCount = 0
    var clearCacheException: Exception? = null
    val deleteOlderThanThresholds = mutableListOf<Long>()
    var deleteOlderThanException: Exception? = null
    var replaceNotificationArticleException: Throwable? = null

    override fun observeTopHeadlinesPaging(
        spec: FeedSpec,
        syncInterval: Long,
    ): Flow<PagingData<Article>> {
        topHeadlinesRequests += TopHeadlinesRequest(spec, syncInterval)
        return topHeadlinesPagingFlow
    }

    override suspend fun fetchTopHeadlines(spec: FeedSpec): List<Article> {
        fetchedTopHeadlinesRequests += spec
        fetchTopHeadlinesException?.let { throw it }
        return fetchedTopHeadlines
    }

    override fun searchNewsPaging(spec: SearchSpec): Flow<PagingData<Article>> {
        searchRequests += spec
        return searchPagingFlowFactory?.invoke() ?: searchPagingFlow
    }

    override fun observeFavoriteArticles(): Flow<List<Article>> = favoriteArticles

    override fun observeFavoriteUrls(): Flow<Set<String>> = favoriteUrls

    override fun observeFavoriteArticlesForCategory(
        category: Category
    ): Flow<List<Article>> {
        requestedFavoriteCategories += category
        return favoriteArticlesByCategory[category] ?: flowOf(emptyList())
    }

    override fun observeFavoriteCategories(): Flow<List<Category>> = favoriteCategories

    override suspend fun addToFavorites(article: Article, category: Category?) {
        addedFavorites += article to category
    }

    override suspend fun removeFromFavorites(id: String) {
        removedFavoriteIds += id
    }

    override fun observeArticleById(id: String): Flow<Article?> =
        articlesById[id] ?: flowOf(null)

    override suspend fun clearCachedArticles() {
        clearCacheCallCount += 1
        clearCacheException?.let { throw it }
    }

    override suspend fun deleteCachedArticlesOlderThan(threshold: Long) {
        deleteOlderThanThresholds += threshold
        deleteOlderThanException?.let { throw it }
    }

    override suspend fun replaceNotificationArticle(article: Article) {
        notificationArticles += article
        replaceNotificationArticleException?.let { throw it }
    }
}

internal class FakeSettingsRepository(
    val country: NewsCountry = NewsCountry.US,
    val language: NewsLanguage = NewsLanguage.EN,
    val feedProvider: ProviderId = ProviderId.NEWS_DATA,
    val searchProvider: ProviderId = ProviderId.NEWS_DATA,
    enabledSearchProviders: Set<ProviderId> = setOf(ProviderId.NEWS_DATA),
    var syncInterval: Long = 10 * 60 * 1_000L,
    var notificationsEnabled: Boolean = false,
    var notificationPermissionRequested: Boolean = false,
) : SettingsRepository {
    val countryPreference = MutableStateFlow(country)
    val languagePreference = MutableStateFlow(language)
    val feedProviderPreference = MutableStateFlow(feedProvider)
    val searchProviderPreference = MutableStateFlow(searchProvider)
    val enabledSearchProvidersPreference = MutableStateFlow(enabledSearchProviders)
    val notificationPreferences = MutableStateFlow(notificationsEnabled)
    val notificationPermissionRequestedPreference =
        MutableStateFlow(notificationPermissionRequested)

    override suspend fun getSyncInterval(): Long = syncInterval

    override suspend fun setCountryPreference(country: NewsCountry) {
        countryPreference.value = country
    }

    override suspend fun setLanguagePreference(language: NewsLanguage) {
        languagePreference.value = language
    }

    override fun observeCountryPreference(): Flow<NewsCountry> = countryPreference

    override fun observeLanguagePreference(): Flow<NewsLanguage> = languagePreference

    override suspend fun setFeedProviderPreference(providerId: ProviderId) {
        feedProviderPreference.value = providerId
    }

    override suspend fun setSearchProviderPreference(providerId: ProviderId) {
        searchProviderPreference.value = providerId
    }

    override fun observeFeedProviderPreference(): Flow<ProviderId> = feedProviderPreference

    override fun observeSearchProviderPreference(): Flow<ProviderId> = searchProviderPreference

    override suspend fun setSearchProviderEnabled(providerId: ProviderId, enabled: Boolean) {
        enabledSearchProvidersPreference.value = enabledSearchProvidersPreference.value
            .toMutableSet()
            .apply {
                if (enabled) add(providerId) else remove(providerId)
            }
    }

    override fun observeEnabledSearchProviderIds(): Flow<Set<ProviderId>> =
        enabledSearchProvidersPreference

    override suspend fun setNotificationPreference(enabled: Boolean) {
        notificationsEnabled = enabled
        notificationPreferences.value = enabled
    }

    override fun observeNotificationPreference(): Flow<Boolean> = notificationPreferences

    override suspend fun setNotificationPermissionRequested(requested: Boolean) {
        notificationPermissionRequested = requested
        notificationPermissionRequestedPreference.value = requested
    }

    override fun observeNotificationPermissionRequested(): Flow<Boolean> =
        notificationPermissionRequestedPreference
}

internal class FakeDailyNewsScheduler : DailyNewsScheduler {
    var scheduleCallCount = 0
    var cancelCallCount = 0
    var enqueueOneTimeCallCount = 0

    override fun schedule() {
        scheduleCallCount += 1
    }

    override fun cancel() {
        cancelCallCount += 1
    }

    override fun enqueueOneTime() {
        enqueueOneTimeCallCount += 1
    }
}

internal class FakeSearchHistoryRepository(
    initialHistory: List<String> = emptyList()
) : SearchHistoryRepository {
    val history = MutableStateFlow(initialHistory)
    val savedQueries = mutableListOf<String>()
    val deletedQueries = mutableListOf<String>()

    override fun getSearchHistory(): Flow<List<String>> = history

    override suspend fun saveSearchQuery(query: String) {
        savedQueries += query
        history.value = history.value.filterNot { it == query } + query
    }

    override suspend fun deleteSearchQuery(query: String) {
        deletedQueries += query
        history.value = history.value - query
    }
}

internal class FakeNewsProvidersRepository(
    val categoriesByProvider: MutableMap<ProviderId, Set<Category>> = mutableMapOf(),
    var topHeadlinesProviderIds: Set<ProviderId> = emptySet(),
    var searchProviderIds: Set<ProviderId> = emptySet(),
    var allProviderIds: Set<ProviderId> = emptySet(),
) : NewsProvidersRepository {
    val requestedCategoryProviderIds = mutableListOf<ProviderId>()

    override fun getSupportedCategoriesForProviderId(id: ProviderId): Set<Category> {
        requestedCategoryProviderIds += id
        return categoriesByProvider[id].orEmpty()
    }

    override fun getTopHeadlinesCapableProviderIds(): Set<ProviderId> =
        topHeadlinesProviderIds

    override fun getSearchCapableProviderIds(): Set<ProviderId> = searchProviderIds

    override fun getAvailableProviderIds(): Set<ProviderId> = allProviderIds
}

internal fun testArticle(
    id: String = "article-1",
    originalUrl: String = "https://example.com/$id",
    isFavorite: Boolean = false,
    category: Category? = Category.GENERAL,
): Article = Article(
    id = id,
    providerId = ProviderId.NEWS_DATA,
    publisher = Publisher(
        name = "Test publisher",
        id = "publisher-1",
        websiteUrl = "https://example.com",
    ),
    authors = setOf("Test author"),
    title = "Test article $id",
    description = "Test description",
    originalUrl = originalUrl,
    imageUrl = null,
    publishedAt = 1_000L,
    isFavorite = isFavorite,
    category = category,
)
