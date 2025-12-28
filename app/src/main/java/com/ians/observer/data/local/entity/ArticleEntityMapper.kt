package com.ians.observer.data.local.entity

import com.ians.observer.domain.model.Article
import com.ians.observer.domain.model.Category

fun categoryFromString(category: String?): Category? {
    return when (category) {
        "general" -> Category.GENERAL
        "business" -> Category.BUSINESS
        "entertainment" -> Category.ENTERTAINMENT
        "health" -> Category.HEALTH
        "science" -> Category.SCIENCE
        "sports" -> Category.SPORTS
        "technology" -> Category.TECHNOLOGY
        else -> null
    }
}

fun ArticleEntity.toArticle(): Article {
    return Article(
        id = url,
        page = page,
        sourceId = sourceId,
        sourceName = sourceName,
        author = author,
        title = title,
        description = description,
        url = url,
        imageUrl = imageUrl,
        publishedAt = publishedAt,
        content = content,
        isFavorite = isFavorite,
        category = categoryFromString(category)
    )
}

fun Article.toEntity(): ArticleEntity {
    return ArticleEntity(
        url = url,
        page = page,
        sourceId = sourceId,
        sourceName = sourceName,
        author = author,
        title = title,
        description = description,
        imageUrl = imageUrl,
        publishedAt = publishedAt,
        content = content,
        isFavorite = isFavorite,
        savedAt = System.currentTimeMillis(),
        category = category?.value
    )
}
