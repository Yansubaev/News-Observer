package com.ians.observer.data.remote.newsapi

import com.ians.observer.data.remote.provider.NewsProvider
import com.ians.observer.data.remote.provider.exception.ProviderException
import com.ians.observer.data.remote.provider.model.FeedRequest
import com.ians.observer.data.remote.provider.model.PageToken
import com.ians.observer.data.remote.provider.model.ProviderCapabilities
import com.ians.observer.data.remote.provider.model.ProviderPage
import com.ians.observer.data.remote.provider.model.SearchRequest
import com.ians.observer.domain.model.ProviderId
import javax.inject.Inject

class NewsApiProvider @Inject constructor(
    val newsApiApi: NewsApiApi
) : NewsProvider {

    companion object {
        private const val NETWORK_PAGE_SIZE = 10
    }

    override val id: ProviderId = ProviderId.NEWS_API
    override val capabilities: Set<ProviderCapabilities>
        get() = setOf(
            ProviderCapabilities.TOP_HEADLINES,
            ProviderCapabilities.COUNTRY_FILTER,
            ProviderCapabilities.LANGUAGE_FILTER,
            ProviderCapabilities.SEARCH,
            ProviderCapabilities.CATEGORY_FILTER
        )

    override suspend fun loadFeed(
        request: FeedRequest,
        pageToken: PageToken?
    ): ProviderPage {
        val page = pageToken?.value?.toIntOrNull() ?: 1

        val response = newsApiApi.getHeadlines(
            country = request.country?.code ?: "us",
            category = request.category?.value,
            page = page,
            pageSize = NETWORK_PAGE_SIZE
        )

        println("__________________________")
        println(response.articles.size)
        println(response.articles.distinctBy { it.url }.size)
        println(response.totalResults)
        println("++++++++++++++++++++++++++")

        if (response.status != "ok") {
            throw ProviderException(
                providerId = id,
                message = "NewsAPI return status: ${response.status}"
            )
        }

        val loadedThrough = page * NETWORK_PAGE_SIZE
        val nextToken =
            if (response.articles.isEmpty()) null
            else if (response.totalResults <= loadedThrough) null
            else PageToken((page + 1).toString())

        return ProviderPage(
            articles = response.articles.map { it.toRemoteArticle() },
            nextPageToken = nextToken
        )
    }

    override suspend fun search(
        request: SearchRequest,
        pageToken: PageToken?
    ): ProviderPage {
        val page = pageToken?.value?.toIntOrNull() ?: 1

        val response = newsApiApi.searchNews(
            query = request.query,
            page = pageToken?.value?.toIntOrNull() ?: 1,
            pageSize = NETWORK_PAGE_SIZE,
            language = request.language?.code
        )

        if (response.status != "ok") {
            throw ProviderException(
                providerId = id,
                message = "NewsAPI return status: ${response.status}"
            )
        }

        val loadedThrough = page * NETWORK_PAGE_SIZE
        val nextToken =
            if (response.articles.isEmpty()) null
            else if (response.totalResults <= loadedThrough) null
            else PageToken((page + 1).toString())

        return ProviderPage(
            articles = response.articles.map { it.toRemoteArticle() },
            nextPageToken = nextToken
        )
    }
}

