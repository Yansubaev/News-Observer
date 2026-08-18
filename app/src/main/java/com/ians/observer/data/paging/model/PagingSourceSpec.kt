package com.ians.observer.data.paging.model

import com.ians.observer.domain.model.Category
import com.ians.observer.domain.model.NewsCountry
import com.ians.observer.domain.model.NewsLanguage

sealed interface PagingSourceSpec {
    data class Feed(
        val country: NewsCountry,
        val language: NewsLanguage,
        val category: Category?
    ) : PagingSourceSpec

    data class Search(
        val query: String,
        val language: NewsLanguage?,
        val country: NewsCountry?
    ) : PagingSourceSpec
}