package com.ians.observer.domain.model

data class Article(
    val id: String,
    val providerId: ProviderId,
    val publisher: Publisher,
    val authors: Set<String>?,
    val title: String,
    val description: String?,
    val originalUrl: String,
    val imageUrl: String?,
    val publishedAt: Long,
    val isFavorite: Boolean = false,
    val category: Category? = null,
)

