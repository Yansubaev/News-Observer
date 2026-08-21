package com.ians.observer.domain.model

data class FeedSpec(
    val country: NewsCountry,
    val language: NewsLanguage,
    val category: Category?,
    val providerIds: List<ProviderId>
)

