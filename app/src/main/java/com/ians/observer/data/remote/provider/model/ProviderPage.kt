package com.ians.observer.data.remote.provider.model

data class ProviderPage(
    val articles: List<RemoteArticle>,
    val nextPageToken: PageToken?,
)

