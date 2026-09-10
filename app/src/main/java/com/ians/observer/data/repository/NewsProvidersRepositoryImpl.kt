package com.ians.observer.data.repository

import com.ians.observer.data.remote.provider.NewsProviderRegistry
import com.ians.observer.data.remote.provider.model.ProviderCapabilities
import com.ians.observer.domain.model.Category
import com.ians.observer.domain.model.ProviderId
import com.ians.observer.domain.repository.NewsProvidersRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsProvidersRepositoryImpl @Inject constructor(
    private val newsProviderRegistry: NewsProviderRegistry,
) : NewsProvidersRepository {
    override fun getSupportedCategoriesForProviderId(id: ProviderId): Set<Category> {
        return newsProviderRegistry
            .require(id.takeIf { it in newsProviderRegistry.availableIds } ?: ProviderId.NEWS_DATA)
            .supportedCategories

    }

    override fun getTopHeadlinesCapableProviderIds(): Set<ProviderId> =
        newsProviderRegistry.idsSupporting(ProviderCapabilities.TOP_HEADLINES).toSet()


    override fun getSearchCapableProviderIds(): Set<ProviderId> =
        newsProviderRegistry.idsSupporting(ProviderCapabilities.SEARCH).toSet()

    override fun getAvailableProviderIds(): Set<ProviderId> =
        newsProviderRegistry.availableIds

}