package com.ians.observer.data.remote.provider

import com.ians.observer.data.remote.provider.model.FeedRequest
import com.ians.observer.data.remote.provider.model.PageToken
import com.ians.observer.data.remote.provider.model.ProviderCapabilities
import com.ians.observer.data.remote.provider.model.ProviderPage
import com.ians.observer.data.remote.provider.model.SearchRequest
import com.ians.observer.domain.model.Category
import com.ians.observer.domain.model.ProviderId

interface NewsProvider {
    val id: ProviderId
    val capabilities: Set<ProviderCapabilities>
    val supportedCategories: Set<Category>

    suspend fun loadFeed(
        request: FeedRequest,
        pageToken: PageToken?,
    ): ProviderPage

    suspend fun search(
        request: SearchRequest,
        pageToken: PageToken?,
    ): ProviderPage
}