package com.ians.observer.domain.model

data class Article (
    val id: String,
    val page: Int,
    val sourceId: String?,
    val sourceName: String,
    val author: String?,
    val title: String,
    val description: String?,
    val url: String,
    val imageUrl: String?,
    val publishedAt: Long,
    val content: String?,
    val isFavorite: Boolean = false,
    val category: Category? = null
)

