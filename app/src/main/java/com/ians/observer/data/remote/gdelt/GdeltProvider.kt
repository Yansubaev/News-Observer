package com.ians.observer.data.remote.gdelt

import com.google.gson.JsonParseException
import com.ians.observer.data.remote.provider.NewsProvider
import com.ians.observer.data.remote.provider.exception.ProviderException
import com.ians.observer.data.remote.provider.model.FeedRequest
import com.ians.observer.data.remote.provider.model.PageToken
import com.ians.observer.data.remote.provider.model.ProviderCapabilities
import com.ians.observer.data.remote.provider.model.ProviderPage
import com.ians.observer.data.remote.provider.model.RemoteArticle
import com.ians.observer.data.remote.provider.model.SearchRequest
import com.ians.observer.domain.model.Category
import com.ians.observer.domain.model.ProviderId
import retrofit2.HttpException
import javax.inject.Inject

class GdeltProvider @Inject constructor(
    private val gdeltApi: GdeltApi
) : NewsProvider {

    companion object {
        private const val NETWORK_PAGE_SIZE = 100
        private const val RATE_LIMIT_CODE = 429
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
        throw ProviderException(
            providerId = id,
            message = "GDELT does not support top headlines"
        )
    }

    override suspend fun search(
        request: SearchRequest,
        pageToken: PageToken?
    ): ProviderPage {
        if (pageToken != null) return EmptyPage

        val query = request.toGdeltQuery() ?: return EmptyPage

        val response = try {
            gdeltApi.searchArticles(
                query = query,
                maxRecords = NETWORK_PAGE_SIZE,
            )
        } catch (e: HttpException) {
            throw ProviderException(
                providerId = id,
                message = if (e.code() == RATE_LIMIT_CODE) {
                    "GDELT rate limit reached, try again in a few seconds"
                } else {
                    "GDELT returned HTTP ${e.code()}"
                }
            )
        } catch (e: JsonParseException) {
            throw ProviderException(
                providerId = id,
                message = "GDELT returned a malformed response: ${e.message}"
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
