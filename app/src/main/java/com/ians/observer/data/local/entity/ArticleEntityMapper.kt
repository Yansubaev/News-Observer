package com.ians.observer.data.local.entity

import com.ians.observer.domain.model.Article

fun ArticleEntity.toArticle(): Article {
    return Article(
        id = url,
        sourceId = sourceId,
        sourceName = sourceName,
        author = author,
        title = title,
        description = description,
        url = url,
        imageUrl = imageUrl,
        publishedAt = publishedAt,
        content = content,
        isFavorite = isFavorite
    )
}

fun Article.toEntity(): ArticleEntity{
    return ArticleEntity(
        url = url,
        sourceId = sourceId,
        sourceName = sourceName,
        author = author,
        title = title,
        description = description,
        imageUrl = imageUrl,
        publishedAt = publishedAt,
        content = content,
        isFavorite = isFavorite,
        savedAt = System.currentTimeMillis()
    )
}
