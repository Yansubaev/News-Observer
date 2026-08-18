package com.ians.observer.data.remote.provider.model

import com.ians.observer.domain.model.Category
import com.ians.observer.domain.model.NewsLanguage
import com.ians.observer.domain.model.NewsCountry

data class FeedRequest(
    val country: NewsCountry?,
    val language: NewsLanguage?,
    val category: Category?,
)

