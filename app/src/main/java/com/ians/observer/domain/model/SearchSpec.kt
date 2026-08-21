package com.ians.observer.domain.model

data class SearchSpec(
    val query: String,
    val country: NewsCountry,
    val language: NewsLanguage,
    val category: Category?,
    val providerIds: List<ProviderId>
)