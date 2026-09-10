package com.ians.observer.data.remote.newsdata

import com.ians.observer.data.remote.provider.NewsProvider
import com.ians.observer.data.remote.provider.exception.ProviderException
import com.ians.observer.data.remote.provider.model.FeedRequest
import com.ians.observer.data.remote.provider.model.PageToken
import com.ians.observer.data.remote.provider.model.ProviderCapabilities
import com.ians.observer.data.remote.provider.model.ProviderPage
import com.ians.observer.data.remote.provider.model.SearchRequest
import com.ians.observer.domain.model.Category
import com.ians.observer.domain.model.ProviderId
import javax.inject.Inject

class NewsDataProvider @Inject constructor(
    val newsDataApi: NewsDataApi
) : NewsProvider {
    override val id: ProviderId = ProviderId.NEWS_DATA
    override val capabilities: Set<ProviderCapabilities> =
        setOf(
            ProviderCapabilities.TOP_HEADLINES,
            ProviderCapabilities.COUNTRY_FILTER,
            ProviderCapabilities.LANGUAGE_FILTER,
            ProviderCapabilities.CATEGORY_FILTER,
            ProviderCapabilities.SEARCH,
        )
    override val supportedCategories: Set<Category> =
        setOf(
            Category.GENERAL,
            Category.BREAKING,
            Category.BUSINESS,
            Category.CRIME,
            Category.DOMESTIC,
            Category.EDUCATION,
            Category.ENTERTAINMENT,
            Category.ENVIRONMENT,
            Category.FOOD,
            Category.HEALTH,
            Category.LIFESTYLE,
            Category.OTHER,
            Category.POLITICS,
            Category.SCIENCE,
            Category.SPORTS,
            Category.TECHNOLOGY,
            Category.TOURISM,
            Category.WORLD,
        )

    override suspend fun loadFeed(
        request: FeedRequest,
        pageToken: PageToken?
    ): ProviderPage {
        val page = pageToken?.value

        val response = newsDataApi.getLatest(
            country = request.country?.toNewsDataCountry(),
            category = request.category?.toNewsDataCategory(),
            language = request.language?.toNewsDataLanguage(),
            page = page,
        )

        if (response.status != "success") {
            throw ProviderException(
                providerId = ProviderId.NEWS_DATA,
                message = "NewsAPI return status: ${response.status}"
            )
        }


        val nextToken = response.nextPage

        println("--------------------------------")
        println(response.results.size)
        println(response.results.filter { !it.duplicate }.size)
        println(response.results.filter { it.duplicate }.map { it.title })

        return ProviderPage(
            articles = response.results.map { it.toRemoteArticle() },
            nextPageToken = nextToken?.let { PageToken(it) }
        )
    }

    override suspend fun search(
        request: SearchRequest,
        pageToken: PageToken?
    ): ProviderPage {
        val page = pageToken?.value

        val response = newsDataApi.searchNews(
            country = request.country?.toNewsDataCountry(),
            category = request.category?.toNewsDataCategory(),
            language = request.language?.toNewsDataLanguage(),
            page = page,
            query = request.query,
            size = 10,
        )

        if (response.status != "success") {
            throw ProviderException(
                providerId = ProviderId.NEWS_DATA,
                message = "NewsAPI return status: ${response.status}"
            )
        }

        val nextToken = response.nextPage

        return ProviderPage(
            articles = response.results.map { it.toRemoteArticle() },
            nextPageToken = nextToken?.let { PageToken(it) }
        )

    }
}
