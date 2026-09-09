package com.ians.observer.data.remote.provider.model

import com.ians.observer.domain.model.Category
import com.ians.observer.domain.model.ProviderId

data class RemoteArticle (
    val id: String,
    val providerId: ProviderId,
    val publisherId: String?,
    val publisherName: String,
    val publisherWebsiteUrl: String,
    val authors: Set<String>?,
    val title: String,
    val description: String?,
    val originalUrl: String,
    val imageUrl: String?,
    val publishedAt: Long,
    val category: Category? = null,
)

