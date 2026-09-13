package com.ians.observer.data.remote.gdelt

import com.ians.observer.data.remote.provider.NewsProvider
import com.ians.observer.data.remote.provider.model.FeedRequest
import com.ians.observer.data.remote.provider.model.PageToken
import com.ians.observer.data.remote.provider.model.ProviderCapabilities
import com.ians.observer.data.remote.provider.model.ProviderPage
import com.ians.observer.data.remote.provider.model.RemoteArticle
import com.ians.observer.data.remote.provider.model.SearchRequest
import com.ians.observer.data.remote.provider.providerCall
import com.ians.observer.domain.model.Category
import com.ians.observer.domain.model.ProviderId
import javax.inject.Inject

class GdeltProvider @Inject constructor(
    private val gdeltApi: GdeltApi
) : NewsProvider {

    companion object {
        private const val NETWORK_PAGE_SIZE = 100
    }

    override val id: ProviderId = ProviderId.GDELT
    override val capabilities: Set<ProviderCapabilities> =
        setOf(
            ProviderCapabilities.SEARCH,
            ProviderCapabilities.COUNTRY_FILTER,
            ProviderCapabilities.LANGUAGE_FILTER,
        )
    override val supportedCategories: Set<Category> = setOf(Category.ALL)

    override suspend fun loadFeed(
        request: FeedRequest,
        pageToken: PageToken?
    ): ProviderPage {
        error("GDELT does not support top headlines")
    }

    override suspend fun search(
        request: SearchRequest,
        pageToken: PageToken?
    ): ProviderPage {
        if (pageToken != null) return EmptyPage

        val query = request.toGdeltQuery() ?: return EmptyPage

        val response = providerCall(id) {
            gdeltApi.searchArticles(
                query = query,
                maxRecords = NETWORK_PAGE_SIZE,
            )
        }

        return ProviderPage(
            articles = response?.articles
                ?.toRemoteArticles()
                ?.distinctBy { it.duplicateKey() }
                .orEmpty(),
            nextPageToken = null
        )
    }
}

private val EmptyPage = ProviderPage(articles = emptyList(), nextPageToken = null)

private fun SearchRequest.toGdeltQuery(): String? {
    val operands = buildList {
        query.trim().takeIf { it.isNotEmpty() }?.let(::add)
        country?.let { add("sourcecountry:${it.toGdeltCountry()}") }
        language?.let { add("sourcelang:${it.toGdeltLanguage()}") }
    }

    return operands.takeIf { it.isNotEmpty() }?.joinToString(separator = " ")
}

private val NonAlphanumeric = Regex("""[^\p{L}\p{N}]+""")

private fun RemoteArticle.duplicateKey(): String =
    title.lowercase().replace(NonAlphanumeric, "").ifEmpty { id }
